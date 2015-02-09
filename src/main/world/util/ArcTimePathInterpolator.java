package world.util;

import world.ArcTimePath;
import world.ArcTimePath.Vertex;

// TODO document
public class ArcTimePathInterpolator extends AbstractInterpolator<Double, ArcTimePath.Vertex, Double> {

	public ArcTimePathInterpolator(Seeker<Double, ? extends Vertex> seeker) {
		super(seeker);
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractInterpolator#onSpot(java.lang.Object)
	 */
	@Override
	protected Double onSpot(int index, Double position, Vertex vertex) {
		return vertex.getX();
	}

	/*
	 * (non-Javadoc)
	 * @see world.util.AbstractInterpolator#interpolate(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected Double interpolate(Double position, int idx1, Double t1, Vertex v1, int idx2, Double t2, Vertex v2) {
		double s1 = v1.getX(), s2 = v2.getX();
		double alpha = (position - t1) / (t2 - t1);
		
		return s1 + alpha*(s2-s1);
	}

}
