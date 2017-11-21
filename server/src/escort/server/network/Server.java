package escort.server.network;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.network.tcp.MessageControlTCP;
import escort.common.network.udp.MessageControlUDP;
import escort.common.network.udp.MessageStamp;
import escort.common.network.udp.UDPConfig;
import escort.common.systime.SystemTime;
import escort.server.console.ServerConsole;

/**
 * The server object to be first initialized when server starts
 * @author Kwong Hei Tsang
 *
 */
public final class Server {

	private Thread tcpServer;
	private ServerSocket tcpSocket;
	private DatagramSocket udpSocket = null;
	private Thread udpServer;
	private Thread udpClientsClearingThread;
	private ServerSide ss;
	private final int port;
	private final Map<ClientInfo, MessageControlUDP> udpclients;
	private final Set<MessageControlTCP> tcpClients;
	private final Object tcpclientsLock;
	private final Object udpclientsLock;
	private final String keystore;
	private final String key;
	private final String keypassword;
	private final ServerConsole console;
	
	private Cipher rsacipher = null;
	private Certificate cert = null;
	private PrivateKey privatekey = null;
	
	/**
	 * Create a server object
	 * @param port Port number
	 * @param keystore KeyStore in JKS format
	 * @param alias The alias of the key to be used
	 * @param password The password of the keystore and the private key
	 */
	public Server(int port, String keystore, String alias, String password){
		//initialize parameters
		this.keystore = keystore;
		this.key = alias;
		this.keypassword = password;
		
		//initialize server
		this.port = port;
		this.ss = new ServerSide(this);
		this.udpclients = new HashMap<ClientInfo, MessageControlUDP>();
		this.udpclientsLock = new Object();
		this.tcpclientsLock = new Object();
		this.tcpSocket = null;
		this.tcpClients = new HashSet<>();
		this.console = new ServerConsole(this.ss);
		(new Thread(this.console, "ServerConsole")).start();

		// start TCP server
		this.tcpServer = new Thread(() -> this.tcpServer(), "TCPServer");
		this.tcpServer.start();

		// start UDPClientsClearingThread
		this.udpClientsClearingThread = new Thread(() -> this.removeClosedUDPClientsAndSendKeepalive(), "UDPKeepAlive");
		this.udpClientsClearingThread.start();

		// start UDP server
		this.udpServer = new Thread(() -> this.udpServer(), "UDPServer");
		this.udpServer.start();
	}
	
