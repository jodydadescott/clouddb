package us.jodyscott.clouddb.core.internal;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClientNotActiveException;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.query.SqlPredicate;

import us.jodyscott.clouddb.core.CloudError;
import us.jodyscott.clouddb.core.CloudException;
import us.jodyscott.clouddb.core.DAO;
import us.jodyscott.clouddb.core.Entity;
import us.jodyscott.clouddb.core.EntityListener;
import us.jodyscott.clouddb.core.SerialSet;
import us.jodyscott.clouddb.core.Trigger;
import us.jodyscott.clouddb.core.Event.EntityEventType;
import us.jodyscott.clouddb.core.util.FString;

public class EntityImpl<T> implements Entity<T> {

	private static final Logger LOG = LoggerFactory.getLogger(EntityImpl.class);

	private static final int DEFAULT_LOCK_TRY_TIMEOUT = 10;
	private static final int MAX_ENTITY_TTL = 31536000; // 1 Year
	private static final int MIN_ENTITY_TTL = 15;

	private final CloudControllerImpl cloudController;
	private final int id;
	private final String className;

	private Trigger<T> preTrigger;
	private Trigger<T> postTrigger;

	// private volatile boolean online = false;

	EntityImpl(DAO dao, CloudControllerImpl cloudController) {
		LOG.trace("enter (dao={}, cloudController={})", dao, cloudController);

		assert dao != null;
		assert cloudController != null;

		this.cloudController = cloudController;

		this.id = dao.getId();
		this.className = dao.getClass().getSimpleName();

		for (Entry<String, Boolean> entry : dao.indexMap().entrySet()) {
			LOG.trace("Adding index for key {} with value {}", entry.getKey(), entry.getValue());
			try {
				map().addIndex(entry.getKey(), entry.getValue());
			} catch (CloudException e) {
				throw new AssertionError(e);
			}
		}

		LOG.trace("exit (dao={}, cloudController={})", dao, cloudController);
	}

	@Override
	public int getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	private IMap<String, T> map() throws CloudException {
		return (IMap<String, T>) cloudController.getMap("MAP::" + id);
	}

	private ILock lock() throws CloudException {
		return cloudController.getLock("LOCK::" + id);
	}

	private String newId() throws CloudException {
		String newId = String.valueOf(cloudController.getFlakeIdGenerator("IDGEN::" + id).newId());
		return newId;
	}

