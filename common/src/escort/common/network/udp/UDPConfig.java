package escort.common.network.udp;

/**
 * Configure the UDP communication protocol
 * @author Kwong Hei Tsang
 *
 */
public class UDPConfig {

	//public static final int UDP_CLIENT_RECEIVE_MAXTIME = 2000;
	public static final long UDP_KEEPALIVE = 3000;
	//public static final int UDP_SERVER_RECEIVE_MAXTIME = 1000;
	public static final int UDP_BUFFER_SIZE = 5000;
	public static final long UDP_MAX_INACTIVITY = 10000;
}
