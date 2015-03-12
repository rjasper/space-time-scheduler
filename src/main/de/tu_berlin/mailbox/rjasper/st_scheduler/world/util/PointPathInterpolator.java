package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.PointPath;

// TODO document
public class PointPathInterpolator<
	V extends PointPath.Vertex>
extends AbstractInterpolator<Double, V, ImmutablePoint>
{

	public PointPathInterpolator(Seeker<Double, V> seeker) {
		super(seeker);
	}

	@Override
	protected ImmutablePoint onSpot(int index, Double position, V vertex) {
		return vertex.getPoint();
	}

	@Override
	protected ImmutablePoint interpolate(Double position, int idx1, Double p1, V v1, int idx2, Double p2, V v2) {
		double alpha = (position - p1) / (p2 - p1);
		double x1 = v1.getX(), y1 = v1.getY(), x2 = v2.getX(), y2 = v2.getY();
		
		return immutablePoint(x1 + alpha*(x2-x1), y1 + alpha*(y2-y1));
	}

}
