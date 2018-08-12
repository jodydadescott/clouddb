package us.jodyscott.clouddb.core.util;

public class IpTools {

	public static Integer ipDecimalNotationToInt(String ipAddr) {

		if (ipAddr == null) {
			return null;
		}

		String[] octets = ipAddr.split("\\.");

		if (octets.length != 4)
			throw new NumberFormatException("Expecting four octets seperated by periods, found " + octets.length);

		int result = 0;
		for (int i = 3; i >= 0; i--) {
			long octet = Integer.parseInt(octets[3 - i]);

			if (octet > 255 || octet < 0) {
				throw new NumberFormatException(
						"IP octet " + octet + " in address " + ipAddr + " is NOT valid (expected value is 0 to 255)");
			}

			result |= (octet) << (i * 8);
		}

		return result;
	}

	public static String ipDecimalNotationToString(Integer ipAddr) {

		if (ipAddr == null) {
			return null;
		}

		return ((ipAddr >> 24) & 0xFF) + "." + ((ipAddr >> 16) & 0xFF) + "." + ((ipAddr >> 8) & 0xFF) + "."
				+ (ipAddr & 0xFF);
	}

}
