package de.tu_berlin.mailbox.rjasper.util;

import java.util.Arrays;

/**
 * Provides static array sorting methods.
 */
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
