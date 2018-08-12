package us.jodyscott.clouddb.core;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface Entity<T> {

	T get(String key) throws CloudException;

	void lock(String key) throws CloudException;

	boolean delete(String key) throws CloudException;

	Collection<T> values() throws CloudException;

	Collection<T> search(String sql) throws CloudException;

	String addListener(EntityListener<T> listener) throws CloudException;

	boolean removeListener(String id) throws CloudException;

	void unlockMap() throws CloudException;

	void lockMap() throws CloudException;

	void lockMap(long time, TimeUnit timeunit) throws CloudException;

	boolean isMapLocked() throws CloudException;

	int getId();

	T update(T dao) throws CloudException;

	T create(T dao) throws CloudException;

	void setPreTrigger(Trigger<T> preTrigger);

	void setPostTrigger(Trigger<T> preTrigger);

}
