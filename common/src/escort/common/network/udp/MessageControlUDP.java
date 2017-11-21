package escort.common.network.udp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.systime.SystemTime;

/**
 * The UDP implementation of MessageControl
 * @author Kwong Hei Tsang
 *
 */
public class MessageControlUDP implements MessageControl {

	private final DatagramSocket socket;
	private final int port;
	private final InetAddress addr;
	private final Queue<Message> receivedMessages;
	private final Object inputLock;
	//the thread calling read receiveMessage
	private long message_seq;
	private long message_seq_recv;
	private final Object outputLock;
	//whether this control is the client
	private final boolean isClient;
	//the thread for listening on the socket
	private final Thread clientReceivingThread;
	//AES cipher
	private final SecretKeySpec key;
	private final SecureRandom rand;
	private final Cipher auth;
	//indicating whether this message control is closed
	private boolean isClosed;
	private boolean messageput;
	
	/**
	 * Construct a message control with UDP
	 * @param socket the UDP socket
	 * @param addr The address of the remote end
	 * @param port The port of the remote end
	 * @param isClient whether this is the client
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws InvalidAlgorithmParameterException 
	 */
	public MessageControlUDP(DatagramSocket socket, InetAddress addr, int port, boolean isClient, byte[] aeskey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
		//initialize the cipher
		this.rand = new SecureRandom();
		this.key = new SecretKeySpec(aeskey,"AES");
		this.auth = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] zeroiv = new byte[16];
		Arrays.fill( zeroiv, (byte) 0 );
		this.auth.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(zeroiv));
		
		//initialize the Message Control
		this.socket = socket;
		this.receivedMessages = new ConcurrentLinkedQueue<Message>();
		this.addr = addr;
		this.port = port;
		this.inputLock = new Object();
		this.message_seq = 0;
		this.message_seq_recv = 0;
		this.outputLock = new Object();
		this.isClient = isClient;
		this.isClosed = false;
		if(this.isClient){
			this.clientReceivingThread = new Thread(() -> this.clientReceiver(),"Client Packet Receiver");
			this.clientReceivingThread.start();
		}else{
			this.clientReceivingThread = null;
		}
		this.messageput = false;
	}
	
	/**
	 * Send a message
	 */
	@Override
	public void sendMessage(Message command) throws IOException {
		synchronized(this.outputLock){
			try{
				//create stream Message
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(UDPConfig.UDP_BUFFER_SIZE);
				ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
							
				//write Message
				os.flush();
				os.writeObject(new MessageStamp(++this.message_seq,SystemTime.milliTime(),command,false,0));
				os.flush();
							
				//encrypt message
				byte[] sendBuf = byteStream.toByteArray();
				byte[] iv = new byte[16];
				this.rand.nextBytes(iv);
				Cipher enc = Cipher.getInstance("AES/CBC/PKCS5Padding");
				enc.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(iv));
				//create the packet content
				byte[] cipher = enc.doFinal(sendBuf);
				byte[] authcode = this.auth.doFinal(sendBuf);
				byte[] packetcontent = new byte[iv.length+16+cipher.length];
				System.arraycopy(iv, 0, packetcontent, 0, iv.length);
				System.arraycopy(authcode, authcode.length-16, packetcontent, iv.length, 16);
				System.arraycopy(cipher, 0, packetcontent, iv.length+16, cipher.length);
				
				//send the packet
				DatagramPacket packet = new DatagramPacket(packetcontent, packetcontent.length, this.addr, this.port);
				this.socket.send(packet);
					
				//close socket
				os.close();
				byteStream.close();
			}catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
				this.close();
				throw new IOException("There is a problem with the UDP sending");
			}
		}
	}

	/**
	 * Receive the next message
	 */
	@Override
	public synchronized Message receiveMessage() throws ClassNotFoundException, IOException {
		if(this.isClosed()){
			throw new IOException("Socket closed");
		}
		
		synchronized(inputLock){
			//get message
			Message msg = null;
			msg = this.receivedMessages.poll();
			
			//Message received successfully
			if(msg != null){
				return msg;
			}
			
			//has to wait
			try{
				this.messageput = false;
				long starttime = SystemTime.milliTime();
				while(!this.messageput){
					long waittime = UDPConfig.UDP_MAX_INACTIVITY - (SystemTime.milliTime()-starttime);
					if(waittime > 0 && !this.isClosed()){
						this.wait(waittime);
					}else{
						this.close();
						throw new IOException("Connection lost.");
					}
				}
			}catch(InterruptedException e){
				throw new IOException("Receiving thread interrupted");
			}
			
			return receiveMessage();
		}
	}
	
	///**
	// * Second try to receive a message
	// * @return
	// * @throws ClassNotFoundException
	// * @throws IOException
	// 
	//private Message receiveMessage2() throws ClassNotFoundException, IOException {
	//	//get message
	//	Message msg = null;
	//	msg = this.receivedMessages.poll();
	//	
	//	if(msg == null){
	//		//failed, timeout
	//		this.close();
	//		throw new IOException("Connection lost.");
	//	}
	//	//return message
	//	return msg;
	//}

	/**
	 * Close the message control
	 */
	@Override
	public synchronized void close() throws IOException {
		this.isClosed = true;
		this.notifyAll();
		if(this.isClient){
			this.clientReceivingThread.interrupt();
			this.socket.close();
		}
	}
	
	/**
	 * Is this message control closed?
	 * @return whether this message control is closed
	 */
	public boolean isClosed(){
		return this.isClosed;
	}

	/**
	 * Get the protocol type of this message control
	 */
	@Override
	public String protocol() {
		return "udp";
	}
	
	/**
	 * Put the message to this message control to allow it to receive
	 * @param msg The message control
	 */
	public void putMessage(MessageStamp msg){
		if(msg.seq <= this.message_seq_recv){
			//Prevent replay attack
			return;
		}else{
			this.message_seq_recv = msg.seq;
		}
		
		this.receivedMessages.offer(msg.msg);
		
		synchronized(this){
			this.messageput = true;
			this.notifyAll();
		}
		
	}
	
	/**
	 * The method for the client receiver
	 */
	private void clientReceiver(){
		long lastKeepAlive = SystemTime.milliTime();
		
		while(true){
			try {
				//create buffer
				byte[] buffer = new byte[UDPConfig.UDP_BUFFER_SIZE];
				
				//receive packet
				DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
				//this.socket.setSoTimeout(UDPConfig.UDP_CLIENT_RECEIVE_MAXTIME);
				this.socket.receive(packet);
				
				//get the packet content
				if(packet.getLength() < 32){
					throw new MalformedMessageException();
				}
				byte[] iv = new byte[16];
				byte[] authcode = new byte[16];
				byte[] cipher = new byte[packet.getLength()-32];
				System.arraycopy(buffer, 0, iv, 0, 16);
				System.arraycopy(buffer, 16, authcode, 0, 16);
				System.arraycopy(buffer, 32, cipher, 0, packet.getLength()-32);
				
				//Decrypt the content
				Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
				dec.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(iv));
				byte[] plain = dec.doFinal(cipher);
				byte[] authcodecipher = this.auth.doFinal(plain);
				byte[] authcodecheck = new byte[16];
				System.arraycopy(authcodecipher, authcodecipher.length-16, authcodecheck, 0, 16);
				if(!Arrays.equals(authcode, authcodecheck)){
					throw new MalformedMessageException();
				}
				
				//read message
				ByteArrayInputStream byteStream = new ByteArrayInputStream(plain);
				ObjectInputStream input = new ObjectInputStream(byteStream);
				MessageStamp msg = null;
				Object obj = input.readObject();
				if(obj instanceof MessageStamp){
					msg = (MessageStamp)obj;
				}else{
					throw new MalformedMessageException();
				}
				
				//close streams
				input.close();
				byteStream.close();
				
				//check if the remote address and port is correct
				if(packet.getAddress().equals(this.addr) && packet.getPort() == this.port){
					this.putMessage(msg);
				}
			} catch (Exception e){
			}
			
			if(Thread.currentThread().isInterrupted()){
				break;
			}
			
			//send keepalive
			try{
				if(SystemTime.milliTime() - lastKeepAlive >= UDPConfig.UDP_KEEPALIVE){
					this.sendMessage(new Message(Message.KEEP_ALIVE,null,null));
					lastKeepAlive = SystemTime.milliTime();
				}
			}catch(IOException e){
			}
			
			if(Thread.currentThread().isInterrupted()){
				break;
			}
		}
		
		//close the socket
		this.socket.close();
	}
	
	/**
	 * Get the remote address
	 * @return the remote address
	 */
	public InetAddress getAddress(){
		return this.addr;
	}
	
	/**
	 * Get the remote port
	 * @return The remote port
	 */
	public int getPort(){
		return this.port;
	}
	
	/**
	 * Get the AES encryption key
	 * @return The AES encryption key
	 */
	public SecretKeySpec getAESKey(){
		return this.key;
	}

	/**
	 * Get the authentication cipher
	 * @return The authentication cipher
	 */
	public Cipher getAuthCipher(){
		return this.auth;
	}
}
