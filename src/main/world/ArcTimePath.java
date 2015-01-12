package world;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.IteratorIterable;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Point;

public class ArcTimePath extends Path {

	public ArcTimePath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);
	}

	public ArcTimePath(List<Point> vertices) {
		super(vertices);
	}

	@Override
	protected void checkVertices(List<? extends Point> vertices) {
		super.checkVertices(vertices);
		
		// check arc ordinates
		// arcs have to be equal or greater than 0
		
		boolean nonNegativeArcs = vertices.stream()
			.map(Point::getX) // arc ordinate
			.allMatch(s -> s >= 0);
		
		if (!nonNegativeArcs) // e.g. negative arcs
			throw new IllegalArgumentException("path has negative arc values");
		
		// check time ordinates
		// times have to be strictly increasing
		
		Iterator<Double> it = vertices.stream()
			.map(Point::getY) // time ordinate
			.iterator();
		
		boolean isOrdered = Ordering.natural()
			.isStrictlyOrdered(new IteratorIterable<>(it));
		
		if (!isOrdered)
			throw new IllegalArgumentException("path is not causal");
	}

}
