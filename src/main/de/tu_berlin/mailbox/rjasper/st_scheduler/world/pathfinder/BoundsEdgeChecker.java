package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import com.vividsolutions.jts.geom.Point;

public class BoundsEdgeChecker {

	private final double minArc;
	private final double maxArc;
	private final double minTime;
	private final double maxTime;

	public BoundsEdgeChecker(
		double minArc, double maxArc, double minTime, double maxTime)
	{
		this.minArc = checkDouble(minArc, "startArc");
		this.maxArc = checkDouble(maxArc, "finishArc");
		this.minTime = checkDouble(minTime, "startTime");
		this.maxTime = checkDouble(maxTime, "finishTime");

		if (minArc > maxArc)
			throw new IllegalArgumentException("startArc > finishArc");
		if (minTime > maxTime)
			throw new IllegalArgumentException("startTime > finishTime");
	}

	private static double checkDouble(double value, String name) {
		if (!Double.isFinite(value))
			throw new IllegalArgumentException("illegal " + name);

		return value;
	}

	/**
	 * <p>
	 * Checks if two nodes can be connects. The following conditions have to be
	 * met:
	 * </p>
	 *
	 * <ul>
	 * <li>Both vertices' arc-ordinates are within [minArc, maxArc].</li>
	 * <li>Both vertices' time-ordinates are within [minTime, maxTime].</li>
	 * <li>The first vertex' time is before the second vertex' time.</li>
	 * </ul>
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean check(Point from, Point to) {
		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();

		// if vertex is not on path
		if (s1 < minArc || s1 > maxArc || s2 < minArc || s2 > maxArc)
			return false;

		// if vertex is not within time window
		if (t1 < minTime || t1 > maxTime || t2 < minTime || t2 > maxTime)
			return false;

		// if 'from' happens after 'to'
		if (t1 > t2)
			return false;

		return true;
	}

}
