package jts.geom.immutable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * An immutable version of a {@code CoordinateFilter}. It checks if the filtered
 * {@code Coordinate} was altered by the underlying filter. Throws an
 * {@link UnsupportedOperationException} if the coordinate was altered.
 * 
 * @author Rico
 */
class ImmutableCoordinateFilter implements CoordinateFilter {
	
	/**
	 * The underlying filter.
	 */
	private final CoordinateFilter filter;
	
	/**
	 * Constructs a new {@code ImmutableCoordinateFilter}. Uses the given filter
	 * to be applied as actual filter function.
	 * 
	 * @param filter the underlying filter
	 */
	public ImmutableCoordinateFilter(CoordinateFilter filter) {
		this.filter = filter;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateFilter#filter(com.vividsolutions.jts.geom.Coordinate)
	 */
	@Override
	public void filter(Coordinate coord) {
		Coordinate original = (Coordinate) coord.clone();
		
		filter.filter(coord);
		
		if (!coord.equals(original))
			throw new UnsupportedOperationException("cannot modify coordinate");
	}

}
