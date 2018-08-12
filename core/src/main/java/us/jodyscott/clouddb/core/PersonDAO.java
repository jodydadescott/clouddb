package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.PersonDAOImpl;

public interface PersonDAO extends DAO {

	public static PersonDAO newInstance() {
		return new PersonDAOImpl();
	}

	public static PersonDAO newInstance(String json) {
		if (json == null) {
			return newInstance();
		}
		return JsonMapper.singleton().jsonToObject(PersonDAO.class, json);
	}

	void setLastName(String value);

	String getLastName();

	void setFirstName(String firstName);

	String getFirstName();

}
