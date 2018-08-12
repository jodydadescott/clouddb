package us.jodyscott.clouddb.core.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import us.jodyscott.clouddb.core.JsonMapper;

public class SimpleException {

	@JsonProperty("code") private int code;
	@JsonProperty("message") public String message;

	public SimpleException() {

	}

	public SimpleException(int code, String message) {
		this.code = code;
		this.message = message;
	}

	@JsonGetter("code")
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

}
