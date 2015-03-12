package de.tu_berlin.mailbox.rjasper.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Provides an array deep clone copy operation.
 * 
 * @author Rico
 */
public final class ArraysClone {
	
	private ArraysClone() {}
	
	/**
	 * Creates a new array containing a clone of each element. If an element is
	 * an array itself, then a new array is created recursively.
	 * 
	 * @param array
	 *            to be copied
	 * @return the copy. {@code null} if {@code array} is {@code null}.
	 */
	public static <T extends Cloneable> T[] deepCloneCopy(T[] array) {
		if (array == null)
			return null;
		
		int n = array.length;
		Class<?> clazz = array.getClass();
		
		@SuppressWarnings("unchecked")
		T[] clone = (T[]) Array.newInstance(clazz.getComponentType(), n);
		
		for (int i = 0; i < n; ++i)
			clone[i] = cloneElement(array[i]);
		
		return clone;
	}
	
	/**
	 * Clones the given element. If the element is an array, then a new array is
	 * created containing clones of the original elements.
	 * 
	 * @param element
	 *            to be cloned
	 * @return the clone.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Cloneable> T cloneElement(T element) {
		if (element == null)
			return null;
		
		Class<?> clazz = element.getClass();
		
		// if array then make an element-wise clone copy
		if (clazz.isArray()) {
			Class<?> componentType = clazz.getComponentType();
			
			// is component type also cloneable?
			if (Cloneable.class.isAssignableFrom(componentType)) {
				return (T) deepCloneCopy((Cloneable[]) element);
			} else {
				int n = Array.getLength(element);

				// use standard copy operation
				
				if (componentType.isPrimitive()) {
					if      (componentType == boolean.class)
						return (T) Arrays.copyOf((boolean[]) element, n);
					else if (componentType == char   .class)
						return (T) Arrays.copyOf((char   []) element, n);
					else if (componentType == byte   .class)
						return (T) Arrays.copyOf((byte   []) element, n);
					else if (componentType == short  .class)
						return (T) Arrays.copyOf((short  []) element, n);
					else if (componentType == int    .class)
						return (T) Arrays.copyOf((int    []) element, n);
					else if (componentType == long   .class)
						return (T) Arrays.copyOf((long   []) element, n);
					else if (componentType == float  .class)
						return (T) Arrays.copyOf((float  []) element, n);
					else // (componentType == double .class)
						return (T) Arrays.copyOf((double []) element, n);
				} else {
					return (T) Arrays.copyOf((Object[]) element, n);
				}
			}
		// if no array try to clone
		} else {
			try {
				Method cloneMethod = clazz.getMethod("clone");
				
				return (T) cloneMethod.invoke(element);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// if element is not publicly cloneable then return element itself
				return element;
			}
		}
	}

}
