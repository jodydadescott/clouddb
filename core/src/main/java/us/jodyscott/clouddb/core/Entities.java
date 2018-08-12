package us.jodyscott.clouddb.core;

public interface Entities {

	Entity<HelloWorldDAO> helloWorld();

	Entity<PersonDAO> person();

	Entity<?> getById(int id);

}
