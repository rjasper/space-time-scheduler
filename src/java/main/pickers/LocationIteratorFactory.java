package pickers;

import java.util.Iterator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class LocationIteratorFactory implements Iterable<Point> {
	
	private Geometry space;
	
	private int maxPicks;

	public LocationIteratorFactory(Geometry space, int maxPicks) {
		this.space = space;
		this.maxPicks = maxPicks;
	}

	@Override
	public Iterator<Point> iterator() {
		return new LocationIterator(space, maxPicks);
	}

}
