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

import us.jodyscott.clouddb.core.CloudConfig;
import us.jodyscott.clouddb.core.CloudRoles;
import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.SerialSet;
import us.jodyscott.clouddb.core.util.CommonTools;

public class CloudConfigImpl implements CloudConfig, IdentifiedDataSerializable {

	// ======================================================================================================

	private static int getClazzId() {
		return Constants.CLOUD_DB_CONFIG;
	}

	// ======================================================================================================

	@JsonProperty("name") private String name;
	@JsonProperty("secret") private String secret;
	@JsonProperty("hostname") private String hostname;
	@JsonProperty("roleTypes") private Set<CloudRoles> roleTypes = SerialSet.newHashInstance();
	@JsonProperty("peers") private Set<IpAddrPort> peers = SerialSet.newHashInstance();
	@JsonProperty("dbPort") private Integer dbPort;
	@JsonProperty("quorumSize") private Integer quorumSize;

	@JsonIgnore private boolean immutable;

	public CloudConfigImpl() {

	}

	public CloudConfigImpl(CloudConfig cloudConfig) {

		CommonTools.nullCheck("cloudDbConfig", cloudConfig);

		if (cloudConfig.hasName()) {
			setName(cloudConfig.getName());
		}

		if (cloudConfig.hasSecret()) {
			setSecret(cloudConfig.getSecret());
		}

		if (cloudConfig.hasHostname()) {
			setHostname(cloudConfig.getHostname());
		}

		this.roleTypes.addAll(cloudConfig.getCloudRoles());
		this.peers.addAll(cloudConfig.getPeers());

		if (cloudConfig.hasDbPort()) {
			setDbPort(cloudConfig.getDbPort());
		}

		if (cloudConfig.hasQuorumSize()) {
			setQuorumSize(cloudConfig.getQuorumSize());
		}

	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String getName() {
		CommonTools.nullCheck("name", this.name);
		return this.name;
	}

	@JsonGetter("name")
	@JsonIgnore
	public String _getName() {
		return this.name;
	}

	@Override
	@JsonSetter("name")
	public CloudConfigImpl setName(String dbName) {
		immutableCheck();
		this.name = dbName;
		return this;
	}

	@Override
	@JsonGetter("hasName")
	public boolean hasName() {
		return this.name != null;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String getSecret() {
		CommonTools.nullCheck("secret", this.secret);
		return this.secret;
	}

	@JsonGetter("secret")
	public String _getSecret() {
		return this.secret;
	}

	@Override
	@JsonSetter("secret")
	public CloudConfigImpl setSecret(String dbPassword) {
		immutableCheck();
		this.secret = dbPassword;
		return this;
	}

	@Override
	@JsonGetter("hasSecret")
	public boolean hasSecret() {
		return this.secret != null;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String getHostname() {
		CommonTools.nullCheck("hostname", this.hostname);
		return this.hostname;
	}

	@JsonGetter("hostname")
	public String _getHostname() {
		return this.hostname;
	}

	@Override
	@JsonSetter("hostname")
	public CloudConfig setHostname(String hostname) {
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
	@JsonGetter("roleTypes")
	public Set<CloudRoles> getCloudRoles() {
		return roleTypes;
	}

	@JsonSetter("roleTypes")
	public CloudConfig _setRoleTypes(Set<CloudRoles> roleTypes) {
		immutableCheck();
		this.roleTypes.addAll(roleTypes);
		return this;
	}

	// ======================================================================================================

	@Override
	@JsonGetter("peers")
	public Set<IpAddrPort> getPeers() {
		return peers;
	}

	@JsonSetter("peers")
	public CloudConfig _setPeers(Set<IpAddrPort> peers) {
		immutableCheck();
		this.peers.addAll(peers);
		return this;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public Integer getDbPort() {
		CommonTools.nullCheck("dbPort", this.dbPort);
		return this.dbPort;
	}

	@JsonGetter("dbPort")
	public Integer _getDbPort() {
		return this.dbPort;
	}

	@Override
	@JsonSetter("dbPort")
	public CloudConfigImpl setDbPort(Integer dbPort) {
		immutableCheck();
		CommonTools.portCheck(dbPort);
		this.dbPort = dbPort;
		return this;
	}

	@Override
	@JsonGetter("hasDbPort")
	public boolean hasDbPort() {
		return this.dbPort != null;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public int getQuorumSize() {
		CommonTools.nullCheck("quorumSize", this.quorumSize);
		return this.quorumSize;
	}

	@JsonGetter("quorumSize")
	public Integer _getInitialQuorum() {
		return this.quorumSize;
	}

	@Override
	@JsonSetter("quorumSize")
	public CloudConfigImpl setQuorumSize(Integer quorumSize) {
		immutableCheck();

		if (quorumSize == null) {
			this.quorumSize = null;
			return this;
		} else {
			this.quorumSize = quorumSize;
		}
		return this;
	}

	@Override
	@JsonGetter("hasQuorum")
	public boolean hasQuorumSize() {
		return this.quorumSize != null;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public void readData(ObjectDataInput in) throws IOException {
		immutableCheck();
		this.name = in.readUTF();
		this.secret = in.readUTF();
		this.roleTypes = in.readObject();
		this.peers = in.readObject();
		this.dbPort = in.readObject();
		this.quorumSize = in.readObject();
		constraintCheck();
	}

	@Override
	@JsonIgnore
	public void writeData(ObjectDataOutput out) throws IOException {
		constraintCheck();
		this.immutable = true;
		out.writeUTF(this.name);
		out.writeUTF(this.secret);
		out.writeObject(this.roleTypes);
		out.writeObject(this.peers);
		out.writeObject(this.dbPort);
		out.writeObject(this.quorumSize);
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
	}

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
		result = prime * result + ((dbPort == null) ? 0 : dbPort.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + (immutable ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((peers == null) ? 0 : peers.hashCode());
		result = prime * result + ((quorumSize == null) ? 0 : quorumSize.hashCode());
		result = prime * result + ((roleTypes == null) ? 0 : roleTypes.hashCode());
		result = prime * result + ((secret == null) ? 0 : secret.hashCode());
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
		CloudConfigImpl other = (CloudConfigImpl) obj;
		if (dbPort == null) {
			if (other.dbPort != null)
				return false;
		} else if (!dbPort.equals(other.dbPort))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (immutable != other.immutable)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (peers == null) {
			if (other.peers != null)
				return false;
		} else if (!peers.equals(other.peers))
			return false;
		if (quorumSize == null) {
			if (other.quorumSize != null)
				return false;
		} else if (!quorumSize.equals(other.quorumSize))
			return false;
		if (roleTypes == null) {
			if (other.roleTypes != null)
				return false;
		} else if (!roleTypes.equals(other.roleTypes))
			return false;
		if (secret == null) {
			if (other.secret != null)
				return false;
		} else if (!secret.equals(other.secret))
			return false;
		return true;
	}

	// ======================================================================================================

}
