package us.jodyscott.clouddb.core.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import us.jodyscott.clouddb.core.DAO;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.Event;
import us.jodyscott.clouddb.core.JsonMapper;

public class EventImpl<T> implements Event<T> {

	// ======================================================================================================

	@JsonProperty("entityEventType") private EntityEventType entityEventType;
	@JsonProperty("classId") private int classId;
	@JsonIgnore private Entity<T> entityHandler;
	@JsonProperty("value") private T value;
	@JsonProperty("oldValue") private T oldValue;

	public EventImpl(EntityEventType entityEventType, int classId, Entity<T> entityHandler, T value, T oldValue) {

		assert entityEventType != null;
		assert classId > 0;
		assert entityHandler != null;

		if (value == null && oldValue == null) {
			assert false : "Value and oldValue are BOTH null, this is not expexted";
		}

		this.entityEventType = entityEventType;
		this.classId = classId;
		this.entityHandler = entityHandler;
		this.value = value;
		this.oldValue = oldValue;

	}

	// ======================================================================================================

	@Override
	@JsonGetter("entityEventType")
	public EntityEventType getEntityEventType() {
		return entityEventType;
	}

	// ======================================================================================================

	@Override
	@JsonGetter("classId")
	public int getId() {
		return classId;
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public Entity<T> getEntityHandler() {
		if (entityHandler == null) {
			// This method is not applicable when de-serialized from cold
			// storage like disk
			throw new NullPointerException("entityHandler is null");
		}
		return entityHandler;
	}

	// ======================================================================================================

	@Override
	@JsonGetter("value")
	public T getValue() {
		return value;
	}

	// ======================================================================================================

	@Override
	@JsonGetter("oldValue")
	public T getOldValue() {
		return oldValue;
	}

	@Override
	@JsonGetter("key")
	public String getKey() {
		return ((DAO) value).getKey();
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

	// ======================================================================================================

	@Override
	@JsonIgnore
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + classId;
		result = prime * result + ((entityEventType == null) ? 0 : entityEventType.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	@JsonIgnore
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventImpl<?> other = (EventImpl<?>) obj;
		if (classId != other.classId)
			return false;
		if (entityEventType != other.entityEventType)
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	// ======================================================================================================

}