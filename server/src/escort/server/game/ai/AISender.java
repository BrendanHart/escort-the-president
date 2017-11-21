package escort.server.game.ai;

import escort.common.network.Message;
import escort.common.network.Sender;
import escort.server.game.GameMessageQueuer;

/**
 * Implemented of Sender used by an AI to send messages to the game message
 * queuer.
 * 
 * @author Ahmed Bhallo
 *
 */
public class AISender implements Sender {

	private GameMessageQueuer queuer;

	/**
	 * Instantiates a new AISender object
	 * 
	 * @param queuer
	 *            The game message queuer
	 */
	public AISender(GameMessageQueuer queuer) {
		this.queuer = queuer;
	}

	/**
	 * Puts the message directly into the queue
	 */
	@Override
	public void put(Message msg) {
		queuer.add(msg);
	}
}