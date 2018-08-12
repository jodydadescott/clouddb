package us.jodyscott.clouddb.core.internal;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.IpAddrPort;
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.util.CommonTools;
import us.jodyscott.clouddb.core.util.IpTools;

public class IpAddrPortImpl implements IpAddrPort, IdentifiedDataSerializable {

	// ============================================================================================

	private static int getClazzId() {
		return Constants.IP_ADDR_PORT;
	}

	// ============================================================================================

	@JsonProperty("ipAddr") private Integer ipAddr;
	@JsonProperty("port") private Integer port;

	@JsonIgnore private boolean immutable;

	@Override
	@JsonIgnore
	public int getIpAddr() {
		CommonTools.nullCheck("ipAddr", this.ipAddr);
		return this.ipAddr;
	}

	@JsonGetter("ipAddr")
	public String _getIpAddr() {
		return IpTools.ipDecimalNotationToString(ipAddr);
	}

	@Override
	@JsonIgnore
	public IpAddrPort setIpAddr(Integer ipAddr) {
		immutableCheck();
		this.ipAddr = ipAddr;
		return this;
	}

	@JsonSetter("ipAddr")
	public void _setIpAddr(String ipAddr) {
		immutableCheck();
		this.ipAddr = IpTools.ipDecimalNotationToInt(ipAddr);
	}

	@Override
	@JsonGetter("hasIpAddr")
	public boolean hasIpAddr() {
		return this.ipAddr != null;
	}

	@Override
	@JsonIgnore
	public Integer getPort() {
		CommonTools.nullCheck("port", this.port);
		return this.port;
	}

	@JsonGetter("port")
	public Integer _getPort() {
		return this.port;
	}

	@Override
	@JsonSetter("port")
	public IpAddrPort setPort(Integer port) {
		immutableCheck();
		CommonTools.portCheck(port);
		this.port = port;
		return this;
	}

	@Override
	@JsonGetter("hasPort")
	public boolean hasPort() {
		return this.port != null;
	}

	// ============================================================================================

	@JsonIgnore
	private void constraintCheck() {
	}

	@JsonIgnore
	private void immutableCheck() {
		CommonTools.immutableCheck(immutable);
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public void readData(ObjectDataInput in) throws IOException {
		immutableCheck();
		this.ipAddr = in.readObject();
		this.port = in.readObject();
		constraintCheck();
	}

	@Override
	@JsonIgnore
	public void writeData(ObjectDataOutput out) throws IOException {
		constraintCheck();
		this.immutable = true;

		out.writeObject(this.ipAddr);
		out.writeObject(this.port);
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
		result = prime * result + (immutable ? 1231 : 1237);
		result = prime * result + ((ipAddr == null) ? 0 : ipAddr.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		IpAddrPortImpl other = (IpAddrPortImpl) obj;
		if (immutable != other.immutable)
			return false;
		if (ipAddr == null) {
			if (other.ipAddr != null)
				return false;
		} else if (!ipAddr.equals(other.ipAddr))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	// ============================================================================================

}
