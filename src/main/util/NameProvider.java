package util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <p>The {@link NameProvider} provides static methods to name objects.
 * The purpose is to make arbitrary objects easily identifiable during
 * debugging. It can generate names automatically by composing a string from the
 * object's class name and an integer counter. It also allows to give customized
 * names for each object individually using the
 * {@link #setNameFor(Object, String)} method.</p>
 *
 * <p>Classes whose objects cannot easily be distinguished by their attributes
 * might use the {@link #nameFor(Object)} method to implement their
 * {@link Objects#toString()} method. They can also use
 * {@link #nameForOrDefault(Object, String)} or
 * {@link #nameForOrDefault(Object, Supplier)} to provide a default name for
 * an object.</p>
 *
 * @author Rico Jasper
 */
public final class NameProvider {

	private NameProvider() {}

	/**
	 * The individual counters of classes. Used for name generation.
	 */
	private static Map<Class<?>, Counter> counters = new HashMap<>();

	/**
	 * The names of all named objects. Uses an {@link IdentityHashMap}.
	 */
	private static Map<Object, String> names = new IdentityHashMap<>();

	/**
	 * Helper class to represent a counter. Also removes the necessity to
	 * call put on {@link #counters} to update a counter's value.
	 */
	private static class Counter {
		private int counter = 0;

		public void increment() {
			++counter;
		}

		public int getCounter() {
			return counter;
		}
	}

	/**
	 * Returns the name of the object. Generates a name of the object was not
	 * named yet.
	 *
	 * @param obj
	 * @return the name.
	 * @throws NullPointerException if obj is {@code null}.
	 */
	public static String nameFor(Object obj) {
		// obj is checked by #nameForOrDefault(Object, Supplier)
		return nameForOrDefault(obj, () -> generateNameFor(obj));
	}

	/**
	 * Returns the name of the object. Uses defaultName if the object was not
	 * named yet.
	 *
	 * @param obj
	 * @param defaultName
	 * @return the name.
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public static String nameForOrDefault(Object obj, String defaultName) {
		// obj is checked by #nameForOrDefault(Object, Supplier)
		Objects.requireNonNull(defaultName, "defaultName");

		return nameForOrDefault(obj, () -> defaultName);
	}

	/**
	 * <p>Returns the name of the object. Uses the supplier to name the object if
	 * it was not named yet.</p>
	 *
	 * <p>Compared to {@link #nameForOrDefault(Object, String)} this method has
	 * the advantage that the string providing function of the supplier is
	 * only evaluated if a name of the object indeed does not exists.</p>
	 *
	 * @param obj
	 * @param defaultSupplier
	 * @return the name.
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public static String nameForOrDefault(Object obj, Supplier<String> defaultSupplier) {
		Objects.requireNonNull(obj, "obj");
		Objects.requireNonNull(defaultSupplier, "defaultSupplier");

		String name = names.get(obj);

		// return name if it exists
		if (name != null)
			return name;

		// name the object
		name = defaultSupplier.get();
		names.put(obj, name);

		return name;
	}

	/**
	 * Sets of overwrites the name of an object.
	 *
	 * @param obj the object
	 * @param name
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public static void setNameFor(Object obj, String name) {
		Objects.requireNonNull(obj, "obj");
		Objects.requireNonNull(name, "name");

		names.put(obj, name);
	}

	/**
	 * Generates the name for an object. The name will be a composition of the
	 * object's simple class name and an id: <Class>#<ID>. The id indicates the
	 * number of objects of the same class for which a name was generated at the
	 * moment of calling this method.
	 *
	 * @param object
	 * @return the name.
	 */
	private static String generateNameFor(Object object) {
		Class<?> clazz = object.getClass();
		Counter counter = counters.get(clazz);

		// initialize counter if there was no one before
		if (counter == null) {
			counter = new Counter();
			counters.put(clazz, counter);
		}

		// generate name <Class>#<ID>
		String name = String.format("%s#%d",
			clazz.getSimpleName(), counter.getCounter());

		counter.increment();

		return name;
	}

}
