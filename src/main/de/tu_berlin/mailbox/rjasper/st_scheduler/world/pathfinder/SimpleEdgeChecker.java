package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import com.vividsolutions.jts.geom.Point;

public class SimpleEdgeChecker {

	private final double startArc;
	private final double finishArc;
	private final double startTime;
	private final double finishTime;
	private final double maxVelocity;

	public SimpleEdgeChecker(
		double startArc,
		double finishArc,
		double startTime,
		double finishTime,
		double maxVelocity)
	{
		this.startArc = checkDouble(startArc, "startArc");
		this.finishArc = checkDouble(finishArc, "finishArc");
		this.startTime = checkDouble(startTime, "startTime");
		this.finishTime = checkDouble(finishTime, "finishTime");
		this.maxVelocity = checkDouble(maxVelocity, "maxVelocity");

		if (startArc >= finishArc)
			throw new IllegalArgumentException("startArc >= finishArc");
		if (startTime >= finishTime)
			throw new IllegalArgumentException("startTime >= finishTime");
		if (maxVelocity <= 0.0)
			throw new IllegalArgumentException("illegal maxVelocity");
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
	 * <li>The maximum speed is not exceeded.</li>
	 * </ul>
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean check(Point from, Point to) {
		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();

		// if vertex is not on path
		if (s1 < startArc || s1 > finishArc || s2 < startArc || s2 > finishArc)
			return false;

		// if vertex is not within time window
		if (t1 < startTime || t1 > finishTime || t2 < startTime || t2 > finishTime)
			return false;

		// if 'from' happens after 'to'
		if (t1 > t2)
			return false;

		if (from.equals(to))
			return false;

		// if maximum speed is exceeded
		if (Math.abs((s2 - s1) / (t2 - t1)) > maxVelocity)
			return false;

		return true;
	}

}
