package us.jodyscott.clouddb.core.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import us.jodyscott.clouddb.core.CloudError;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.util.CommonTools;

public class HelloWorldDAOImpl implements IdentifiedDataSerializable, HelloWorldDAO {

	// ============================================================================================

	private static int getClazzId() {
		return Constants.USER_HELLO_WORLD;
	}

	// ============================================================================================

	@JsonProperty("key") private String key;
	@JsonProperty("name") private String name;
	@JsonProperty("value") private String value;

	@JsonIgnore private boolean immutable;

	public HelloWorldDAOImpl() {

	}

	public HelloWorldDAOImpl(HelloWorldDAO dao) {
		CommonTools.nullCheck("dao", dao);

		this.key = dao.getKey();
		this.name = dao.getName();

		if (dao.hasValue()) {
			this.value = dao.getValue();
		}
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public boolean isImmutable() {
		return this.immutable;
	}

	@Override
	@JsonGetter("key")
	public String getKey() {
		return this.key;
	}

	@Override
	@JsonSetter("key")
	public void setKey(String key) {
		immutableCheck();
		this.key = key;
	}

	// ============================================================================================
	// Name can NOT be null

	@Override
	@JsonGetter("name")
	public String getName() {
		return this.name;
	}

	@Override
	@JsonSetter("name")
	public void setName(String name) {
		immutableCheck();
		this.name = name;
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public String getValue() {
		CommonTools.nullCheck("value", this.value);
		return this.value;
	}

	@JsonGetter("value")
	public String _getValue() {
		return this.value;
	}

	@Override
	@JsonSetter("value")
	public void setValue(String value) {
		immutableCheck();
		this.value = value;
	}

	@Override
	@JsonGetter("hasValue")
	public boolean hasValue() {
		return this.value != null;
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public void setTtl(int ttl) {
		immutableCheck();
		throw new AssertionError("Not supported in this implementation");
	}

	@Override
	@JsonIgnore
	public int getTtl() {
		return 0;
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public Map<String, Boolean> indexMap() {
		Map<String, Boolean> indexMap = new HashMap<>();
		indexMap.put("key", false);
		indexMap.put("name", false);
		return indexMap;
	}

	@Override
	@JsonIgnore
	public synchronized void readData(ObjectDataInput in) throws IOException {
		immutableCheck();
		this.key = in.readUTF();
		this.name = in.readUTF();
		this.value = in.readObject();
		constraintCheck();
	}

	@Override
	@JsonIgnore
	public void writeData(ObjectDataOutput out) throws IOException {
		constraintCheck();
		this.immutable = true;
		constraintCheck();
		out.writeUTF(this.key);
		out.writeUTF(this.name);
		out.writeObject(this.value);
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
		if (name == null) {
			throw CloudError.newInstance(CloudException.entityMalformed("name is null"));
		}
	}

	@JsonIgnore
	private void immutableCheck() {
		CommonTools.immutableCheck(immutable);
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

	// ============================================================================================

	@Override
	@JsonIgnore
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		HelloWorldDAOImpl other = (HelloWorldDAOImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	// ============================================================================================

}