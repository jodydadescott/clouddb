package us.jodyscott.clouddb.core;

import java.util.Collection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.Entities;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.PersonDAO;
import us.jodyscott.clouddb.core.util.FString;

public class SingleNodeTest {

	static {
		System.setProperty("logback.configurationFile", "src/main/resources/dev_logback.xml");
	}

	private static final Logger LOG = LoggerFactory.getLogger(SingleNodeTest.class);

	private CloudConfig config;

	private CloudController controller;
	private Entities entities;

	{

		this.config = CloudConfig.newInstance().setName("dev_username").setSecret("dev_pass").setDbPort(9000)
				.setQuorumSize(1);

		this.config.getCloudRoles().add(CloudRoles.SERVER);
		this.config.getCloudRoles().add(CloudRoles.QUORUM);

		this.controller = CloudController.newInstance(config);
		this.entities = controller.entities();

		this.controller.entities().helloWorld().setPreTrigger((entityEvent) -> {

			assert entityEvent != null;

			// We only check if entity is a new install or has a name change

			HelloWorldDAO oldDao = entityEvent.getOldValue();
			HelloWorldDAO newDao = entityEvent.getValue();

			if (oldDao == null || oldDao.getName().equals(newDao.getName())) {
				try {
					entityEvent.getEntityHandler().lockMap();
					String sql = FString.format("name = {}", newDao.getName());

					// TODO : Entity Event needs to be able to get to root of
					// structure.

					Collection<HelloWorldDAO> results = entityEvent.getEntityHandler().search(sql);
					for (HelloWorldDAO existingDao : results) {
						if (existingDao.getKey().equals(newDao.getKey())) {
							continue;
						}
						throw CloudException.user(100, "Name {} is in use by entity {}", existingDao);
					}

				} finally {
					if (entityEvent.getEntityHandler().isMapLocked()) {
						entityEvent.getEntityHandler().unlockMap();
					}
				}

			}
		});

		this.controller.setOnline();
	}

	private static HelloWorldDAO newHelloWorldDao(String name) {
		HelloWorldDAO dao = HelloWorldDAO.newInstance();
		dao.setName(name);
		return dao;
	}

	private HelloWorldDAO getByKeyNoException(String key) throws CloudException {
		return entities.helloWorld().get(key);
	}

	private void sleep(int seconds) {
		try {
			LOG.info("sleeping for {} seconds", seconds);
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
	}

	private void shutdown() {
		LOG.info("shutting down");
		controller.shutdown();
	}

	private void putAndExpectResourceConflictException(HelloWorldDAO dao) throws CloudException {
		try {
			entities.helloWorld().create(dao);
			assert false : "Exception should have been thrown";
		} catch (CloudException e) {

		}
	}

	@Test
	public void test() throws CloudException {

		String name = "fox1";

		HelloWorldDAO fox1 = newHelloWorldDao(name);
		fox1 = entities.helloWorld().create(fox1);

		getByKeyNoException(fox1.getKey());

		assert entities.helloWorld().values().size() > 0;

		String sql = FString.format("name = {}", name);
		assert entities.helloWorld().search(sql).size() > 0;

		HelloWorldDAO fox2 = newHelloWorldDao("fox2");
		fox2 = entities.helloWorld().create(fox2);

		HelloWorldDAO falseFox2 = newHelloWorldDao("fox2");
		putAndExpectResourceConflictException(falseFox2);

		PersonDAO person = PersonDAO.newInstance();
		person.setLastName("Scott");
		person.setFirstName("Jody");
		entities.person().create(person);

		sleep(2);

		shutdown();

	}

}
