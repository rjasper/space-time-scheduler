package jts.geom.util;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateIterable implements Iterable<Coordinate> {
	
	private final CoordinateSequence sequence;
	
	private final boolean useCopy;
	
	private int i = 0;

	public CoordinateIterable(CoordinateSequence sequence) {
		this(sequence, false);
	}

	public CoordinateIterable(CoordinateSequence sequence, boolean useCopy) {
		this.sequence = sequence;
		this.useCopy = useCopy;
	}

	@Override
	public Iterator<Coordinate> iterator() {
		return new Iterator<Coordinate>() {

			@Override
			public boolean hasNext() {
				return i == sequence.size();
			}

			@Override
			public Coordinate next() {
				if (useCopy)
					return sequence.getCoordinateCopy(i++);
				else
					return sequence.getCoordinate(i++);
			}
			
		};
	}
	
	@Override
	public Spliterator<Coordinate> spliterator() {
		return Spliterators.spliterator(iterator(), sequence.size(), NONNULL | ORDERED);
	}

}
