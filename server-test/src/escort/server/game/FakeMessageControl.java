package escort.server.game;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.network.Message;
import escort.common.network.MessageControl;

/**
 * A fake message control for simulating a network client, but for testing Lobby and network protocol only.
 * Do not use this to test game logic
 * @author Kwong Hei Tsang
 *
 */
public class FakeMessageControl implements MessageControl {

	private int sentmessage;
	private final Queue<Message> toServer;
	private final Queue<Message> fromServer;
	private final Object fromServerWait;
	private Thread receivingthread;
	private boolean messageput;
	
	public FakeMessageControl(){
		this.sentmessage = 0;
		this.toServer = new ConcurrentLinkedQueue<Message>();
		this.fromServer = new ConcurrentLinkedQueue<Message>();
		this.fromServerWait = new Object();
		this.receivingthread = null;
		this.messageput = false;
	}
	
	@Override
	public void sendMessage(Message command) throws IOException {
		this.fromServer.offer(command);
		synchronized(this.fromServerWait){
			this.fromServerWait.notifyAll();
		}
	}

	@Override
	public synchronized Message receiveMessage() throws ClassNotFoundException, IOException {
		this.receivingthread = Thread.currentThread();
		try{
			while(true){
				Message msg = toServer.poll();
				if(msg != null){
					return msg;
				}
				
				this.messageput = false;
				while(!this.messageput){
					this.wait();
				}
			}
		}catch(InterruptedException e){
			throw new IOException("Connection broken");
		}
	}

	@Override
	public void close() throws IOException {
		
	}

	@Override
	public String protocol() {
		// TODO Auto-generated method stub
		return "fake";
	}

	
	public int getSentMessageCount(){
		return this.sentmessage;
	}
	
	public void terminate(){
		synchronized(this.receivingthread){
			this.receivingthread.interrupt();
		}
	}
	
	public void putMessage(Message command){
		this.toServer.add(command);
		synchronized(this){
			this.messageput = true;
			this.notifyAll();
		}
	}
	
	public Message getMessage(){
		Message msg = null;
		while((msg = this.fromServer.poll()) == null){
			synchronized(this.fromServerWait){
				try {
					this.fromServerWait.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		return msg;
	}
}
