package world;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.DurationConv;
import world.util.DoubleSubPointPathOperation;
import world.util.TrajectoryComposer;

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
	 * </ul>
	 */
	public DecomposedTrajectory(
		LocalDateTime baseTime,
		SpatialPath spatialPathComponent,
		ArcTimePath arcTimePathComponent)
	{
		Objects.requireNonNull(baseTime, "baseTime");
		
//		checkComponents(spatialPathComponent, arcTimePathComponent);
		Objects.requireNonNull(spatialPathComponent, "spatialPathComponent");
		Objects.requireNonNull(arcTimePathComponent, "arcTimePathComponent");
		
		// both components must either be empty or non-empty
		if (spatialPathComponent.isEmpty() != arcTimePathComponent.isEmpty())
			throw new IllegalArgumentException("incompatible path components sizes");
		
		if (!arcTimePathComponent.isEmpty()) {
			// TODO no tolerance might be too strict
			if (spatialPathComponent.length() < arcTimePathComponent.maxArc())
				throw new IllegalArgumentException("arcTimePath includes arcs larger than spatialPath");
		}
		
		this.baseTime = baseTime;
		this.spatialPathComponent = spatialPathComponent;
		this.arcTimePathComponent = arcTimePathComponent;
	}
	
//	public static void checkComponents(
//		SpatialPath spatialPathComponent,
//		ArcTimePath arcTimePathComponent)
//	{
//		Objects.requireNonNull(spatialPathComponent, "spatialPathComponent");
//		Objects.requireNonNull(arcTimePathComponent, "arcTimePathComponent");
//		
//		// both components must either be empty or non-empty
//		if (spatialPathComponent.isEmpty() != arcTimePathComponent.isEmpty())
//			throw new IllegalArgumentException("incompatible path components sizes");
//		
//		if (!arcTimePathComponent.isEmpty()) {
//			// TODO no tolerance might be too strict
//			if (spatialPathComponent.length() < arcTimePathComponent.maxArc())
//				throw new IllegalArgumentException("arcTimePath includes arcs larger than spatialPath");
//		}
//	}

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
	 * The cached composed trajectory.
	 */
	private transient SimpleTrajectory composedTrajectory = null;

	/**
	 * Calculates the composed trajectory.
	 *
	 * @return the composed trajectory.
	 */
	private SimpleTrajectory compose() {
//		SpatialPath spatialPath = getSpatialPathComponent();
//		ArcTimePath arcTimePath = getArcTimePathComponent();
//		LocalDateTime baseTime = getBaseTime();
//	
//		TrajectoryComposer builder = new TrajectoryComposer();
//	
//		builder.setSpatialPathComponent(spatialPath);
//		builder.setArcTimePathComponent(arcTimePath);
//		builder.setBaseTime(baseTime);
//	
//		builder.compose();
//	
//		return builder.getResultTrajectory();
		
		return TrajectoryComposer.compose(this);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#getVertex(int)
	 */
	@Override
	public Vertex getVertex(int index) {
		return getComposedTrajectory().getVertex(index);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#getSegment(int)
	 */
	@Override
	public Segment getSegment(int index) {
		return getComposedTrajectory().getSegment(index);
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
	
	/**
	 * Caches the start location.
	 */
	private transient ImmutablePoint startLocation = null;

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartLocation()
	 */
	@Override
	public ImmutablePoint getStartLocation() {
		if (isEmpty())
			return null;
		
		if (startLocation == null) {
			double s = getArcTimePathComponent().getFirstPoint().getX();
			SpatialPath xy = getSpatialPathComponent();

			// TODO don't interpolate if composed
			
			startLocation = s == 0.0
				? xy.getFirstPoint()
				: xy.interpolateLocation(s);
		}
		
		return startLocation;
	}
	
	/**
	 * Caches the finish location.
	 */
	private transient ImmutablePoint finishLocation = null;

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishLocation()
	 */
	@Override
	public ImmutablePoint getFinishLocation() {
		if (isEmpty())
			return null;
		
		if (finishLocation == null) {
			ArcTimePath st = getArcTimePathComponent();
			SpatialPath xy = getSpatialPathComponent();
			double s = getArcTimePathComponent().getLastPoint().getX();
			
			// TODO don't interpolate if composed
			
			finishLocation = s == st.maxArc()
				? xy.getLastPoint()
				: xy.interpolateLocation(s);
		}

		return finishLocation;
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

		double t = arcTimePath.getPoint(0).getY();
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
		double t = arcTimePath.getPoint(n-1).getY();
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
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public Iterator<Vertex> vertexIterator() {
		return getComposedTrajectory().vertexIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public Iterator<Segment> segmentIterator() {
		return getComposedTrajectory().segmentIterator();
	}

	/**
	 * @return
	 * @see world.SimpleTrajectory#size()
	 */
	@Override
	public int size() {
		return getComposedTrajectory().size();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#length()
	 */
	@Override
	public double length() {
		return getArcTimePathComponent().length();
	}
	
	private transient Geometry trace = null;

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#trace()
	 */
	@Override
	public Geometry trace() {
		if (isEmpty())
			return immutableLineString();
		
		if (trace == null) {
			ArcTimePath stComponent = getArcTimePathComponent();
			SpatialPath xyComponent = getSpatialPathComponent();
			
			if (stComponent.minArc() == 0.0 &&
				stComponent.maxArc() == xyComponent.length())
			{
				trace = xyComponent.trace();
			} else {
				SpatialPath subXyComponent = DoubleSubPointPathOperation.subPath(
					xyComponent,
					SpatialPath.Vertex::getArc,
					SpatialPath::new,
					stComponent.minArc(), stComponent.maxArc());
				
				trace = subXyComponent.trace();
			}
		}
		
		return trace;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#interpolateLocation(java.time.LocalDateTime)
	 */
	@Override
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		if (isEmpty())
			throw new NoSuchElementException("trajectory is empty");
		if (time.isBefore(getStartTime()) || time.isAfter(getFinishTime()))
			throw new IllegalArgumentException("time must be covered by this trajectory");
		
		// TODO short cut if time is start or finish time
		
		if (isComposed()) {
			return getComposedTrajectory().interpolateLocation(time);
		} else {
			double t = inSeconds(Duration.between(getBaseTime(), time));
			double s = getArcTimePathComponent().interpolateArc(t);
			
			return getSpatialPathComponent().interpolateLocation(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#concat(world.Path)
	 */
	@Override
	public Trajectory concat(Path<? extends Vertex, ? extends Segment> other) {
		return getComposedTrajectory().concat(other);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#subPath(double, double)
	 */
	@Override
	public SimpleTrajectory subPath(double startPosition, double finishPosition) {
		// TODO decompose trajectory
		
		return getComposedTrajectory().subPath(startPosition, finishPosition);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#subPath(java.time.LocalDateTime, java.time.LocalDateTime)
	 */
	@Override
	public DecomposedTrajectory subPath(LocalDateTime startTime, LocalDateTime finishTime) {
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTimei");
		
		if (isEmpty())
			throw new NoSuchElementException("trajectory is empty");
		if (startTime.compareTo(finishTime) >= 0)
			throw new IllegalArgumentException("invalid time interval");
		
		if (startTime .compareTo(getStartTime ()) == 0 &&
			finishTime.compareTo(getFinishTime()) == 0)
		{
			return this;
		}
		
		LocalDateTime baseTime = getBaseTime();
		double t1 = inSeconds(Duration.between(baseTime, startTime ));
		double t2 = inSeconds(Duration.between(baseTime, finishTime));
		
		SpatialPath xyComponent = getSpatialPathComponent();
		ArcTimePath stComponent = getArcTimePathComponent();
		
		ArcTimePath stSubComponent = DoubleSubPointPathOperation.subPath(stComponent,
			ArcTimePath.Vertex::getY,
			ArcTimePath::new,
			t1, t2);
		
		return new DecomposedTrajectory(baseTime, xyComponent, stSubComponent);
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getComposedTrajectory().toString();
	}

}
