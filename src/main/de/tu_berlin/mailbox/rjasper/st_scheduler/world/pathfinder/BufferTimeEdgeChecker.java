package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;

import java.util.function.BiFunction;

import com.vividsolutions.jts.geom.Point;

public class BufferTimeEdgeChecker {

	private final double bufferDuration;

	private final BiFunction<Point, Point, Boolean> edgeChecker;

	public BufferTimeEdgeChecker(
		double bufferDuration,
		BiFunction<Point, Point, Boolean> edgeChecker)
	{
		this.bufferDuration = bufferDuration;
		this.edgeChecker = requireNonNull(edgeChecker, "edgeChecker");

		if (!Double.isFinite(bufferDuration) || bufferDuration < 0.0)
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
		Point p2 = point(s, t + bufferDuration);

		return edgeChecker.apply(p1, p2);
	}

}
