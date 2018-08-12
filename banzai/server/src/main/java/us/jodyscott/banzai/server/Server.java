package us.jodyscott.banzai.server;

import us.jodyscott.banzai.server.internal.ServerImpl;
import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.SimpleServer;

public interface Server extends SimpleServer {

	public static Server newInstance(CloudConfig cloudConfig) {
		return new ServerImpl(cloudConfig);
	}

}
