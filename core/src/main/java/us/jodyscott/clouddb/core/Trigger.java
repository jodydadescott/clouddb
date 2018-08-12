package us.jodyscott.clouddb.core;

public interface Trigger<T> {

	void handle(Event<T> entityEvent) throws CloudException;

}
