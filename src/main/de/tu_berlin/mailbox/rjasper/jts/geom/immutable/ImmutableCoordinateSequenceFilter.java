package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

/**
 * An immutable version of a {@code CoordinateSequenceFilter}. It checks if the
 * filtered {@code CoordinateSequence} was altered by the underlying filter.
 * Throws an {@link UnsupportedOperationException} if the sequence was.
 * altered.
 * 
 * @author Rico
 */
class ImmutableCoordinateSequenceFilter implements CoordinateSequenceFilter {

	/**
	 * The underlying filter.
	 */
	private final CoordinateSequenceFilter filter;
	
	/**
	 * The original sequence. Stores the original unmodified sequence.
	 */
	private CoordinateSequence original = null;
	
	/**
	 * The actual sequence.
	 */
	private CoordinateSequence actual = null;

	/**
	 * Constructs a new {@code CoordinateSequenceFilter}. Uses the given filter
	 * to be applied as actual filter function.
	 * 
	 * @param filter the underlying filter
	 */
	public ImmutableCoordinateSequenceFilter(CoordinateSequenceFilter filter) {
		this.filter = filter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFilter#filter(com.vividsolutions.jts.geom.CoordinateSequence, int)
	 */
	@Override
	public void filter(CoordinateSequence seq, int i) {
		if (original == null) {
			original = (CoordinateSequence) seq.clone();
			actual = seq;
		}
		
		filter.filter(seq, i);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFilter#isDone()
	 */
	@Override
	public boolean isDone() {
		return filter.isDone();
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFilter#isGeometryChanged()
	 */
	@Override
	public boolean isGeometryChanged() {
		if (filter.isGeometryChanged())
			throw new UnsupportedOperationException("cannot modify coordinate");
		if (original != null && !original.equals(actual))
			throw new UnsupportedOperationException("cannot modify coordinate");
		
		return false;
	}
	
}
