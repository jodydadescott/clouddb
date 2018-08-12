package us.jodyscott.clouddb.core;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import us.jodyscott.clouddb.core.internal.SimpleException;
import us.jodyscott.clouddb.core.util.FString;

/*
 * This unchecked exception is thrown if object violates defined constraints
 * 
 */
public class CloudException extends Exception {

	// ============================================================================================
	// This differs from CloudError

	private static final long serialVersionUID = 1L;

	public final static int DB_OFFLINE = 1;
	public final static int DB_QUORUM = 2;
	public final static int DB_LOCK_FAILED = 3;

	public final static int REQUEST_MALFORMED = 100;
	public final static int ENTITY_NOT_FOUND = 101;
	public final static int ENTITY_MALFORMED = 102;
	public final static int ENTITY_TTL_TO_BIG = 103;
	public final static int ENTITY_TTL_TO_SMALL = 104;

	private final static Map<Integer, String> intToStringMap = new HashMap<>();

	static {
		intToStringMap.put(DB_OFFLINE, "database offline");
		intToStringMap.put(DB_QUORUM, "database does not have quorum");
		intToStringMap.put(REQUEST_MALFORMED, "request is malformed");
		intToStringMap.put(ENTITY_NOT_FOUND, "entity not found");
		intToStringMap.put(ENTITY_MALFORMED, "entity is malformed");
		intToStringMap.put(DB_LOCK_FAILED, "database lock failed");
		intToStringMap.put(ENTITY_TTL_TO_BIG, "TTL to small");
		intToStringMap.put(ENTITY_TTL_TO_SMALL, "TTL to big");
	}

	public static CloudException user(int usercode, String msg, Object... objects) {
		return new CloudException(usercode, msg, objects);
	}

	public static CloudException entityNotFound(String msg, Object... objects) {
		return new CloudException(ENTITY_NOT_FOUND, msg, objects);
	}

	public static CloudException requestMalformed(String msg, Object... objects) {
		return new CloudException(REQUEST_MALFORMED, msg, objects);
	}

	public static CloudException entityMalformed(String msg, Object... objects) {
		return new CloudException(ENTITY_MALFORMED, msg, objects);
	}

	public static CloudException lockFailed(String msg, Object... objects) {
		return new CloudException(DB_LOCK_FAILED, msg, objects);
	}

	public static CloudException offline(String msg, Object... objects) {
		return new CloudException(DB_OFFLINE, msg, objects);
	}

	public static CloudException quorum(String msg, Object... objects) {
		return new CloudException(DB_QUORUM, msg, objects);
	}

	public static CloudException entityTtlToBig(String msg, Object... objects) {
		return new CloudException(ENTITY_TTL_TO_BIG, msg, objects);
	}

	public static CloudException entityTtlToSmall(String msg, Object... objects) {
		return new CloudException(ENTITY_TTL_TO_SMALL, msg, objects);
	}

	// ============================================================================================

	private int code;

	// ============================================================================================

	private CloudException(int code, String message, Object... objects) {
		super(FString.format("Code->{}, Message->{}", code,
				FString.format(intToStringMap.get(code) + "::" + message, objects)));
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public SimpleException toSimpleException() {
		return new SimpleException(code, this.getMessage());
	}

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

	// ============================================================================================

}
