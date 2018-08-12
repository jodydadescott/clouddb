package us.jodyscott.clouddb.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.jodyscott.clouddb.core.Entities;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.util.FString;

public abstract class EntitiesAbstractImpl implements Entities {

	// ======================================================================================================

	private static final Logger LOG = LoggerFactory.getLogger(EntitiesAbstractImpl.class);

	// ======================================================================================================

	protected final Map<Integer, EntityImpl<?>> entityMap = new HashMap<>();

	@Override
	public Entity<?> getById(int id) {
		LOG.trace("enter Entity<?> getById(id={})", id);

		Entity<?> result = entityMap.get(id);

		if (result == null) {
			throw new AssertionError(FString.format("Handler with id {} not found", id));
		}

		LOG.debug("Entity<?> getById(id={}) ->{}", id, result);
		LOG.trace("exit Entity<?> getById(id={})", id);
		return result;
	}

	// ======================================================================================================

}
