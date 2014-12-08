package util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class NameProvider {

	private static Map<Class<?>, Counter> counters = new HashMap<>();
	private static Map<Object, String> names = new IdentityHashMap<>();

	public static String nameFor(Object obj) {
		return nameForOrDefault(obj, null);
	}

	public static String nameForOrDefault(Object obj, String defaultName) {
		if (obj == null)
			throw new NullPointerException("obj is null");

		Class<?> clazz = obj.getClass();

//		IdentityKey key = identityKey(obj);
		String name = names.getOrDefault(obj, defaultName);

		if (name != null)
			return name;

		name = generateNameFor(clazz);
		names.put(obj, name);

		return name;
	}

	public static void setNameFor(Object obj, String name) {
		if (obj == null)
			throw new NullPointerException("obj is null");
		if (name == null)
			throw new NullPointerException("name is null");

		names.putIfAbsent(obj, name);
	}

	private static String generateNameFor(Class<?> clazz) {
		Counter counter = counters.get(clazz);

		if (counter == null) {
			counter = new Counter();
			counters.put(clazz, counter);
		}

		String name = String.format("%s#%d",
			clazz.getSimpleName(), counter.getCounter());

		counter.increment();

		return name;
	}

	private static class Counter {
		private int counter = 0;

		public void increment() {
			++counter;
		}

		public int getCounter() {
			return counter;
		}
	}

//	private static IdentityKey identityKey(Object obj) {
//		return new IdentityKey(obj);
//	}

//	private static class IdentityKey {
//		private final int hash;
//		private final Object obj;
//
//		public IdentityKey(Object obj) {
//			this.hash = System.identityHashCode(obj);
//			this.obj = obj;
//		}
//
//		@Override
//		public int hashCode() {
//			return hash;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			IdentityKey other = (IdentityKey) obj;
//			if (this.obj == null) {
//				if (other.obj != null)
//					return false;
//			} else if (this.obj != other.obj)
//				return false;
//			return true;
//		}
//
//	}

//	private static class NameWrapper {
//		private final String name;
//
//		public NameWrapper(Object obj) {
//			this.name = nameFor(obj);
//		}
//
//		@Override
//		public String toString() {
//			return name;
//		}
//	}

}
