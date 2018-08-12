package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.JsonMapperImpl;

public interface JsonMapper {

	public static JsonMapper singleton() {
		return JsonMapperImpl.singleton();
	}

	public String objectToJson(Object object);

	public <T> T jsonToObject(Class<T> clazz, String json);

}
