package escort.client.network.protocols;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import escort.common.network.udp.MessageControlUDP;
import escort.common.network.udp.UDPConfig;

/**
 * A UDP client implementation
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class ClientUDP {
	
	/**
	 * Get a UDP Message Control
	 * 
	 * @param server
	 * @param port
	 * @param refcert
	 *            The reference certificate to compare, null for obtain from TCP
	 *            server
	 * @return
	 * @throws CertificateException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	static final MessageControlUDP getMessageControl(String server, int port, Certificate refcert)
			throws CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
		// Initialize socket and set receive timeout to 2 seconds
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(server);
		socket.connect(address, port);
		socket.setSoTimeout(2000);

		// Create certificate factory
		CertificateFactory factory = CertificateFactory.getInstance("X.509");

		// Request server certificate
		byte[] request = new byte[3];
		Arrays.fill(request, (byte) 0);
		DatagramPacket reqpacket = new DatagramPacket(request, request.length);
		socket.send(reqpacket);
		// Obtain server certificate and recreate a new socket
		byte[] response = new byte[UDPConfig.UDP_BUFFER_SIZE];
		DatagramPacket respacket = new DatagramPacket(response, response.length);
		socket.receive(respacket);
		byte[] certbyte = new byte[respacket.getLength()];
		System.arraycopy(response, 0, certbyte, 0, certbyte.length);
		Certificate cert = factory.generateCertificate(new BufferedInputStream(new ByteArrayInputStream(certbyte)));
		PublicKey publickey = cert.getPublicKey();

		// Validate the certificate of the server
		boolean valid = true;
		if (refcert != null) {
			// Verify public key against supplied certificate, abort if fails
			valid = publickey.equals(refcert.getPublicKey());
			if (!valid) {
				socket.close();
				throw new IOException("Verification fails");
			}
		}else{
			socket.close();
			throw new IOException("Certificate not found.");
		}

		// Generate AES key
		SecureRandom rand = new SecureRandom();
		byte[] aeskey = new byte[16];
		rand.nextBytes(aeskey);

		// Send the encrypted AES key to the server
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publickey);
		byte[] aescipher = cipher.doFinal(aeskey);
		socket.send(new DatagramPacket(aescipher, aescipher.length, address, port));

		// Wait for server response
		byte[] ackbytes = new byte[UDPConfig.UDP_BUFFER_SIZE];
		DatagramPacket ack = new DatagramPacket(ackbytes, ackbytes.length);
		socket.receive(ack);
		
		// Set receive timeout to indefinite and create message control
		socket.setSoTimeout(0);
		MessageControlUDP control = new MessageControlUDP(socket, address, port, true, aeskey);
		
		return control;
	}

}
