package us.jodyscott.clouddb.core.util;

public class CommonTools {

	public static void nullCheck(String s, Object o) {
		assert s != null;
		if (o == null) {
			throw new NullPointerException(s);
		}
	}

	public static void immutableCheck(boolean immutable) {
		if (immutable) {
			throw new AssertionError("Entity is immutable");
		}
	}

	public static void portCheck(Integer port) {
		if (port == null) {
			return;
		}

		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port " + port
					+ " is invalid. Port must be greater then or equal to zero and less then or equal to 65535");
		}
	}

}
