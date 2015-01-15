package jts.geom.immutable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

class ImmutableCoordinateFilter implements CoordinateFilter {
	
	private final CoordinateFilter filter;
	
	public ImmutableCoordinateFilter(CoordinateFilter filter) {
		this.filter = filter;
	}

	@Override
	public void filter(Coordinate coord) {
		Coordinate original = (Coordinate) coord.clone();
		
		filter.filter(coord);
		
		if (!coord.equals(original))
			throw new UnsupportedOperationException("cannot modify coordinate");
	}

}
