package util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class ArraysClone {
	
	private ArraysClone() {}
	
	public static <T extends Cloneable> T[] deepCloneCopy(T[] array) {
		int n = array.length;
		Class<?> clazz = array.getClass();
		
		@SuppressWarnings("unchecked")
		T[] clone = (T[]) Array.newInstance(clazz.getComponentType(), n);
		
		for (int i = 0; i < n; ++i)
			clone[i] = cloneElement(array[i]);
		
		return clone;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Cloneable> T cloneElement(T element) {
		if (element == null)
			return null;
		
		Class<?> clazz = element.getClass();
		
		if (clazz.isArray()) {
			Class<?> componentType = clazz.getComponentType();
			
			// is component type also cloneable?
			if (Cloneable.class.isAssignableFrom(componentType)) {
				return (T) deepCloneCopy((Cloneable[]) element);
			} else {
				int n = Array.getLength(element);
				
				if (componentType.isPrimitive()) {
					if (componentType == boolean.class)
						return (T) Arrays.copyOf((boolean[]) element, n);
					else if (componentType == char.class)
						return (T) Arrays.copyOf((char[]) element, n);
					else if (componentType == byte.class)
						return (T) Arrays.copyOf((byte[]) element, n);
					else if (componentType == short.class)
						return (T) Arrays.copyOf((short[]) element, n);
					else if (componentType == int.class)
						return (T) Arrays.copyOf((int[]) element, n);
					else if (componentType == long.class)
						return (T) Arrays.copyOf((long[]) element, n);
					else if (componentType == float.class)
						return (T) Arrays.copyOf((float[]) element, n);
					else // (componentType == double.class)
						return (T) Arrays.copyOf((double[]) element, n);
				} else {
					return (T) Arrays.copyOf((Object[]) element, n);
				}
			}
		} else {
			try {
				Method cloneMethod = clazz.getMethod("clone");
				
				return (T) cloneMethod.invoke(element);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
