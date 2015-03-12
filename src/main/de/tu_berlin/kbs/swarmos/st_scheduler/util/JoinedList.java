package de.tu_berlin.kbs.swarmos.st_scheduler.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class JoinedList<E> extends AbstractList<E> {
	
	public static <E> JoinedList<E> of(List<E> firstList, List<E> secondList) {
		return new JoinedList<>(firstList, secondList);
	}
	
	private final List<E> firstList;
	private final List<E> secondList;
	
	public JoinedList(List<E> firstList, List<E> secondList) {
		this.firstList = firstList;
		this.secondList = secondList;
	}

	@Override
	public E get(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		
		if (index < firstList.size())
			return firstList.get(index);
		else
			return secondList.get(index - firstList.size());
	}

	@Override
	public Iterator<E> iterator() {
		return JoinedIterator.of(firstList.iterator(), secondList.iterator());
	}

	@Override
	public int size() {
		return firstList.size() + secondList.size();
	}

}
