package us.jodyscott.clouddb.core.internal;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import us.jodyscott.clouddb.core.CloudError;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.CloudMember;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.SerialSet;
import us.jodyscott.clouddb.core.util.CommonTools;

public class CloudMemberImpl implements IdentifiedDataSerializable, CloudMember {

	// ======================================================================================================

	private static int getClazzId() {
		return Constants.CLOUD_MEMBER;
	}

	// ======================================================================================================

	@JsonProperty("uuid") private String uuid;
	@JsonProperty("hostname") private String hostname;
	@JsonProperty("cloudRoles") private Set<CloudRoles> cloudRoles = SerialSet.newHashInstance();
	@JsonProperty("port") private Integer port;

	private boolean immutable;

	// ======================================================================================================
	// UUID is required

	@Override
	@JsonGetter("uuid")
	public String getUuid() {
		return this.uuid;
	}

	@Override
	@JsonSetter("uuid")
	public CloudMember setUuid(String uuid) {
		CommonTools.immutableCheck(immutable);
		this.uuid = uuid;
		return this;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String getHostname() {
		CommonTools.nullCheck("hostname", hostname);
		return this.hostname;
	}

	@JsonGetter("hostname")
	public String _getHostname() {
		return this.hostname;
	}

	@Override
	@JsonSetter("hostname")
	public CloudMember setHostname(String hostname) {
		CommonTools.immutableCheck(immutable);
		this.hostname = hostname;
		return this;
	}

	@Override
	@JsonGetter("hasHostname")
	public boolean hasHostname() {
		return this.hostname != null;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public Integer getPort() {
		CommonTools.nullCheck("port", port);
		return this.port;
	}

	@JsonGetter("port")
	public Integer _getPort() {
		return this.port;
	}

	@Override
	@JsonSetter("port")
	public CloudMember setPort(Integer port) {
		this.port = port;
		return this;
	}

	@Override
	@JsonIgnore
	public boolean hasPort() {
		return this.port != null;
	}

	// ======================================================================================================

	@Override
	@JsonGetter("cloudRoles")
	public Set<CloudRoles> getCloudRoles() {
		return this.cloudRoles;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public synchronized void readData(ObjectDataInput in) throws IOException {
		immutableCheck();
		this.uuid = in.readUTF();
		this.hostname = in.readUTF();
		this.port = in.readObject();
		this.cloudRoles = in.readObject();
		constraintCheck();
	}

	@Override
	@JsonIgnore
	public void writeData(ObjectDataOutput out) throws IOException {
		constraintCheck();
		this.immutable = true;
		out.writeUTF(this.uuid);
		out.writeUTF(this.hostname);
		out.writeObject(port);
		out.writeObject(this.cloudRoles);
	}

	@Override
	@JsonIgnore
	public int getFactoryId() {
		return Constants.FACTORY_ID;
	}

	@Override
	@JsonIgnore
	public int getId() {
		return getClazzId();
	}

	// ======================================================================================================

	@JsonIgnore
	private void constraintCheck() {
		if (uuid == null) {
			throw CloudError.newInstance(CloudException.entityMalformed("uuid is null"));
		}
	}

	@JsonIgnore
	private void immutableCheck() {
		CommonTools.immutableCheck(immutable);
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + (immutable ? 1231 : 1237);
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((cloudRoles == null) ? 0 : cloudRoles.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	@JsonIgnore
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudMemberImpl other = (CloudMemberImpl) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (immutable != other.immutable)
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (cloudRoles == null) {
			if (other.cloudRoles != null)
				return false;
		} else if (!cloudRoles.equals(other.cloudRoles))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	// ======================================================================================================

}