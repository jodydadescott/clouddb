package us.jodyscott.clouddb.core;

public interface Event<T> {

	public enum EntityEventType {
		ADD, UPDATE, REMOVE, EVICT
	};

	EntityEventType getEntityEventType();

	int getId();

	Entity<T> getEntityHandler();

	T getValue();

	T getOldValue();

	String getKey();

}