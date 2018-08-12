package us.jodyscott.clouddb.core.internal;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.util.FString;

public class SerialFactory implements DataSerializableFactory {

	@Override
	public IdentifiedDataSerializable create(int id) {

		switch (id) {

		case Constants.CLOUD_DB_CONFIG:
			return new CloudConfigImpl();

		case Constants.CLOUD_MEMBER:
			return new CloudMemberImpl();

		case Constants.SERIAL_HASH_SET:
			return new SerialHashSetImpl<Object>();

		case Constants.USER_HELLO_WORLD:
			return new HelloWorldDAOImpl();

		case Constants.USER_PERSON_DAO:
			return new PersonDAOImpl();

		}

		throw new AssertionError(FString.format("Class with id {} not found", id));
	}

}
