package de.tu_berlin.kbs.swarmos.st_scheduler.util;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.IntFunction;

// TODO document
public class SmartArrayCache<T> {
	
	private static final SoftReference<Object[]> NULL_REF =
		new SoftReference<Object[]>(null);
	
	private SoftReference<Object[]> storageRef = NULL_REF;
	
	private final int size;
	
	private final IntFunction<T> provider;
	
	public SmartArrayCache(IntFunction<T> provider, int size) {
		if (size < 0)
			throw new IllegalArgumentException("size is negative");
		
		this.size = size;
		this.provider = Objects.requireNonNull(provider, "provider");
	}
	
	@SuppressWarnings("unchecked") // cast to T
	public T get(int index) {
		if (index < 0 || index >= size)
			throw new IllegalArgumentException("invalid index");
		
		Object[] storage = storageRef.get();
		
		if (storage == null) {
			storage = new Object[size];
			storageRef = new SoftReference<Object[]>(storage);
		}
		
		Object element = storage[index];
		
		if (element == null) {
			element = provider.apply(index);
			storage[index] = element;
		}
		
		return (T) element;
	}

}