	/**
	 * Create a server object
	 * 
	 * @param port
	 *            The port to listen on for both TCP and UDP
	 */
	public Server(int port) {
		this(port,"key","mykey","1234567");
	}

	
	/**
	 * Shutdown the server
	 */
	public final void interrupt() {
		this.tcpServer.interrupt();
		this.udpServer.interrupt();
		
		if(this.tcpSocket != null){
			try {
				//Need to make accept throw exception
				this.tcpSocket.close();
			} catch (IOException e) {
			}
		}
		
		if(this.udpSocket != null){
			this.udpSocket.close();
		}
		
		try {
			this.tcpServer.join();
			this.udpServer.join();
		} catch (InterruptedException e1) {
			//won't happen anyway
		}
		
		//close the connections
		for(MessageControlTCP control : this.tcpClients){
			try {
				control.close();
			} catch (IOException e) {
			}
		}
		for(MessageControlUDP control : this.udpclients.values()){
			try {
				control.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Method run by the TCP server
	 */
	private final void tcpServer() {
		ServerSocketFactory ssocketFactory = null;
		ServerSocket ssocket = null;

		try {
			// load server certificate
			// System.setProperty("javax.net.ssl.keyStore",System.getProperty("user.dir")
			// + "/key");
			System.setProperty("javax.net.ssl.keyStore", keystore);
			System.setProperty("javax.net.ssl.keyStorePassword", keypassword);
			
			// listen to port
			ssocketFactory = SSLServerSocketFactory.getDefault();
			ssocket = ssocketFactory.createServerSocket(port);
			this.tcpSocket = ssocket;
		} catch (IOException e) {
			System.out.println("Exception occured at TCP server.");
			System.out.println("Please check if the TCP port " + this.port + " is available and you have the correct key at key store file.");
			System.out.println("Please check if you have supplied the correct key password as well.");
			return;
		} finally {
			// Set the TCP server state to ready
			this.console.tcpReady();
		}
		
		while(true){
			try{
				// accept connection and get data input stream
				SSLSocket s = (SSLSocket) ssocket.accept();
				
				MessageControlTCP control = new MessageControlTCP(s);
				synchronized(this.tcpclientsLock){
					this.tcpClients.add(control);
				}
				Player player = new Player(control, ss);

				// Start player thread
				player.start();
			}catch(IOException e){
				//e.printStackTrace();
			}
			// Check if current thread is interrupted
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
		}

		try {
			ssocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("TCP server shut down.");
	}
	
	/**
	 * Remove a specific TCP client
	 * @param control
	 */
	public final void removeTCPClient(MessageControl control){
		synchronized(this.tcpclientsLock){
			this.tcpClients.remove(control);
		}
	}

	/**
	 * Method run by UDP Server
	 * @throws InvalidKeyException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CertificateException 
	 */
	private final void udpServer(){
		try {
			//try to load the cipher
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			store.load(new FileInputStream(keystore), keypassword.toCharArray());
			this.privatekey = (PrivateKey) store.getKey(key, keypassword.toCharArray());
			this.cert = store.getCertificate(key);
			//Initialize decryption cipher
			this.rsacipher = Cipher.getInstance("RSA");
			this.rsacipher.init(Cipher.DECRYPT_MODE, privatekey);
			
			// try to listen to port
			this.udpSocket = new DatagramSocket(this.port);
		} catch (Exception e) {
			System.out.println("UDP Server failed initialize.");
			System.out.println("Please check if your Java implementation supports RSA and port " + this.port + " is free on your server.");
			return;
		} finally {
			this.console.udpReady(this.cert);
		}
		
		while (true) {
			try {
				// create buffer
				byte[] buffer = new byte[UDPConfig.UDP_BUFFER_SIZE];

				// receive packet
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				//socket.setSoTimeout(UDPConfig.UDP_SERVER_RECEIVE_MAXTIME);
				this.udpSocket.receive(packet);

				// check if client exists
				ClientInfo info = new ClientInfo(packet.getAddress(), packet.getPort());
				MessageControlUDP control;
				synchronized (this.udpclientsLock) {
					control = this.udpclients.get(info);
				}
				if (control != null) {
					// client exists, decrypt message and put message
					new Thread(() -> readMessage(control,packet),"Read Message UDP").start();
				} else {
					//client does not exist, add client
					new Thread(() -> initializeClient(packet,info),"Initialize UDP Client").start();
				}
			} catch (IOException e1) {
				//e1.printStackTrace();
			}

			// Check if current thread is interrupted
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
		}
		
		// server shuts down
		// interrupt the clearing thread
		this.udpClientsClearingThread.interrupt();
		this.udpSocket.close();
		System.out.println("UDP server shut down.");
	}

	/**
	 * Method for automatic cleaning of closed UDP clients
	 */
	private final void removeClosedUDPClientsAndSendKeepalive() {
		long starttime;
		long looptime;
		try {
			while (true) {
				starttime = SystemTime.milliTime();
				// Get closed clients alive clients
				LinkedList<MessageControlUDP> closedClients = new LinkedList<MessageControlUDP>();
				synchronized (this.udpclientsLock) {
					for (MessageControlUDP client : udpclients.values()) {
						if (client.isClosed()) {
							closedClients.add(client);
						} else {
							//send keepalive to open clients
							try {
								client.sendMessage(new Message(Message.KEEP_ALIVE, null, null));
							} catch (IOException e) {
							}
						}
					}
				}

				// Remove closed clients
				for (MessageControlUDP client : closedClients) {
					synchronized (this.udpclientsLock) {
						this.udpclients.remove(new ClientInfo(client.getAddress(), client.getPort()), client);
					}
				}
				
				looptime = SystemTime.milliTime() - starttime;
				if(UDPConfig.UDP_KEEPALIVE > looptime){
					Thread.sleep(UDPConfig.UDP_KEEPALIVE - looptime);
				}
				if(Thread.currentThread().isInterrupted()){
					break;
				}
			}
		} catch (InterruptedException e) {
			// Server is stopping
			//e.printStackTrace();
		}

		System.out.println("UDP keepalive module shut down.");
	}

	/**
	 * Read message method and put message into message control
	 * @param control
	 * @param packet
	 */
	private final void readMessage(MessageControlUDP control, DatagramPacket packet){
		try{
			//Get AES Key
			SecretKeySpec aeskey = control.getAESKey();
			
			//Decrypt the message
			//get the packet content
			if(packet.getLength() < 32){
				throw new MalformedMessageException();
			}
			byte[] iv = new byte[16];
			byte[] authcode = new byte[16];
			byte[] cipher = new byte[packet.getLength()-32];
			byte[] buffer = packet.getData();
			System.arraycopy(buffer, 0, iv, 0, 16);
			System.arraycopy(buffer, 16, authcode, 0, 16);
			System.arraycopy(buffer, 32, cipher, 0, packet.getLength()-32);
			
			//Decrypt the content
			Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
			dec.init(Cipher.DECRYPT_MODE, aeskey, new IvParameterSpec(iv));
			byte[] plain = dec.doFinal(cipher);
			byte[] authcodecipher = control.getAuthCipher().doFinal(plain);
			byte[] authcodecheck = new byte[16];
			System.arraycopy(authcodecipher, authcodecipher.length-16, authcodecheck, 0, 16);
			if(!Arrays.equals(authcode, authcodecheck)){
				throw new MalformedMessageException();
			}
			
			// read message
			ByteArrayInputStream byteStream = new ByteArrayInputStream(plain);
			ObjectInputStream input = new ObjectInputStream(byteStream);
			MessageStamp msg = null;
			Object obj = input.readObject();
			if(obj instanceof MessageStamp){
				msg = (MessageStamp)obj;
			}else{
				throw new MalformedMessageException();
			}

			// close streams
			input.close();
			byteStream.close();
			
			//Put the message
			control.putMessage(msg);
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
	
	/**
	 * The client is not yet in client list, initialize client
	 * @param socket
	 * @param packet
	 * @param info
	 */
	private final void initializeClient(DatagramPacket packet, ClientInfo info){
		try{
			//copy encrypted AES key to new array
			byte[] cipher = new byte[packet.getLength()];
			byte[] buffer = packet.getData();
			System.arraycopy(buffer,0,cipher,0,packet.getLength());
			
			if(cipher.length == 3){
				//initial request
				byte[] certbyte = this.cert.getEncoded();
				DatagramPacket respacket = new DatagramPacket(certbyte,certbyte.length,packet.getAddress(),packet.getPort());
				this.udpSocket.send(respacket);
				return;
			}
			
			//decrypt AES key
			byte[] aeskey = this.rsacipher.doFinal(cipher);
			
			if(aeskey.length != 16){
				return;
			}
			// client does not exist, add client
			MessageControlUDP newcontrol = new MessageControlUDP(this.udpSocket, packet.getAddress(), packet.getPort(), false, aeskey);
			synchronized (this.udpclientsLock) {
				this.udpclients.put(info, newcontrol);
			}
			Player player = new Player(newcontrol, ss);
			player.start();
			
			//send acknowledge to client
			byte[] ackbytes = new byte[3];
			Arrays.fill(ackbytes, (byte)0);
			DatagramPacket ackpacket = new DatagramPacket(ackbytes,ackbytes.length,packet.getAddress(),packet.getPort());
			this.udpSocket.send(ackpacket);
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
}
