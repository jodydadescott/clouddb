package us.jodyscott.clouddb.core.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.SimpleError;

public class SimpleErrorImpl implements SimpleError {

	@JsonProperty("code") private int code;
	@JsonProperty("traceback") private int traceback;
	@JsonProperty("message") private String message;

	public SimpleErrorImpl(int code, int traceback, String message) {
		this.code = code;
		this.traceback = traceback;
		if (message == null) {
			this.message = new String();
		} else {
			this.message = message;
		}
	}

	@Override
	@JsonGetter("code")
	public int getCode() {
		return code;
	}

	@Override
	@JsonSetter("code")
	public void setCode(int code) {
		this.code = code;
	}

	@Override
	@JsonGetter("traceback")
	public int getTraceBack() {
		return traceback;
	}

	@Override
	@JsonSetter("traceback")
	public void setTraceBack(int traceback) {
		this.traceback = traceback;
	}

	@Override
	@JsonSetter("message")
	public void setMessage(String message) {
		if (message == null) {
			this.message = new String();
		} else {
			this.message = message;
		}
	}

	@Override
	@JsonGetter("message")
	public String getMessage() {
		return message;
	}

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

}
