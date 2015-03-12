package de.tu_berlin.kbs.swarmos.st_scheduler.util;

public interface TriFunction<T, U, V, R> {
	
    R apply(T t, U u, V v);

}
