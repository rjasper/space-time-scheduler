package world;

import com.vividsolutions.jts.geom.Geometry;

public abstract class CachedTrajectory implements Trajectory {

	private transient double length = Double.NaN;

	private transient Geometry trace = null;

	@Override
	public double getLength() {
		if (Double.isNaN(length))
			length = calcLength();

		return length;
	}

	protected abstract double calcLength();

	@Override
	public Geometry getTrace() {
		if (trace == null)
			trace = calcTrace();

		return trace;
	}

	protected abstract Geometry calcTrace();

}
