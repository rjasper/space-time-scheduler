package util;

import java.util.Arrays;

// TODO document
public final class ArraysSort {
	
	/**
	 * Sorts the given array in place.
	 * 
	 * @param array
	 * @return the array.
	 */
	public static <T> T[] sort(T[] array) {
		Arrays.sort(array);
		
		return array;
	}

}
