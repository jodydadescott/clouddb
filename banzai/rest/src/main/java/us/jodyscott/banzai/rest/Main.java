package us.jodyscott.banzai.rest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.SimpleServer;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String ENTITY_DELIMITER = ",";

	public final static int DEFAULT_HTTP_PORT = 8080;

	public final static String ENV_HOSTNAME = "HOSTNAME";
	public final static String ENV_NAME = "NAME";
	public final static String ENV_PASS = "PASS";
	public final static String ENV_DB_PORT = "DB_PORT";
	public final static String ENV_HTTP_PORT = "HTTP_PORT";
	public final static String ENV_PEERS = "PEERS";
	public final static String ENV_QUORUM = "QUORUM";
	public final static String PID_FILE = "PID_FILE";

	public static void main(String[] args) throws IOException {
		new Main().run(args);
	}

	private SimpleServer server;
	private String pidFile;

	private void run(String[] args) throws FileNotFoundException, IOException {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				LOG.trace("Caught signal");

				if (server == null) {
					LOG.warn("Server is not running");
				} else {
					LOG.info("Shutting down server gracefully");
					server.shutdown();
				}

				if (pidFile == null) {
					LOG.trace("pidFile is not set");
				} else {
					LOG.trace("Removing pid file {}", pidFile);

					File file = new File(pidFile);

					if (file.delete()) {
						LOG.trace("PidFile {} removed", pidFile);
					} else {
						LOG.trace("Unable to remove PidFile {}", pidFile);
					}

				}

			}
		});

		CloudConfig cloudConfig = CloudConfig.newInstance();

		Map<String, String> map = System.getenv();

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String jvmName = bean.getName();
		long pid = Long.valueOf(jvmName.split("@")[0]);

		LOG.info("Process ID is {}", pid);

		if (map.containsKey(PID_FILE)) {
			this.pidFile = map.get(PID_FILE);
			LOG.trace("Writing pid {} to file {}", pid, pidFile);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pidFile), "utf-8"))) {
				writer.write(String.valueOf(pid));

			} catch (UnsupportedEncodingException e) {
				throw new AssertionError("We should not be here");
			}

		}

		if (map.containsKey(ENV_HOSTNAME)) {
			cloudConfig.setHostname(map.get(ENV_HOSTNAME));
		}

		if (map.containsKey(ENV_NAME)) {
			cloudConfig.setName(map.get(ENV_NAME));
		}

		if (map.containsKey(ENV_PASS)) {
			cloudConfig.setSecret(map.get(ENV_PASS));
		}

		if (map.containsKey(ENV_DB_PORT)) {
			cloudConfig.setDbPort(Integer.valueOf(map.get(ENV_DB_PORT)));
		}

		if (map.containsKey(ENV_PEERS)) {
			String peers = map.get(ENV_PEERS).replaceAll("\\s+", "");
			if (peers.equals("")) {
				LOG.warn("{} is set but empty", ENV_PEERS);
			} else {
				for (String entity : map.get(ENV_PEERS).split(ENTITY_DELIMITER)) {
					cloudConfig.getPeers().add(IpAddrPort.valueOf(entity));
				}
			}
		}

		if (map.containsKey(ENV_QUORUM)) {
			cloudConfig.setQuorumSize(Integer.valueOf(map.get(ENV_QUORUM)));
		}

		// We are a client only
		cloudConfig.getCloudRoles().add(CloudRoles.CLIENT);

		int httpPort = DEFAULT_HTTP_PORT;
		if (map.containsKey(ENV_HTTP_PORT)) {
			httpPort = Integer.valueOf(map.get(ENV_HTTP_PORT));
		}

		this.server = RestServer.newInstance(httpPort, cloudConfig);

	}

}
