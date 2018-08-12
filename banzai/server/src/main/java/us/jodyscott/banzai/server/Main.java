package us.jodyscott.banzai.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.SimpleServer;
import us.jodyscott.clouddb.core.util.RoleUtil;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String ENTITY_DELIMITER = ",";

	public final static String ENV_HOSTNAME = "HOSTNAME";
	public final static String ENV_NAME = "NAME";
	public final static String ENV_PASS = "PASS";
	public final static String ENV_DB_PORT = "DB_PORT";
	public final static String ENV_PEERS = "PEERS";
	public final static String ENV_QUORUM = "QUORUM";
	public final static String ENV_ROLES = "ROLES";

	public static void main(String[] args) {
		new Main().run(args);
	}

	private SimpleServer server;

	private void run(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (server != null) {
					server.shutdown();
				}
			}
		});

		CloudConfig cloudConfig = CloudConfig.newInstance();

		Map<String, String> map = System.getenv();

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

		if (map.containsKey(ENV_ROLES)) {
			RoleUtil roleUtil = new RoleUtil();
			for (String entity : map.get(ENV_ROLES).split(ENTITY_DELIMITER)) {
				roleUtil.addRole(entity);
			}
			cloudConfig.getCloudRoles().addAll(roleUtil.getRoles());
		}

		if (cloudConfig.getCloudRoles().contains(CloudRoles.CLIENT)) {
			LOG.warn("Role {} is not valid and will be removed", CloudRoles.CLIENT);
			cloudConfig.getCloudRoles().remove(CloudRoles.CLIENT);
		}

		// We are always a server (at the least)
		cloudConfig.getCloudRoles().add(CloudRoles.SERVER);

		this.server = Server.newInstance(cloudConfig);

	}

}
