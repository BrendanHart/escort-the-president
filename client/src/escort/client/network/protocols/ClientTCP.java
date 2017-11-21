package escort.client.network.protocols;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.utils.Colors;
import escort.common.network.tcp.MessageControlTCP;

/**
 * A TCP client implementation
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class ClientTCP {

	/**
	 * Connect to a specific server and port over TCP
	 * @param client
	 * @param server
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public static final MessageControlTCP getMessageControl(Client client, String server, int port) throws IOException {
		try {
			// Try the secure way
			return getMessageControlSecure(server, port);
		} catch (Exception e) {
			// Fails to verify the server, fall back to manual
		}

		try {
			return getMessageControlManual(client, server, port);
		} catch (IOException e) {
			// fails
			throw e;
		} catch (KeyManagementException | NoSuchAlgorithmException | InterruptedException e) {
			throw new IOException("Handsake failed");
		}
	}

	/**
	 * Get a message control object for TCP
	 * 
	 * @param server
	 *            The server hostname
	 * @param port
	 *            The server port
	 * @return MessageControl object for TCP
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	public static final MessageControlTCP getMessageControlSecure(String server, int port)
			throws IOException, KeyManagementException, NoSuchAlgorithmException {
		// Default socket factory
		SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLParameters p = new SSLParameters();
		p.setEndpointIdentificationAlgorithm("HTTPS");

		// connect to server and start SSL handshake
		SSLSocket s = (SSLSocket) f.createSocket();
		s.connect(new InetSocketAddress(server, port), 5000);
		s.setUseClientMode(true);
		s.setSSLParameters(p);
		s.startHandshake();

		MessageControlTCP control = new MessageControlTCP(s);
		return control;
	}

	private static boolean waitingForDialog = true;
	private static boolean certificateRejected = false;

	/**
	 * Get a message control object for TCP with manual verification
	 * 
	 * @param server
	 *            The server hostname
	 * @param port
	 *            The server port
	 * @return MessageControl object for TCP
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	public static final MessageControlTCP getMessageControlManual(Client client, String server, int port)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, InterruptedException {
		// Ignore certificate error
		SSLContext sc;
		sc = SSLContext.getInstance("TLS");
		sc.init(null, new TrustManager[] { new TrustAllX509TrustManager() }, new java.security.SecureRandom());
		SSLSocketFactory f = sc.getSocketFactory();

		// connect to server and start SSL handshake
		SSLSocket s = (SSLSocket) f.createSocket();
		s.connect(new InetSocketAddress(server, port), 2000);
		s.startHandshake();

		// Obtain certificate
		if (s.getSession().getPeerCertificates().length == 0) {
			throw new IOException("Failed to read server certificate");
		}
		Certificate cert = s.getSession().getPeerCertificates()[0];
		PublicKey publickey = cert.getPublicKey();

		if (client == null) {
			return new MessageControlTCP(s);
		}

		// Generate base64 encoded public key to be printed
		Encoder b64encode = Base64.getEncoder();
		String encodedkey = new String(b64encode.encode(publickey.getEncoded()));

		final Object obj = new Object();
		Inputs inputs = client.getInputHandler().getInputs();
		TextButton rejectButton = new TextButton("Cancel", inputs);
		rejectButton.addListener(e -> {
			waitingForDialog = false;
			certificateRejected = true;
			synchronized (obj) {
				obj.notifyAll();
			}
		});
		TextButton acceptButton = new TextButton("I trust this connection", inputs);
		acceptButton.addListener(e -> {
			waitingForDialog = false;
			certificateRejected = false;
			synchronized (obj) {
				obj.notifyAll();
			}
		});
		Panel buttons = new Panel(inputs, 240 * Scale.factor, rejectButton.getHeight());
		buttons.add(rejectButton, 0, 0);
		buttons.add(acceptButton, buttons.getWidth() - acceptButton.getWidth(), 0);

		Panel publicKeyText = TextUtils.wrappedTextLabel(inputs, encodedkey, 32, Colors.LIGHT_BLACK, false);
		ScrollableList publicKeyPanel = new ScrollableList(inputs, publicKeyText.getWidth() + 12 * Scale.factor,
				40 * Scale.factor);
		publicKeyPanel.setBackground(Colors.DARK_GRAY);
		publicKeyPanel.addEntry(publicKeyText);

		Panel container = new Panel(inputs, buttons.getWidth(),
				publicKeyPanel.getHeight() + buttons.getHeight() + 10 * Scale.factor);
		container.add(publicKeyPanel, container.center(publicKeyPanel).x, 5 * Scale.factor);
		container.add(buttons, 0, container.getHeight() - buttons.getHeight());

		DialogPanel leaveWarning = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"Unable to authenticate server's certificate", "Please verify manually:", container);
		client.setDialog(leaveWarning);

		waitingForDialog = true;
		certificateRejected = false;
		while (waitingForDialog) {
			synchronized (obj) {
				obj.wait();
			}
		}

		client.setDialog(null);

		if (certificateRejected) {
			s.close();
			throw new IOException("Client refused certificate.");
		}

		// Create the TCP message control
		MessageControlTCP control = new MessageControlTCP(s);
		return control;
	}

	/**
	 * A class to accept any certificate
	 * 
	 * @author Kwong Hei Tsang
	 *
	 */
	public static final class TrustAllX509TrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

	}
}
