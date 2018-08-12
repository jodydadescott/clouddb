package us.jodyscott.banzai.rest;

import us.jodyscott.banzai.rest.internal.RestServerImpl;
import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.SimpleServer;

public interface RestServer extends SimpleServer {

	public static RestServer newInstance(Integer port, CloudConfig cloudConfig) {
		return new RestServerImpl(port, cloudConfig);
	}

}
