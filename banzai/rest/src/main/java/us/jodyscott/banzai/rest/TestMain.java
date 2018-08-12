package us.jodyscott.banzai.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.SimpleServer;

public class TestMain {

	private static final Logger LOG = LoggerFactory.getLogger(TestMain.class);

	public static final String ENTITY_DELIMITER = ",";

	public final static int DEFAULT_HTTP_PORT = 8080;

	public final static String ENV_HOSTNAME = "HOSTNAME";
	public final static String ENV_NAME = "NAME";
	public final static String ENV_PASS = "PASS";
	public final static String ENV_DB_PORT = "DB_PORT";
	public final static String ENV_HTTP_PORT = "HTTP_PORT";
	public final static String ENV_PEERS = "PEERS";
	public final static String ENV_QUORUM = "QUORUM";

	public static void main(String[] args) {
		new TestMain().run(args);
	}

	private SimpleServer server;

	private void run(String[] args) {

		LOG.info("Running test");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (server != null) {
					server.shutdown();
				}
			}
		});

		CloudConfig cloudConfig = CloudConfig.newInstance();

		cloudConfig.setHostname("testclient");

		cloudConfig.setName("dev_test");

		cloudConfig.setSecret("pass_test");

		cloudConfig.getPeers().add(IpAddrPort.valueOf("127.0.0.1:9001"));
		cloudConfig.getPeers().add(IpAddrPort.valueOf("127.0.0.1:9002"));
		cloudConfig.getPeers().add(IpAddrPort.valueOf("127.0.0.1:9003"));
		cloudConfig.setQuorumSize(3);

		// We are a client only
		cloudConfig.getCloudRoles().add(CloudRoles.CLIENT);

		this.server = RestServer.newInstance(DEFAULT_HTTP_PORT, cloudConfig);

	}

}
