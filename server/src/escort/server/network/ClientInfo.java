package escort.server.network;

import java.net.*;

/**
 * Represent client information
 * @author Kwong Hei Tsang
 *
 */
public class ClientInfo {

	public final InetAddress addr;
	public final int port;
	
	/**
	 * Construct a Client info
	 * @param addr
	 * @param port
	 */
	public ClientInfo(InetAddress addr, int port){
		this.addr = addr;
		this.port = port;
	}
	
	/**
	 * Compare whether the object is equals to thism client info
	 */
	@Override
	public boolean equals(Object info){
		if(info instanceof ClientInfo){
			ClientInfo info2 = (ClientInfo)info;
			return addr.equals(info2.addr) && port == info2.port;
		}else{
			return false;
		}
	}
	
	/**
	 * Calculate the hashcode of this object
	 */
	@Override
	public int hashCode(){
		return addr.hashCode() + port;
	}
	
}
