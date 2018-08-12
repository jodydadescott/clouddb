package us.jodyscott.clouddb.core;

import java.util.Set;

import us.jodyscott.clouddb.core.internal.SerialHashSetImpl;

public interface SerialSet<T> extends Set<T> {

	public static <T> SerialSet<T> newHashInstance() {
		return new SerialHashSetImpl<>();
	}

	boolean isImmutable();

}
