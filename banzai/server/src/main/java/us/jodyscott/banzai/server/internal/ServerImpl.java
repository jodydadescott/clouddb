package us.jodyscott.banzai.server.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.banzai.bizlogic.BizLogic;
import us.jodyscott.banzai.server.Server;
import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;

public class ServerImpl implements Server {

	private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);

	private final CloudController cloudController;

	private Object lockObject = new Object();

	private volatile boolean running;

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

			synchronized (lockObject) {
				if (running) {
					try {
						LOG.debug("Stopping CloudController");
						this.cloudController.shutdown();
					} catch (Exception e) {
						LOG.error("Jetty threw an exception while shutting down", e);
					}
				} else {
					LOG.warn("Not running");
				}
			}
		} finally {
			LOG.trace("exit void shutdown()");
		}
		LOG.trace("enter shutdown()");
	}
}
