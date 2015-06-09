package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import com.vividsolutions.jts.geom.Point;

/**
 * Checks if two points can be connected without violating the maximum velocity.
 *
 * @author Rico Jasper
 */
public class VelocityEdgeChecker {

	private final double maxVelocity;

	public VelocityEdgeChecker(double maxVelocity) {
		if (!Double.isFinite(maxVelocity))
			throw new IllegalArgumentException("illegal maxVelocity");

		this.maxVelocity = maxVelocity;
	}
	public boolean check(Point from, Point to) {
		if (from.equalsTopo(to))
			return true;

		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();

		return Math.abs((s2 - s1) / (t2 - t1)) <= maxVelocity;
	}

}
