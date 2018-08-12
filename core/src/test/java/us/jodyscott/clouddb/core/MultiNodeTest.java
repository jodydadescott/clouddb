package us.jodyscott.clouddb.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.Trigger;
import us.jodyscott.clouddb.core.util.FString;

public class MultiNodeTest {

	private static final Logger LOG = LoggerFactory.getLogger(MultiNodeTest.class);

	private CloudController controller1;
	private CloudController controller2;

	private Entity<HelloWorldDAO> handle1;
	@SuppressWarnings("unused") private Entity<HelloWorldDAO> handle2;

	{

		IpAddrPort port1 = IpAddrPort.valueOf("127.0.0.1:9001");
		IpAddrPort port2 = IpAddrPort.valueOf("127.0.0.1:9002");
		Set<IpAddrPort> ports = new HashSet<>();
		ports.add(port1);
		ports.add(port2);

		CloudConfig commonConfig = CloudConfig.newInstance().setName("dev_username").setSecret("dev_pass")
				.setQuorumSize(3);
		commonConfig.getPeers().addAll(ports);
		commonConfig.getCloudRoles().add(CloudRoles.SERVER);
		commonConfig.getCloudRoles().add(CloudRoles.QUORUM);

		CloudConfig config1 = CloudConfig.newInstance(commonConfig).setDbPort(port1.getPort());
		CloudConfig config2 = CloudConfig.newInstance(commonConfig).setDbPort(port2.getPort());

		LOG.info("Config1->{}", config1);
		LOG.info("Config2->{}", config2);

		this.controller1 = CloudController.newInstance(config1);
		this.controller2 = CloudController.newInstance(config2);

		Trigger<HelloWorldDAO> trigger = (entityEvent) -> {

			assert entityEvent != null;

			// We only check if entity is a new install or has a name change

			HelloWorldDAO oldDao = entityEvent.getOldValue();
			HelloWorldDAO newDao = entityEvent.getValue();

			if (oldDao == null || oldDao.getName().equals(newDao.getName())) {
				try {
					entityEvent.getEntityHandler().lockMap();
					String sql = FString.format("name = {}", newDao.getName());
					Collection<HelloWorldDAO> results = entityEvent.getEntityHandler().search(sql);
					for (HelloWorldDAO existingDao : results) {
						if (existingDao.getKey().equals(newDao.getKey())) {
							continue;
						}
						throw CloudException.user(100, "Name {} is in use by key {}", existingDao.getName(),
								existingDao.getKey());
					}

				} finally {
					if (entityEvent.getEntityHandler().isMapLocked()) {
						entityEvent.getEntityHandler().unlockMap();
					}
				}

			}
		};

		this.controller1.entities().helloWorld().setPreTrigger(trigger);
		this.controller2.entities().helloWorld().setPreTrigger(trigger);

		this.controller1.setOnline();
		this.controller2.setOnline();

		this.handle1 = controller1.entities().helloWorld();
		this.handle2 = controller2.entities().helloWorld();

	}

	private static HelloWorldDAO newHelloWorldDao(String name) {
		HelloWorldDAO dao = HelloWorldDAO.newInstance();
		dao.setName(name);
		return dao;
	}

	@Test
	public void test() throws CloudException {

		sleep(2);

		LOG.info("Starting test");
		LOG.info("Putting object expecting no Exception");
		put("fox1");

		sleep(2);

		LOG.info("Shutting down node2");
		controller2.shutdown();

		sleep(2);

		LOG.info("Putting object expecting Exception");
		// put("fox2");

		LOG.info("Shutting down node1");
		controller1.shutdown();
	}

	private void put(String name) throws CloudException {
		HelloWorldDAO fox1 = newHelloWorldDao(name);
		fox1 = handle1.create(fox1);
	}

	private static void sleep(int s) {
		LOG.info("Pausing for " + s + " seconds");
		TestTools.sleep(s);
	}

}
