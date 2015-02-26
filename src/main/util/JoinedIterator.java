package util;

import java.util.Iterator;

public class JoinedIterator<E> implements Iterator<E> {
	
	public static <E> JoinedIterator<E> of(Iterator<E> firstIterator, Iterator<E> secondIterator) {
		return new JoinedIterator<>(firstIterator, secondIterator);
	}
	
	private boolean first;
	
	private final Iterator<E> firstIterator;
	private final Iterator<E> secondIterator;

	public JoinedIterator(Iterator<E> firstIterator, Iterator<E> secondIterator) {
		this.firstIterator = firstIterator;
		this.secondIterator = secondIterator;
		
		this.first = firstIterator.hasNext();
	}

	@Override
	public boolean hasNext() {
		return first ? firstIterator.hasNext() : secondIterator.hasNext();
	}

	@Override
	public E next() {
		E next;
		if (first) {
			next = firstIterator.next();
			first = firstIterator.hasNext();
		} else {
			next = secondIterator.next();
		}
		
		return next;
	}

}
