package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.SimpleErrorImpl;

public interface SimpleError {

	public static SimpleError newInstance(int code, int traceback, String message) {
		return new SimpleErrorImpl(code, traceback, message);
	}

	int getCode();

	void setCode(int code);

	int getTraceBack();

	void setTraceBack(int traceback);

	String getMessage();

	void setMessage(String message);

}
