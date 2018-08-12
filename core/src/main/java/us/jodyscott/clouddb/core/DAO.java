package us.jodyscott.clouddb.core;

import java.util.Map;

/*
 * All DAO Interfaces extend this interface. This is implemented in an abstract class that each DAO extends and implements its specific interface.
 * 
 */
public interface DAO {

	void setTtl(int ttl);

	int getTtl();

	void setKey(String key);

	String getKey();

	int getId();

	Map<String, Boolean> indexMap();

	boolean isImmutable();

}
