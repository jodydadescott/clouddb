package us.jodyscott.clouddb.core;

import java.util.Set;

import us.jodyscott.clouddb.core.internal.CloudConfigImpl;

public interface CloudConfig {

	// ======================================================================================================

	public static CloudConfig newInstance() {
		return new CloudConfigImpl();
	}

	public static CloudConfig newInstance(CloudConfig cloudDbConfig) {
		return new CloudConfigImpl(cloudDbConfig);
	}

	// ======================================================================================================

	String getName();

	CloudConfig setName(String username);

	boolean hasName();

	// ======================================================================================================

	String getSecret();

	CloudConfig setSecret(String password);

	boolean hasSecret();

	// ======================================================================================================

	Set<CloudRoles> getCloudRoles();

	// ======================================================================================================

	Set<IpAddrPort> getPeers();

	// ======================================================================================================

	Integer getDbPort();

	CloudConfig setDbPort(Integer port);

	boolean hasDbPort();

	// ======================================================================================================

	int getQuorumSize();

	CloudConfig setQuorumSize(Integer quorum);

	boolean hasQuorumSize();

	// ======================================================================================================

	String getHostname();

	CloudConfig setHostname(String hostname);

	boolean hasHostname();

	// ======================================================================================================

}
