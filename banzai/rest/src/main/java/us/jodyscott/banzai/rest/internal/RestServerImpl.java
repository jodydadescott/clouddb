package us.jodyscott.banzai.rest.internal;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.banzai.bizlogic.BizLogic;
import us.jodyscott.banzai.rest.RestServer;
import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudController;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.PersonDAO;

public class RestServerImpl implements RestServer {

	private static final Logger LOG = LoggerFactory.getLogger(RestServerImpl.class);

	private final CloudController cloudController;
	private final Server server;
	private final ServletHandler handler;

	private volatile boolean isFinal;

	public RestServerImpl(Integer port, CloudConfig cloudConfig) {

		try {

			LOG.trace("enter (port={}, cloudConfig={})", port, cloudConfig);

			assert port != null;
			assert cloudConfig != null;

			this.cloudController = BizLogic.newCloudControllerInstance(cloudConfig);

			this.server = new Server(port);
			this.handler = new ServletHandler();
			this.server.setHandler(handler);

			ServletHolder helloworldHolder = new ServletHolder();
			helloworldHolder
					.setServlet(new RestServlet<>(cloudController.entities().helloWorld(), HelloWorldDAO.class));
			handler.addServletWithMapping(helloworldHolder, "/helloworld/*");

			ServletHolder personHolder = new ServletHolder();
			personHolder.setServlet(new RestServlet<>(cloudController.entities().person(), PersonDAO.class));
			handler.addServletWithMapping(personHolder, "/person/*");

			try {
				LOG.trace("enter void start()");

				try {
					this.server.start();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} finally {
				LOG.trace("exit void start()");
			}

		} finally {
			LOG.trace("exit (port={}, cloudConfig={})", port, cloudConfig);
		}

	}

	protected <T> void register(Entity<T> entityHandler, Class<T> clazz, String uri) {
		ServletHolder externalHolder = new ServletHolder();
		externalHolder.setServlet(new RestServlet<>(entityHandler, clazz));
		handler.addServletWithMapping(externalHolder, "/" + uri + "/*");
	}

	@Override
	public synchronized void shutdown() {
		try {
			LOG.trace("enter void shutdown()");

			if (isFinal) {
				LOG.warn("Server is not running (final)");
				return;
			}

			this.isFinal = true;

			try {
				LOG.debug("Stopping Jetty");
				this.server.stop();

				LOG.debug("Stopping CloudController");
				this.cloudController.shutdown();
			} catch (Exception e) {
				LOG.error("Jetty threw an exception while shutting down", e);
			}
		} finally {
			LOG.trace("exit void shutdown()");
		}
		LOG.trace("enter shutdown()");
	}
}
