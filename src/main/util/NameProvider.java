package util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class NameProvider {

	private static Map<Class<?>, Counter> counters = new HashMap<>();
	private static Map<Object, String> names = new IdentityHashMap<>();

	public static String nameFor(Object obj) {
		return nameForOrDefault(obj, () -> generateNameFor(obj));
	}

	public static String nameForOrDefault(Object obj, String defaultName) {
		return nameForOrDefault(obj, () -> defaultName);
	}

	public static String nameForOrDefault(Object obj, Supplier<String> defaultSupplier) {
		Objects.requireNonNull(obj, "obj");
		Objects.requireNonNull(defaultSupplier, "defaultSupplier");

		String name = names.get(obj);

		if (name != null)
			return name;

		name = defaultSupplier.get();
		names.put(obj, name);

		return name;
	}

	public static void setNameFor(Object obj, String name) {
		Objects.requireNonNull(obj, "obj");
		Objects.requireNonNull(name, "name");

		names.put(obj, name);
	}

	private static String generateNameFor(Object object) {
		Class<?> clazz = object.getClass();
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

}
