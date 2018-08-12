package us.jodyscott.clouddb.core;

import us.jodyscott.clouddb.core.internal.IpAddrPortImpl;
import us.jodyscott.clouddb.core.util.IpTools;

public interface IpAddrPort {

	public static final String IP_PORT_DELIMITER = ":";

	public static IpAddrPort valueOf(Integer ipAddr, Integer port) {

		if (ipAddr == null && port == null) {
			return null;
		}

		IpAddrPort result = new IpAddrPortImpl();

		if (ipAddr != null) {
			result.setIpAddr(ipAddr);
		}

		if (port != null) {
			result.setPort(port);
		}

		return result;
	}

	public static IpAddrPort valueOf(String ipAddrPort) {

		if (ipAddrPort == null) {
			return null;
		}

		if (ipAddrPort.contains(IP_PORT_DELIMITER)) {
			String split[] = ipAddrPort.split(IP_PORT_DELIMITER);

			return new IpAddrPortImpl().setIpAddr(IpTools.ipDecimalNotationToInt(split[0]))
					.setPort(Integer.valueOf(split[1]));

		}
		return new IpAddrPortImpl().setIpAddr(IpTools.ipDecimalNotationToInt(ipAddrPort));
	}

	int getIpAddr();

	IpAddrPort setIpAddr(Integer ipAddr);

	boolean hasIpAddr();

	Integer getPort();

	IpAddrPort setPort(Integer port);

	boolean hasPort();

}
