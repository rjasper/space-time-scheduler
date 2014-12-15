package world;

import java.time.Duration;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A {@code CachedTrajectory} caches its length and its trace once they were
 * calculated.
 *
 * @author Rico Jasper
 *
 */
public abstract class CachedTrajectory implements Trajectory {

	/**
	 * The cached length.
	 */
	private transient double length = Double.NaN;

	/**
	 * The cached trace.
	 */
	private transient Geometry trace = null;

	/**
	 * The cached duration.
	 */
	private transient Duration duration = null;

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getLength()
	 */
	@Override
	public double getLength() {
		if (Double.isNaN(length))
			length = calcLength();

		return length;
	}

	/**
	 * Calculates the trajectory's spatial length.
	 *
	 * @return the length.
	 */
	protected abstract double calcLength();

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTrace()
	 */
	@Override
	public Geometry getTrace() {
		if (trace == null)
			trace = calcTrace();

		return trace;
	}

	/**
	 * Calculates the trajectory's spatial trace.
	 *
	 * @return the trace.
	 */
	protected abstract Geometry calcTrace();

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getDuration()
	 */
	@Override
	public Duration getDuration() {
		if (duration == null)
			duration = Duration.between(getStartTime(), getFinishTime());

		return duration;
	}

}
