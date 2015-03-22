package de.tu_berlin.mailbox.rjasper.collect;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JoinedCollection<E> extends AbstractCollection<E> {

//	public static <E> JoinedCollection<E> of(Collection<E> firstCollection, Collection<E> secondCollection) {
//		return new JoinedCollection<>(firstCollection, secondCollection);
//	}

	public static <E> JoinedCollection<E> of(List<Collection<E>> collections) {
		return new JoinedCollection<>(collections);
	}

	private final List<Collection<E>> collections;

//	private final Collection<E> firstCollection;
//	private final Collection<E> secondCollection;

//	public JoinedCollection(Collection<E> firstCollection, Collection<E> secondCollection) {
//		this.firstCollection = firstCollection;
//		this.secondCollection = secondCollection;
//	}

	public JoinedCollection(List<Collection<E>> collections) {
		this.collections = requireNonNull(collections);
	}

	@Override
	public Iterator<E> iterator() {
		List<Iterator<E>> its = collections.stream()
			.map(Collection::iterator)
			.collect(toList());

		return JoinedIterator.of(its);
	}

	@Override
	public int size() {
		return collections.stream()
			.mapToInt(Collection::size)
			.sum();
	}

}
