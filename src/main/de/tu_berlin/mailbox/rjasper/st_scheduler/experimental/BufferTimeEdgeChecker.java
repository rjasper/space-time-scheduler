package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;

import java.util.function.BiFunction;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class BufferTimeEdgeChecker {

	private final double bufferTime;

	private final BiFunction<Point, Point, Boolean> edgeChecker;

	public BufferTimeEdgeChecker(
		double bufferTime,
		BiFunction<Point, Point, Boolean> edgeChecker)
	{
		this.bufferTime = bufferTime;
		this.edgeChecker = requireNonNull(edgeChecker, "edgeChecker");

		if (!Double.isFinite(bufferTime) || bufferTime < 0.0)
			throw new IllegalArgumentException("illegal bufferTime");
	}

	/**
	 * Checks if the vertex is collision free at its arc for at least
	 * {@link #bufferDuration} amount of time.
	 * @param vertex
	 *
	 * @return {@code true} if the vertex is collision free.
	 */
	public boolean check(Point vertex) {
		double s = vertex.getX(), t = vertex.getY();

		Point p1 = vertex;
		Point p2 = point(s, t + bufferTime);

		return edgeChecker.apply(p1, p2);
	}

}
