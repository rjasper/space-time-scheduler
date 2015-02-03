package world;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.DurationConv;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

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
public class DecomposedTrajectory implements Trajectory {
	
	/**
	 * An empty {@code DecomposedTrajectory}.
	 */
	private static final DecomposedTrajectory EMPTY =
		new DecomposedTrajectory(LocalDateTime.MIN, SpatialPath.empty(), ArcTimePath.empty());
	
	/**
	 * @return an empty {@code DecomposedTrajectory}.
	 */
	public static DecomposedTrajectory empty() {
		return EMPTY;
	}

	/**
	 * The base time.
	 */
	private final LocalDateTime baseTime;

	/**
	 * The spatial path component.
	 */
	private final SpatialPath spatialPathComponent;

	/**
	 * The arc time component.
	 */
	private final ArcTimePath arcTimePathComponent;

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
		SpatialPath spatialPathComponent,
		ArcTimePath arcTimePathComponent)
	{
		Objects.requireNonNull(baseTime, "baseTime");
		Objects.requireNonNull(spatialPathComponent, "spatialPathComponent");
		Objects.requireNonNull(arcTimePathComponent, "arcTimePathComponent");

		// TODO no tolerance might be too strict
		if (spatialPathComponent.length() < arcTimePathComponent.maxArc())
			throw new IllegalArgumentException("path components' incompatible");
		
		this.baseTime = baseTime;
		this.spatialPathComponent = spatialPathComponent;
		this.arcTimePathComponent = arcTimePathComponent;
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
	 * @return the composed trajectory.
	 */
	public SimpleTrajectory getComposedTrajectory() {
		if (composedTrajectory == null)
			composedTrajectory = compose();
	
		return composedTrajectory;
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
	public SpatialPath getSpatialPathComponent() {
		return spatialPathComponent;
	}

	/**
	 * @return the arc time component (s-t).
	 */
	public ArcTimePath getArcTimePathComponent() {
		return arcTimePathComponent;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getSpatialPath()
	 */
	@Override
	public SpatialPath getSpatialPath() {
		return getComposedTrajectory().getSpatialPath();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTimes()
	 */
	@Override
	public ImmutableList<LocalDateTime> getTimes() {
		return getComposedTrajectory().getTimes();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartLocation()
	 */
	@Override
	public ImmutablePoint getStartLocation() {
		if (isEmpty())
			return null;

		return getSpatialPathComponent().get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishLocation()
	 */
	@Override
	public ImmutablePoint getFinishLocation() {
		if (isEmpty())
			return null;

		SpatialPath spatialPathComponent = getSpatialPathComponent();
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

		ArcTimePath arcTimePath = getArcTimePathComponent();
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

		ArcTimePath arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		int n = arcTimePath.size();
		double t = arcTimePath.get(n-1).getY();
		Duration duration = DurationConv.ofSeconds(t);

		return baseTime.plus(duration);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getDuration()
	 */
	@Override
	public Duration getDuration() {
		return getArcTimePathComponent().duration();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#length()
	 */
	@Override
	public double length() {
		return getArcTimePathComponent().length();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#trace()
	 */
	@Override
	public Geometry trace() {
		return getSpatialPathComponent().trace();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#subTrajectory(java.time.LocalDateTime, java.time.LocalDateTime)
	 */
	@Override
	public DecomposedTrajectory subTrajectory(LocalDateTime startTime, LocalDateTime finishTime) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#calcArcTimePath(java.time.LocalDateTime)
	 */
	@Override
	public ArcTimePath calcArcTimePath(LocalDateTime baseTime) {
		LocalDateTime ownBaseTime = getBaseTime();
		ArcTimePath arcTimePathComponent = getArcTimePathComponent();
		
		if (baseTime.equals(ownBaseTime))
			return arcTimePathComponent;
		
		double offset = inSeconds( Duration.between(baseTime, ownBaseTime) );
		
		ImmutableList<ImmutablePoint> vertices = arcTimePathComponent.getPoints().stream()
			.map(p -> immutablePoint(p.getX(), p.getY() + offset))
			.collect(toImmutableList());
		
		return new ArcTimePath(vertices);
	}

	/**
	 * Calculates the composed trajectory.
	 *
	 * @return the composed trajectory.
	 */
	private SimpleTrajectory compose() {
		SpatialPath spatialPath = getSpatialPathComponent();
		ArcTimePath arcTimePath = getArcTimePathComponent();
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
