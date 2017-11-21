package escort.common.network.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;

/**
 * A TCP implementation of MessageControl
 * @author kh_tsang
 *
 */
public class MessageControlTCP implements MessageControl{

	private final SSLSocket socket;
	private final ObjectInputStream input;
	private final ObjectOutputStream output;
	private final Object inputLock;
	private final Object outputLock;
	
	/**
	 * Create a message control with TCP
	 * @param socket The TCP socket
	 * @throws IOException
	 */
	public MessageControlTCP(SSLSocket socket) throws IOException{
		this.socket = socket;
		this.socket.setKeepAlive(true);
		this.socket.setSoTimeout(10000);
		this.output = new ObjectOutputStream(socket.getOutputStream());
		this.input = new ObjectInputStream(socket.getInputStream());
		this.inputLock = new Object();
		this.outputLock = new Object();
		
		new Thread(() -> this.keepAlive()).start();
	}
	
	/**
	 * Send a message
	 */
	@Override
	public void sendMessage(Message Message) throws IOException{
		synchronized(outputLock){
			this.output.writeObject(Message);
		}
	}

	/**
	 * Receive the next message
	 */
	@Override
	public Message receiveMessage() throws ClassNotFoundException, IOException, MalformedMessageException{
		synchronized(inputLock){
			Object message = null;
			
			// keep alive
			while((message = this.input.readObject()) instanceof Message && ((Message) message).messageType == Message.KEEP_ALIVE);
			
			if(message instanceof Message){
				return (Message)message;
			}else{
				throw new IOException("Protocol error");
			}
		}
	}

	/**
	 * Close the message control and required connections
	 */
	@Override
	public synchronized void close() throws IOException{
		this.input.close();
		this.output.close();
		this.socket.close();
	}

	/**
	 * Get the type of the protocol
	 */
	@Override
	public String protocol(){
		return "tcp";
	}
	
	public Certificate getCert() throws SSLPeerUnverifiedException{
		return this.socket.getSession().getPeerCertificates()[0];
	}
	
	private void keepAlive(){
		Message keepalive = new Message(Message.KEEP_ALIVE, null, null);
		
		try{
			while(true){
				this.sendMessage(keepalive);
				
				Thread.sleep(3000);
			}
		} catch (InterruptedException e){
		} catch (IOException e) {
		}
		
	}
}
