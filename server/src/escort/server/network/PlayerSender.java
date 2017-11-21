package escort.server.network;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import escort.common.network.CriticalCheck;
import escort.common.network.Message;
import escort.common.network.Sender;

/**
 * Sever sender thread for each client
 * 
 * @author Kwong Hei Tsang
 *
 */
public class PlayerSender extends Thread implements Sender {

	private final Player player;
	private final Queue<Message> messages;
	private PlayerSender anothersender;
	private boolean messageput;

	/**
	 * Construct a player sender object
	 * @param player The receiving thread
	 */
	public PlayerSender(Player player) {
		super("PlayerSender");
		this.player = player;
		this.messages = new ConcurrentLinkedDeque<Message>();
		this.anothersender = null;
		this.messageput = true;
	}

	/**
	 * The statements run in this thread
	 */
	public void run() {
		try {
			while (true) {
				Message msg = this.messages.poll();
				
				//no more message to send
				if (msg == null) {
					synchronized(this){
						// wait for message
						this.messageput = false;
						while(!this.messageput){
							this.wait();
						}
					}
				}else{
					//has a message to send
					if(this.anothersender != null && !CriticalCheck.isCritical(msg) && !this.anothersender.isInterrupted()){
						this.anothersender.put(msg);
					}else{
						this.player.getControl().sendMessage(msg);
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			// exception
		}
	}

	/**
	 * Configure another player sender
	 * @param sender The other player sender
	 */
	void setAnotherSender(PlayerSender sender){
		this.anothersender = sender;
	}
	
	/**
	 * Add a message to the queue to be sent to the player
	 */
	@Override
	public void put(Message msg) {
		// put message and wake this thread up
		this.messages.offer(msg);
		synchronized (this) {
			this.messageput = true;
			this.notifyAll();
		}
	}
}
