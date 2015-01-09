package world;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.*;
import static util.DurationConv.*;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jts.geom.factories.EnhancedGeometryBuilder;
import util.DurationConv;
import util.PathOperations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * A {@code DecomposedTrajectory} is a immutable trajectory which stores its
 * 3-dimensional path as two 2-dimensional components. The first component is
 * its spatial path (x-y). The second component is the arc time path (s-t).
 * </p>
 *
 * <p>
 * The decomposition enables to delay the composition of both components. It
 * also allows access to the spatial path component to calculate new
 * trajectories with another arc time path.
 * </p>
 *
 * <p>
 * The time component of the arc time path is represented as double values.
 * Since trajectories are required to provide the times of their vertices a base
 * time is required. The double representation stores the offset in seconds to
 * the base time.
 * </p>
 *
 * <p>
 * Some operations require to compose both components. A
 * {@link SimpleTrajectory} is used to represent the composed trajectory which
 * is cached once it is calculated.
 * </p>
 *
 * @author Rico Jasper
 */
public class DecomposedTrajectory extends CachedTrajectory {

	/**
	 * The base time.
	 */
	private final LocalDateTime baseTime;

	/**
	 * The spatial path component.
	 */
	private final List<Point> spatialPathComponent;

	/**
	 * The arc time component.
	 */
	private final List<Point> arcTimePathComponent;

	/**
	 * The cached composed trajectory.
	 */
	private transient SimpleTrajectory composedTrajectory = null;

	/**
	 * Constructs a new DecomposedTrajectory using the provided components.
	 * The baseTime is used to determine the vertex times of the arc time path.
	 *
	 * @param baseTime
	 * @param spatialPathComponent
	 * @param arcTimePathComponent
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if any of the following is true:
	 * <ul>
	 * <li>One of the components have the size "1".</li>
	 * <li>The components don't have the same euclidean length.</li>
	 * <li>The arcTimePathComponent is not causal (not increasing in time).</li>
	 * </ul>
	 */
	public DecomposedTrajectory(
		LocalDateTime baseTime,
		List<Point> spatialPathComponent,
		List<Point> arcTimePathComponent)
	{
		Objects.requireNonNull(baseTime, "baseTime");
		Objects.requireNonNull(spatialPathComponent, "spatialPathComponent");
		Objects.requireNonNull(arcTimePathComponent, "arcTimePathComponent");

		if (spatialPathComponent.size() == 1 || arcTimePathComponent.size() == 1)
			throw new IllegalArgumentException("illegal path component size");

		// TODO check components
		// * same euclidean length (tolerating error?)
		// * causal arc time path (time ordinates must be non-strictly increasing)
		
		// TODO initialize as unmodifiable

		this.baseTime = baseTime;
		this.spatialPathComponent = unmodifiableList( immutable(spatialPathComponent) );
		this.arcTimePathComponent = unmodifiableList( immutable(arcTimePathComponent) );
	}

	@Override
	public boolean isEmpty() {
		return spatialPathComponent.isEmpty();
	}

	/**
	 * @return {@code true} iff a composed trajectory is cached.
	 */
	public boolean isComposed() {
		return composedTrajectory != null;
	}

	/**
	 * @return the base time.
	 */
	public LocalDateTime getBaseTime() {
		return baseTime;
	}

	/**
	 * @return the spatial component (x-y).
	 */
	public List<Point> getSpatialPathComponent() {
		return spatialPathComponent;
	}

	/**
	 * @return the arc time component (s-t).
	 */
	public List<Point> getArcTimePathComponent() {
		return arcTimePathComponent;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getSpatialPath()
	 */
	@Override
	public List<Point> getSpatialPath() {
		return getComposedTrajectory().getSpatialPath();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTimes()
	 */
	@Override
	public List<LocalDateTime> getTimes() {
		return getComposedTrajectory().getTimes();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartLocation()
	 */
	@Override
	public Point getStartLocation() {
		if (isEmpty())
			return null;

		return getSpatialPathComponent().get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishLocation()
	 */
	@Override
	public Point getFinishLocation() {
		if (isEmpty())
			return null;

		List<Point> spatialPathComponent = getSpatialPathComponent();
		int n = spatialPathComponent.size();

		return spatialPathComponent.get(n-1);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartTime()
	 */
	@Override
	public LocalDateTime getStartTime() {
		if (isEmpty())
			return null;
		if (isComposed())
			return getComposedTrajectory().getStartTime();

		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		double t = arcTimePath.get(0).getY();
		Duration duration = DurationConv.ofSeconds(t);

		return baseTime.plus(duration);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishTime()
	 */
	@Override
	public LocalDateTime getFinishTime() {
		if (isEmpty())
			return null;
		if (isComposed())
			return getComposedTrajectory().getFinishTime();

		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		int n = arcTimePath.size();
		double t = arcTimePath.get(n-1).getY();
		Duration duration = DurationConv.ofSeconds(t);

		return baseTime.plus(duration);
	}

	@Override
	public List<Point> calcArcTimePath(LocalDateTime baseTime) {
		LocalDateTime ownBaseTime = getBaseTime();
		List<Point> arcTimePathComponent = getArcTimePathComponent();
		
		if (baseTime.equals(ownBaseTime))
			return new ArrayList<>();
		
		double offset = inSeconds( Duration.between(baseTime, ownBaseTime) );
		
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		return arcTimePathComponent.stream()
			.map(p -> geomBuilder.point(p.getX(), p.getY() + offset))
			.collect(toList());
	}

	/**
	 * @return the composed trajectory.
	 */
	public SimpleTrajectory getComposedTrajectory() {
		if (composedTrajectory == null)
			composedTrajectory = compose();

		return composedTrajectory;
	}

	/*
	 * (non-Javadoc)
	 * @see world.CachedTrajectory#calcLength()
	 */
	@Override
	protected double calcLength() {
		return PathOperations.length( getSpatialPathComponent() );
	}

	/*
	 * (non-Javadoc)
	 * @see world.CachedTrajectory#calcTrace()
	 */
	@Override
	protected Geometry calcTrace() {
		return PathOperations.calcTrace( getSpatialPathComponent() );
	}

	/**
	 * Calculates the composed trajectory.
	 *
	 * @return the composed trajectory.
	 */
	private SimpleTrajectory compose() {
		List<Point> spatialPath = getSpatialPathComponent();
		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		TrajectoryComposer builder = new TrajectoryComposer();

		builder.setSpatialPathComponent(spatialPath);
		builder.setArcTimePathComponent(arcTimePath);
		builder.setBaseTime(baseTime);

		builder.compose();

		return builder.getResultTrajectory();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getComposedTrajectory().toString();
	}

}
