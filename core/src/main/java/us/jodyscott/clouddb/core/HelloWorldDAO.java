package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.HelloWorldDAOImpl;

public interface HelloWorldDAO extends DAO {

	public static HelloWorldDAO newInstance() {
		return new HelloWorldDAOImpl();
	}

	public static HelloWorldDAO newInstance(String json) {
		if (json == null) {
			return newInstance();
		}
		return JsonMapper.singleton().jsonToObject(HelloWorldDAO.class, json);
	}

	void setName(String name);

	String getName();

	void setValue(String value);

	String getValue();

	boolean hasValue();

}
