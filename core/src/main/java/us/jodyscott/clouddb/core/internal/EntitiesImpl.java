package us.jodyscott.clouddb.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.Entities;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.HelloWorldDAO;
import us.jodyscott.clouddb.core.PersonDAO;

public class EntitiesImpl extends EntitiesAbstractImpl implements Entities {

	// ======================================================================================================

	private static final Logger LOG = LoggerFactory.getLogger(EntitiesImpl.class);

	// ======================================================================================================

	private final EntityImpl<HelloWorldDAO> helloWorld;
	private final EntityImpl<PersonDAO> person;

	EntitiesImpl(CloudControllerImpl cloudController) {
		super();
		LOG.trace("enter (cloudController={})", cloudController);
		assert cloudController != null;

		this.helloWorld = new EntityImpl<>(new HelloWorldDAOImpl(), cloudController);
		this.person = new EntityImpl<>(new PersonDAOImpl(), cloudController);

		this.entityMap.put(helloWorld.getId(), helloWorld);
		this.entityMap.put(person.getId(), person);

		LOG.trace("exit (hazelcastInstance={})", cloudController);
	}

	@Override
	public Entity<HelloWorldDAO> helloWorld() {
		return helloWorld;
	}

	@Override
	public Entity<PersonDAO> person() {
		return person;
	}

	// ======================================================================================================

}
