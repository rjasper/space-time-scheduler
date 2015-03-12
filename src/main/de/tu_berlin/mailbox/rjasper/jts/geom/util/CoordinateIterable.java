package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

/**
 * Provides an iterator over the coordinates of a {@link CoordinateSequence}.
 * 
 * @author Rico Jasper
 */
public class CoordinateIterable implements Iterable<Coordinate> {
	
	/**
	 * The coordinate sequence.
	 */
	private final CoordinateSequence sequence;
	
	/**
	 * Whether a copy of the sequence's coordinates shall be returned.
	 */
	private final boolean useCopy;

	/**
	 * Constructs a new iterable which can be iterated over the coordinates of
	 * the given sequence. Does iterate over the actual coordinates (no copies).
	 * 
	 * @param sequence
	 */
	public CoordinateIterable(CoordinateSequence sequence) {
		this(sequence, false);
	}

	/**
	 * Constructs a new iterable which can be iterated over the coordinates of
	 * the given sequence.
	 * 
	 * @param sequence
	 * @param useCopy whether to return coordinate copies.
	 */
	public CoordinateIterable(CoordinateSequence sequence, boolean useCopy) {
		this.sequence = sequence;
		this.useCopy = useCopy;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Coordinate> iterator() {
		return new Iterator<Coordinate>() {
			
			/**
			 * The position of the current coordinate in the sequence.
			 */
			private int i = 0;

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return i < sequence.size();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public Coordinate next() {
				if (useCopy)
					return sequence.getCoordinateCopy(i++);
				else
					return sequence.getCoordinate(i++);
			}
			
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#spliterator()
	 */
	@Override
	public Spliterator<Coordinate> spliterator() {
		return Spliterators.spliterator(iterator(), sequence.size(), NONNULL | ORDERED);
	}

}
