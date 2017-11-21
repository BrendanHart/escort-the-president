package escort.common.network.udp;

import java.io.*;

import escort.common.network.*;

/**
 * Stamp the message, for use in UDP, for necessary future protocol handling
 * @author Kwong Hei Tsang
 *
 */
public class MessageStamp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8608740631361843230L;
	public final long seq;
	public final long time;
	public final Message msg;
	public final boolean ack;
	public final long ackseq;
	
	/**
	 * Create a message stamp for the message
	 * @param seq the sequence of this message
	 * @param time The time of this message being sent
	 * @param msg The message
	 * @param ack Whether this is an acknowledgement message
	 * @param ackseq Which message are you replying to
	 */
	public MessageStamp(long seq,long time,Message msg,boolean ack,long ackseq){
		this.seq = seq;
		this.time = time;
		this.msg = msg;
		this.ack = ack;
		this.ackseq = ackseq;
	}
}
