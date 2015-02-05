package world.util;

import world.ArcTimePath;
import world.ArcTimePath.Vertex;

// TODO document
public class ArcTimePathInterpolator extends AbstractPathInterpolator<
	Double,
	ArcTimePath.Vertex,
	ArcTimePath>
{

	public ArcTimePathInterpolator(
		ArcTimePath path,
		PathVertexSeeker<ArcTimePath.Vertex> seeker)
	{
		super(path, seeker);
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractPathInterpolator#onSpot(world.AbstractPath.Vertex)
	 */
	@Override
	protected Double onSpot(Vertex vertex) {
		return vertex.getX();
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractPathInterpolator#interpolate(double, world.AbstractPath.Vertex, world.AbstractPath.Vertex)
	 */
	@Override
	protected Double interpolate(double position, Vertex v1, Vertex v2) {
		double s1 = onSpot(v1), s2 = onSpot(v2);
		double t1 = position(v1), t2 = position(v2);
		double alpha = (position - t1) / (t2 - t1);
		
		return s1 + alpha*(s2-s1);
	}

}
