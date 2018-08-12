package us.jodyscott.clouddb.core;

import java.util.Set;

public interface CloudMember {

	String getUuid();

	CloudMember setUuid(String uuid);

	Set<CloudRoles> getCloudRoles();

	String getHostname();

	CloudMember setHostname(String hostname);

	boolean hasHostname();

	Integer getPort();

	CloudMember setPort(Integer port);

	boolean hasPort();

}
