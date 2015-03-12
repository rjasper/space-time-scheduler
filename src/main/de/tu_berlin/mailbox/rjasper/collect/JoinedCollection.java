package de.tu_berlin.mailbox.rjasper.collect;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class JoinedCollection<E> extends AbstractCollection<E> {

	public static <E> JoinedCollection<E> of(Collection<E> firstCollection, Collection<E> secondCollection) {
		return new JoinedCollection<>(firstCollection, secondCollection);
	}
	
	private final Collection<E> firstCollection;
	private final Collection<E> secondCollection;

	public JoinedCollection(Collection<E> firstCollection, Collection<E> secondCollection) {
		this.firstCollection = firstCollection;
		this.secondCollection = secondCollection;
	}

	@Override
	public Iterator<E> iterator() {
		return JoinedIterator.of(firstCollection.iterator(), secondCollection.iterator());
	}

	@Override
	public int size() {
		return firstCollection.size() + secondCollection.size();
	}

}
