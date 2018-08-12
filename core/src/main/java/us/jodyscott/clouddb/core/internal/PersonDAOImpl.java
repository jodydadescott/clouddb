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
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.PersonDAO;
import us.jodyscott.clouddb.core.util.CommonTools;

public class PersonDAOImpl implements IdentifiedDataSerializable, PersonDAO {

	// ============================================================================================

	private static int getClazzId() {
		return Constants.USER_PERSON_DAO;
	}

	// ============================================================================================

	@JsonProperty("key") private String key;
	@JsonProperty("lastName") private String lastName;
	@JsonProperty("firstName") private String firstName;

	@JsonIgnore private boolean immutable;

	public PersonDAOImpl() {

	}

	public PersonDAOImpl(PersonDAO dao) {
		CommonTools.nullCheck("dao", dao);
		this.key = dao.getKey();
		this.lastName = dao.getLastName();
		this.firstName = dao.getFirstName();
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
	@JsonGetter("lastName")
	public String getLastName() {
		return this.lastName;
	}

	@Override
	@JsonSetter("lastName")
	public void setLastName(String lastName) {
		immutableCheck();
		this.lastName = lastName;
	}

	// ============================================================================================

	@Override
	@JsonGetter("firstName")
	public String getFirstName() {
		return this.firstName;
	}

	@Override
	@JsonSetter("value")
	public void setFirstName(String firstName) {
		immutableCheck();
		this.firstName = firstName;
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
		indexMap.put("lastName", true);
		indexMap.put("firstName", false);
		return indexMap;
	}

	@Override
	@JsonIgnore
	public synchronized void readData(ObjectDataInput in) throws IOException {
		immutableCheck();
		this.key = in.readUTF();
		this.lastName = in.readUTF();
		this.firstName = in.readObject();
		constraintCheck();
	}

	@Override
	@JsonIgnore
	public void writeData(ObjectDataOutput out) throws IOException {
		constraintCheck();
		this.immutable = true;
		constraintCheck();
		out.writeUTF(this.key);
		out.writeUTF(this.lastName);
		out.writeObject(this.firstName);
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

	// ============================================================================================

	@JsonIgnore
	private void constraintCheck() {
		if (this.lastName == null) {
			throw CloudError.newInstance(CloudException.entityMalformed("lastName is null"));
		}
		if (this.firstName == null) {
			throw CloudError.newInstance(CloudException.entityMalformed("firstName is null"));
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonDAOImpl other = (PersonDAOImpl) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}

	// ============================================================================================

}