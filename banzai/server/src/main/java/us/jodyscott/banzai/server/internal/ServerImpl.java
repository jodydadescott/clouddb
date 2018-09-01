package us.jodyscott.banzai.server.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.banzai.bizlogic.BizLogic;
import us.jodyscott.banzai.server.Server;
import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;

public class ServerImpl implements Server {

	private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);

	private final CloudController cloudController;

	private volatile boolean isFinal;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public ServerImpl(CloudConfig cloudConfig) {

		try {
			LOG.trace("enter (cloudConfig={})", cloudConfig);

			assert cloudConfig != null;

			this.cloudController = BizLogic.newCloudControllerInstance(cloudConfig);

		} finally {
			LOG.trace("exit (cloudConfig={})", cloudConfig);
		}

	}

	@Override
	public synchronized void shutdown() {
		try {
			LOG.trace("enter void shutdown()");

			if (isFinal) {
				LOG.warn("Not running, instance is final");
			} else {
				isFinal = true;
				LOG.debug("Stopping CloudController");
				this.cloudController.shutdown();

			}
		} finally {
			LOG.trace("exit void shutdown()");
		}
		LOG.trace("enter shutdown()");
	}
}
