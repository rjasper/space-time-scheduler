package de.tu_berlin.kbs.swarmos.st_scheduler.util;

import java.util.Map.Entry;

public final class Maps {
	
	private Maps() {}
	
	public static <K> K key(Entry<K, ?> entry) {
		return entry == null ? null : entry.getKey();
	}
	
	public static <V> V value(Entry<?, V> entry) {
		return entry == null ? null : entry.getValue();
	}

}
