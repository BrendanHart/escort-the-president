package escort.server.network.test;
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
	
	// For testing protocol switch
	private Message credential;
	
	public FakeMessageControl(Queue<Message> toServer){
		this.sentmessage = 0;
		this.toServer = toServer;
		this.fromServer = new ConcurrentLinkedQueue<Message>();
		this.fromServerWait = new Object();
		this.receivingthread = null;
		this.messageput = false;
		this.credential = null;
	}
	
	@Override
	public void sendMessage(Message command) throws IOException {
		// filter game messages
		switch(command.messageType){
		case Message.PRES_FOLLOW:
		case Message.UNIT_MOVED:
		case Message.PRES_UNFOLLOW:
		case Message.GRENADE_ID:
		case Message.THROW_GRENADE:
		case Message.PISTOL_BULLET:
		case Message.MG_BULLET:
		case Message.WEAPON_RELOAD_ACK:
		case Message.WEAPON_SWITCH_ACK:
		case Message.HP_LEFT:
		case Message.POWERUP_ASSIGNMENT:
		case Message.POWERUP_USED:
		case Message.SHIELD_HP_LEFT:
		case Message.ROLL:
		case Message.MOVE:
		case Message.SHOOT_GUN:
		case Message.RESPAWN:
			return;
		default:
			System.out.println(command);
			this.sentmessage += 1;
			break;
		}
		
		if(command.messageType == Message.PROTOCOL_SWITCH_RESPONSE){
			this.credential = command;
		}
		
		this.fromServer.offer(command);
		synchronized(this.fromServerWait){
			this.fromServerWait.notifyAll();
		}
	}
	
	public Message getSwitchCredential(){
		return this.credential;
	}

	@Override
	public Message receiveMessage() throws ClassNotFoundException, IOException {
		this.receivingthread = Thread.currentThread();
		try{
			while(true){
				Message msg = toServer.poll();
				if(msg != null){
					return msg;
				}
				
				synchronized(this){
					this.messageput = false;
					while(!this.messageput){
						this.wait();
					}
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
