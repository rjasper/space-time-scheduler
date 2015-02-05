package world.util;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import jts.geom.immutable.ImmutablePoint;
import world.SpatialPath;
import world.SpatialPath.Vertex;

// TODO document
public class SpatialPathInterpolator extends AbstractPathInterpolator<
	ImmutablePoint,
	SpatialPath.Vertex,
	SpatialPath.Segment,
	SpatialPath>
{
	
	public SpatialPathInterpolator(
		SpatialPath spatialPath,
		PathVertexSeeker<SpatialPath.Vertex, SpatialPath.Segment, SpatialPath> seeker)
	{
		super(spatialPath, seeker);
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractPathInterpolator#onSpot(world.AbstractPath.Vertex)
	 */
	@Override
	protected ImmutablePoint onSpot(Vertex vertex) {
		return vertex.getPoint();
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractPathInterpolator#interpolate(double, world.AbstractPath.Vertex, world.AbstractPath.Vertex)
	 */
	@Override
	protected ImmutablePoint interpolate(double position, Vertex v1, Vertex v2) {
		double s1 = position(v1), s2 = position(v2);
		double alpha = (position - s1) / (s2 - s1);
		double x1 = v1.getX(), y1 = v1.getY(), x2 = v2.getX(), y2 = v2.getY();
		
		return immutablePoint(x1 + alpha*(x2-x1), y1 + alpha*(y2-y1));
	}

}
