package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.CloudControllerImpl;

public interface CloudController {

	public static CloudController newInstance(CloudConfig cloudConfig) {
		return new CloudControllerImpl(cloudConfig);
	}

	void setOnline();

	Entities entities();

	void shutdown();

	void tryQuorum() throws CloudException;

}