	@Override
	public T get(String key) throws CloudException {

		try {

			LOG.trace("enter {} get(key={})", className, key);

			if (key == null) {
				throw CloudException.requestMalformed("key is null");
			}

			T result = map().get(key);

			if (result == null) {
				throw CloudException.entityNotFound("Entity with key {} not found", key);
			}

			LOG.debug("{} get(key={}) returning->{}", className, key, result);
			return result;

		} finally {
			LOG.trace("exit {} get(key={})", className, key);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean delete(String key) throws CloudException {

		try {

			LOG.trace("enter boolean delete(key={})", key);

			cloudController.tryQuorum();

			if (key == null) {
				throw CloudException.requestMalformed("key is null");
			}

			if (!map().isLocked(key)) {
				throw CloudException.lockFailed("Key {} is not locked", key);
			}

			DAO dao = (DAO) map().get(key);

			if (dao == null) {
				return false;
			}

			if (preTrigger != null) {
				preTrigger.handle(new EventImpl(EntityEventType.REMOVE, id, this, dao, dao));
			}

			map().remove(key);
			unlock(key);
			LOG.info("removed key {}", key);

			if (postTrigger != null) {
				postTrigger.handle(new EventImpl(EntityEventType.REMOVE, id, this, dao, dao));
			}

			return true;

		} catch (CloudError e) {
			throw e.getCloudException();

		} catch (HazelcastClientNotActiveException e) {
			throw CloudException.quorum("DB not active");

		} finally {
			LOG.trace("exit boolean delete(key={})", key);
		}

	}

	@Override
	public T create(T dao) throws CloudException {

		try {

			LOG.trace("enter {} create(dao={})", className, dao);

			if (dao == null) {
				throw CloudException.requestMalformed("dao is null");
			}

			DAO _dao = (DAO) dao;

			if (_dao.getKey() != null) {
				throw CloudException.entityMalformed("Entity should not have key set on create");
			}

			String key = newId();
			_dao.setKey(key);
			lock(_dao.getKey());

			return putOperation(dao);

		} finally {
			LOG.trace("exit {} create(dao={})", className, dao);
		}
	}

	@Override
	public T update(T dao) throws CloudException {

		try {

			LOG.trace("enter {} update(dao={})", className, dao);

			if (dao == null) {
				throw CloudException.requestMalformed("dao is null");
			}

			DAO _dao = (DAO) dao;

			if (_dao.getKey() == null) {
				throw CloudException.entityMalformed("Entity should have key set on update");
			}

			return putOperation(dao);

		} finally {
			LOG.trace("exit {} update(dao={})", className, dao);
		}

	}

	private T putOperation(T dao) throws CloudException {

		try {

			LOG.trace("enter {} putOperation(dao={})", className, dao);

			assert dao != null;
			DAO _dao = (DAO) dao;

			cloudController.tryQuorum();

			if (_dao.getTtl() > 0) {

				if (_dao.getTtl() > MAX_ENTITY_TTL) {
					throw CloudException.entityTtlToBig(
							"ttl of {} is illegal, must be greater then {} and less then {}", MIN_ENTITY_TTL,
							MAX_ENTITY_TTL);
				}
				if (_dao.getTtl() < MIN_ENTITY_TTL) {
					throw CloudException.entityTtlToSmall(
							"ttl of {} is illegal, must be greater then {} and less then {}", MIN_ENTITY_TTL,
							MAX_ENTITY_TTL);
				}

			}

			try {

				if (!map().isLocked(_dao.getKey())) {
					throw CloudException.lockFailed("Key {} is not locked", _dao.getKey());
				}

				cloudController.tryQuorum();

				T existingDao = map().get(_dao.getKey());

				if (preTrigger != null) {
					EntityEventType type = null;

					if (existingDao == null) {
						type = EntityEventType.ADD;
					} else {
						type = EntityEventType.UPDATE;
					}

					preTrigger.handle(new EventImpl<>(type, id, this, dao, existingDao));
				}

				T result = null;
				DAO _result = null;

				if (existingDao == null) {
					LOG.info("Create: entity->{}", _dao);

					map().put(_dao.getKey(), (T) dao, _dao.getTtl(), TimeUnit.SECONDS);
					result = map().get(_dao.getKey());
					_result = (DAO) result;

				} else {

					if (existingDao.equals(_dao)) {
						LOG.info("ignored (already exist) entity->{}", _dao);
					} else {
						LOG.info("Replaced ", _dao, existingDao);
						LOG.info("Update: entity->{}", _dao);

						map().put(_dao.getKey(), (T) dao, _dao.getTtl(), TimeUnit.SECONDS);
						result = map().get(_dao.getKey());
						_result = (DAO) result;
					}

				}

				if (_result.getTtl() == 0) {
					LOG.trace("Entity key {} with NO eviction", _result.getKey());
				} else {
					LOG.trace("Entity key {} with eviction of {}", _result.getKey(), _result.getTtl());
				}

				if (postTrigger != null) {
					EntityEventType type = null;

					if (existingDao == null) {
						type = EntityEventType.ADD;
					} else {
						type = EntityEventType.UPDATE;
					}

					postTrigger.handle(new EventImpl<>(type, id, this, dao, existingDao));
				}

				return result;

			} catch (CloudError e) {

				throw e.getCloudException();

			} catch (HazelcastClientNotActiveException e) {
				throw CloudException.quorum("DB not active");
			}

			finally {
				if (map().isLocked(_dao.getKey())) {
					unlock(_dao.getKey());
				}
			}

		} finally {
			LOG.trace("exit {} putOperation(dao={})", className, dao);
		}

	}

	@Override
	public Collection<T> values() throws CloudException {

		try {
			LOG.trace("enter Collection<{}> values()", className);

			SerialSet<T> results = SerialSet.newHashInstance();
			results.addAll(map().values());
			LOG.debug("Collection<{}> values() returning.size()->{}", className, results.size());
			return results;

		} finally {
			LOG.trace("exit Collection<{}> values()", className);
		}
	}

	@Override
	public Collection<T> search(String sql) throws CloudException {

		try {
			LOG.trace("enter Collection<{}> search(sql={})", className, sql);

			if (sql == null) {
				throw CloudException.requestMalformed("sql is null");
			}

			SerialSet<T> results = SerialSet.newHashInstance();
			results.addAll(map().values(new SqlPredicate(sql)));
			LOG.debug("Collection<{}> search(sql={}) size()->{}", className, sql, results.size());
			LOG.trace("Collection<{}> search(sql={})", className, sql);
			return results;

		} finally {
			LOG.trace("exit Collection<{}> search(sql={})", className, sql);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public String addListener(EntityListener<T> listener) throws CloudException {

		try {
			LOG.trace("enter String addListener(listener=)", listener);

			if (listener == null) {
				throw CloudException.requestMalformed("listener is null");
			}

			String result = null;

			String key = listener.getFilterByKey();
			String sql = listener.getFilterBySql();

			if (key == null && sql == null) {

				result = map().addEntryListener(new EntryListenerDelegate(listener), true);

			} else if (key != null) {

				result = map().addEntryListener(new EntryListenerDelegate(listener), key, true);

			} else if (sql != null) {

				@SuppressWarnings("unchecked")
				com.hazelcast.query.Predicate<String, T> sqlPredicte = new com.hazelcast.query.SqlPredicate(sql);
				result = map().addEntryListener(new EntryListenerDelegate(listener), sqlPredicte, true);
			}

			LOG.debug("String addListener(listener={}) returning->{}", listener, result);
			return result;

		} finally {
			LOG.trace("exit String addListener(listener=)", listener);
		}
	}

	@Override
	public boolean removeListener(String id) throws CloudException {

		try {
			LOG.trace("enter boolean removeListener(id={})", id);

			if (id == null) {
				throw CloudException.requestMalformed("id is null");
			}

			assert id != null;
			boolean result = map().removeEntryListener(id);
			LOG.debug("{} addListener(id={}) returning->{}", boolean.class.getSimpleName(), id, result);
			return result;

		} finally {
			LOG.trace("exit boolean removeListener(id={})", id);
		}
	}

	@Override
	public void lockMap() throws CloudException {

		try {
			LOG.trace("enter void lockMap()");

			try {
				if (lock().tryLock(DEFAULT_LOCK_TRY_TIMEOUT, TimeUnit.SECONDS)) {
					return;
				}
			} catch (InterruptedException e) {
			}
			throw CloudException.lockFailed(FString.format("Lock failed for map type {}", className));

		} finally {
			LOG.trace("exit void lockMap()");
		}
	}

	@Override
	public void lockMap(long time, TimeUnit timeunit) throws CloudException {

		try {
			LOG.trace("enter lockMap(time={}, timeunit={}", time, timeunit);

			if (time <= 0) {
				throw CloudException.requestMalformed("time must be greater than zero");
			}

			if (timeunit == null) {
				throw CloudException.requestMalformed("timeunit is null");
			}

			try {
				if (lock().tryLock(time, timeunit)) {
					return;
				}
			} catch (InterruptedException e) {
			}
			throw CloudException.lockFailed(FString.format("Lock failed for map type {}", className));

		} finally {
			LOG.trace("exit lockMap(time={}, timeunit={}", time, timeunit);
		}
	}

	@Override
	public void unlockMap() throws CloudException {
		LOG.trace("enter void unlockMap()");
		lock().unlock();
		LOG.trace("exit void unlockMap()");
	}

	@Override
	public boolean isMapLocked() throws CloudException {
		try {
			LOG.trace("enter boolean isMapLocked()");
			boolean result = lock().isLocked();
			LOG.debug("boolean isMapLocked() ->{}", result);
			return result;

		} finally {
			LOG.trace("exit boolean isMapLocked()");
		}
	}

	@Override
	public void lock(String key) throws CloudException {

		try {
			LOG.trace("enter void lock(key={})", key);

			if (key == null) {
				throw CloudException.requestMalformed("key is null");
			}

			try {
				if (map().tryLock(key, DEFAULT_LOCK_TRY_TIMEOUT, TimeUnit.SECONDS)) {
					LOG.trace("Locked map {} key {}", className, key);
					return;
				}
			} catch (InterruptedException e) {
				throw CloudException.lockFailed(FString.format("Lock failed for map type {}", className));
			}
			throw CloudException.lockFailed(FString.format("Lock failed for map type {}", className));

		} finally {
			LOG.trace("exit void lock(key={})", key);
		}
	}

	@Override
	public void setPreTrigger(Trigger<T> preTrigger) {
		LOG.trace("enter setPreTrigger(preTrigger={})", preTrigger);
		this.preTrigger = preTrigger;
		LOG.trace("exit setPreTrigger(preTrigger={})", preTrigger);
	}

	@Override
	public void setPostTrigger(Trigger<T> postTrigger) {
		LOG.trace("enter setPostTrigger(preTrigger={})", postTrigger);
		this.postTrigger = postTrigger;
		LOG.trace("exit setPostTrigger(preTrigger={})", postTrigger);
	}

	private void unlock(String key) throws CloudException {
		LOG.trace("enter unlock(key={})", key);
		assert key != null;
		map().unlock(key);
		LOG.trace("exit unlock(key={})", key);
	}

	private Entity<T> thisEntityHandler = this;

	private class EntryListenerDelegate implements EntryListener<String, T> {

		// There is a EntryListener created on each member. We do not want all
		// of them to fire so we use a lock to determine who will fire and who
		// will hold.

		private final EntityListener<T> entityListener;

		public EntryListenerDelegate(EntityListener<T> entityListener) {
			assert entityListener != null;
			this.entityListener = entityListener;
		}

		@Override
		public void entryAdded(EntryEvent<String, T> entryEvent) {
			LOG.trace("enter void entryAdded(entryEvent={}", entryEvent);
			entityListener.handle(new EventImpl<T>(EntityEventType.ADD, id, thisEntityHandler, entryEvent.getValue(),
					entryEvent.getValue()));
			LOG.trace("exit void entryAdded(entryEvent={}", entryEvent);
		}

		@Override
		public void entryEvicted(EntryEvent<String, T> entryEvent) {
			LOG.trace("enter void entryEvicted(entryEvent={}", entryEvent);
			entityListener.handle(new EventImpl<T>(EntityEventType.EVICT, id, thisEntityHandler, entryEvent.getValue(),
					entryEvent.getValue()));
			LOG.trace("exit void entryEvicted(entryEvent={}", entryEvent);
		}

		@Override
		public void entryRemoved(EntryEvent<String, T> entryEvent) {
			LOG.trace("enter void entryRemoved(entryEvent={}", entryEvent);
			entityListener.handle(
					new EventImpl<T>(EntityEventType.REMOVE, id, thisEntityHandler, entryEvent.getOldValue(), null));
			LOG.trace("exit void entryRemoved(entryEvent={}", entryEvent);
		}

		@Override
		public void entryUpdated(EntryEvent<String, T> entryEvent) {
			LOG.trace("enter void entryUpdated(entryEvent={}", entryEvent);
			entityListener.handle(new EventImpl<T>(EntityEventType.UPDATE, id, thisEntityHandler, entryEvent.getValue(),
					entryEvent.getOldValue()));
			LOG.trace("exit void entryUpdated(entryEvent={}", entryEvent);
		}

		@Override
		public void mapCleared(MapEvent mapEvent) {
			throw new AssertionError("This should not have happened");
		}

		@Override
		public void mapEvicted(MapEvent mapEvent) {
			// Maps are not evicted, hence geeting here is not expected
			throw new AssertionError("The method mapEvicted is not implemented and this even should NOT occur");
		}

	}

}
