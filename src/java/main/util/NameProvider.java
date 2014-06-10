package util;

import java.util.HashMap;

public class NameProvider {
	
	private static final HashMap<Class<?>, Integer> counters = new HashMap<>();
	
	public static String nameFor(Class<?> clazz) {
		Integer counter = counters.get(clazz);
		
		if (counter == null)
			counter = 0;
		else
			++counter;
		
		counters.put(clazz, counter);
		
		return String.format("$%d", counter);
	}
	
	public static void reset(Class<?> clazz) {
		counters.remove(clazz);
	}

}
