package de.tu_berlin.mailbox.rjasper.collect;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class JoinedIterator<E> implements Iterator<E> {

//	public static <E> JoinedIterator<E> of(Iterator<E> firstIterator, Iterator<E> secondIterator) {
//		return new JoinedIterator<>(firstIterator, secondIterator);
//	}

	public static <E> JoinedIterator<E> of(List<Iterator<E>> iterators) {
		return new JoinedIterator<>(iterators);
	}

//	private boolean first;
//
//	private final Iterator<E> firstIterator;
//	private final Iterator<E> secondIterator;

	private final Queue<Iterator<E>> iterators;
	private Iterator<E> currentIterator = null;
	private E nextElement = null;

//	public JoinedIterator(Iterator<E> firstIterator, Iterator<E> secondIterator) {
//		this.firstIterator = firstIterator;
//		this.secondIterator = secondIterator;
//
//		this.first = firstIterator.hasNext();
//	}

	public JoinedIterator(List<Iterator<E>> iterators) {
		requireNonNull(iterators, "iterators");

		this.iterators = new LinkedList<>(iterators);

		nextIterator();
		nextElement();
	}

	@Override
	public boolean hasNext() {
		return nextElement != null;
	}

	@Override
	public E next() {
		if (!hasNext())
			throw new NoSuchElementException();

		E currentElement = nextElement;

		nextElement();

		return currentElement;
	}

	private void nextIterator() {
		do {
			if (iterators.isEmpty()) {
				currentIterator = null;
				return;
			}

			currentIterator = iterators.poll();

		} while (!currentIterator.hasNext());
	}

	private void nextElement() {
		if (currentIterator == null) {
			nextElement = null;
		} else {
			nextElement = currentIterator.next();

			if (!currentIterator.hasNext())
				nextIterator();
		}
	}

}
