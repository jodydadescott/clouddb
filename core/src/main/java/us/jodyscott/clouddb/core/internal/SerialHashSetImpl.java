package us.jodyscott.clouddb.core.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import us.jodyscott.clouddb.core.Constants;
import us.jodyscott.clouddb.core.JsonMapper;
import us.jodyscott.clouddb.core.SerialSet;
import us.jodyscott.clouddb.core.util.CommonTools;

public class SerialHashSetImpl<T> implements SerialSet<T>, IdentifiedDataSerializable {

	// ======================================================================================================

	private static int getClazzId() {
		return Constants.SERIAL_HASH_SET;
	}

	// ======================================================================================================

	private Set<T> backingSet = new HashSet<>();

	private boolean immutable;

	// ======================================================================================================

	@Override
	public boolean isImmutable() {
		return this.immutable;
	}

	@Override
	public int size() {
		return backingSet.size();
	}

	@Override
	public boolean isEmpty() {
		return backingSet.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return backingSet.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return backingSet.iterator();
	}

	@Override
	public Object[] toArray() {
		return backingSet.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return backingSet.toArray(a);
	}

	@Override
	public boolean add(T e) {
		immutableCheck();
		return backingSet.add(e);
	}

	@Override
	public boolean remove(Object o) {
		immutableCheck();
		return backingSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return backingSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		immutableCheck();
		return backingSet.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		immutableCheck();
		return backingSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		immutableCheck();
		return backingSet.removeAll(c);
	}

	@Override
	public void clear() {
		immutableCheck();
		backingSet.clear();
	}

	// ======================================================================================================

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		immutableCheck();

		int arraySize = in.readInt();

		for (int i = 0; i < arraySize; i++) {
			this.backingSet.add(in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		this.immutable = true;
		out.writeInt(this.backingSet.size());
		for (T t : this.backingSet) {
			out.writeObject(t);
		}
	}

	@Override
	public int getFactoryId() {
		return Constants.FACTORY_ID;
	}

	@Override
	public int getId() {
		return getClazzId();
	}

	// ======================================================================================================

	@Override
	public String toString() {
		return JsonMapper.singleton().objectToJson(this);
	}

	// ======================================================================================================

	private void immutableCheck() {
		CommonTools.immutableCheck(immutable);
	}

	// ======================================================================================================

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backingSet == null) ? 0 : backingSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SerialHashSetImpl<?> other = (SerialHashSetImpl<?>) obj;
		if (backingSet == null) {
			if (other.backingSet != null)
				return false;
		} else if (!backingSet.equals(other.backingSet))
			return false;
		return true;
	}

	// ======================================================================================================

}
