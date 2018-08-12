package us.jodyscott.clouddb.core.internal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.jodyscott.clouddb.core.JsonMapper;

public class JsonMapperImpl implements JsonMapper {

	private static JsonMapper singleton = new JsonMapperImpl();

	public static JsonMapper singleton() {
		return singleton;
	}

	private final ObjectMapper objectMapper = new ObjectMapperImpl();

	private JsonMapperImpl() {

	}

	@Override
	public String objectToJson(Object object) {
		assert object != null;
		try {
			String json = objectMapper.writeValueAsString(object);
			return json;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public <T> T jsonToObject(Class<T> clazz, String json) {
		assert clazz != null;
		assert json != null;
		try {
			T result = (T) objectMapper.readValue(json, clazz);
			return result;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
