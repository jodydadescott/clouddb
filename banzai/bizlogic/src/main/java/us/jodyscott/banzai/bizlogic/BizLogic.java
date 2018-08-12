package us.jodyscott.banzai.bizlogic;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.util.FString;

public class BizLogic {

	private static final Logger LOG = LoggerFactory.getLogger(BizLogic.class);

	public static CloudController newCloudControllerInstance(CloudConfig cloudConfig) {

		assert cloudConfig != null;

		LOG.info("Running with config {}", cloudConfig);

		CloudController cloudController = CloudController.newInstance(cloudConfig);

		// Unique name trigger
		cloudController.entities().helloWorld().setPreTrigger((entityEvent) -> {

			assert entityEvent != null;

			// We only check if entity is a new install or has a name change

			HelloWorldDAO oldDao = entityEvent.getOldValue();
			HelloWorldDAO newDao = entityEvent.getValue();

			if (oldDao == null || oldDao.getName().equals(newDao.getName())) {
				try {
					try {
						entityEvent.getEntityHandler().lockMap();
					} catch (CloudException e) {
						throw e;
					}

					String sql = FString.format("name = {}", newDao.getName());
					Collection<HelloWorldDAO> results = entityEvent.getEntityHandler().search(sql);
					for (HelloWorldDAO existingDao : results) {
						if (existingDao.getKey().equals(newDao.getKey())) {
							continue;
						}
						throw CloudException.user(100, "Name {} is in use by key {}", newDao.getName(),
								existingDao.getKey());
					}

				} finally {
					if (entityEvent.getEntityHandler().isMapLocked()) {
						entityEvent.getEntityHandler().unlockMap();
					}
				}

			}
		});

		cloudController.setOnline();

		return cloudController;
	}

}
