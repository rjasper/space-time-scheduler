package jts.geom.immutable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

class ImmutableCoordinateSequenceFilter implements CoordinateSequenceFilter {

	private final CoordinateSequenceFilter filter;

	public ImmutableCoordinateSequenceFilter(CoordinateSequenceFilter filter) {
		this.filter = filter;
	}

	@Override
	public void filter(CoordinateSequence seq, int i) {
		Coordinate original = seq.getCoordinateCopy(i);
		
		filter.filter(seq, i);
		
		Coordinate coord = seq.getCoordinate(i);
		
		if (!coord.equals(original))
			throw new UnsupportedOperationException("cannot modify coordinate");
	}

	@Override
	public boolean isDone() {
		return filter.isDone();
	}

	@Override
	public boolean isGeometryChanged() {
		return filter.isGeometryChanged();
	}
	
}
