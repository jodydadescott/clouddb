package us.jodyscott.clouddb.core;

public interface EntityListener<T> {

	void handle(Event<T> event);

	String getFilterByKey();

	String getFilterBySql();

}
