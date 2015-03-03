package world;

import static common.collect.ImmutablesCollectors.*;
import static java.lang.Math.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.TimeConv.*;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.TimeConv;
import world.util.BinarySearchSeeker;
import world.util.DoubleSubPointPathOperation;
import world.util.Seeker;
import world.util.Seeker.SeekResult;
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

	@Override
	public boolean isEmpty() {
		return spatialPathComponent.isEmpty();
	}

	/**
	 * @return {@code true} iff a composed trajectory is cached.
	 */
	public boolean isComposed() {
		return composedTrajectoryCache != null;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#getVertex(int)
	 */
	@Override
	public Vertex getVertex(int index) {
		return composed().getVertex(index);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#getSegment(int)
	 */
	@Override
	public Segment getSegment(int index) {
		return composed().getSegment(index);
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
		return composed().getSpatialPath();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTimes()
	 */
	@Override
	public ImmutableList<LocalDateTime> getTimes() {
		return composed().getTimes();
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
			throw new NoSuchElementException("trajectory is empty");
		
		if (startLocation == null) {
			if (isComposed()) {
				startLocation = composed().getStartLocation();
			} else {
				double s = getArcTimePathComponent().getFirstPoint().getX();
				SpatialPath xy = getSpatialPathComponent();
	
				startLocation = xy.interpolateLocation(s);
			}
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
			throw new NoSuchElementException("trajectory is empty");
		
		if (finishLocation == null) {
			if (isComposed()) {
				finishLocation = composed().getFinishLocation();
			} else {
				SpatialPath xy = getSpatialPathComponent();
				double s = getArcTimePathComponent().getLastPoint().getX();
				
				finishLocation = xy.interpolateLocation(s);
			}
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
			throw new NoSuchElementException("trajectory is empty");
		if (isComposed())
			return composed().getStartTime();

		ArcTimePath arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		double t = arcTimePath.getPoint(0).getY();

		return TimeConv.secondsToTime(t, baseTime);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishTime()
	 */
	@Override
	public LocalDateTime getFinishTime() {
		if (isEmpty())
			throw new NoSuchElementException("trajectory is empty");
		if (isComposed())
			return composed().getFinishTime();

		ArcTimePath arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();

		int n = arcTimePath.size();
		double t = arcTimePath.getPoint(n-1).getY();
		
		return TimeConv.secondsToTime(t, baseTime);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getDuration()
	 */
	@Override
	public Duration duration() {
		return getArcTimePathComponent().duration();
	}

	@Override
	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		// short cut
		if (isComposed())
			return composed().isStationary(from, to);

		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to  , "to"  );
		
		if (isEmpty())
			throw new IllegalStateException("trajectory is empty");
		
		if (!from.isBefore(to) ||
			!from.isBefore(getFinishTime()) ||
			!to.isAfter(getStartTime()))
		{
			throw new IllegalArgumentException("invalid interval");
		}
		
		// determine min and max arc between 'from' and 'to'
		
		double minArc, maxArc;
		if (from.equals(getStartTime()) && to.equals(getFinishTime())) {
			minArc = arcTimePathComponent.minArc();
			maxArc = arcTimePathComponent.maxArc();
		} else {
			double fromD = durationToSeconds(Duration.between(baseTime, from));
			double toD   = durationToSeconds(Duration.between(baseTime, to  ));
			
			Seeker<Double, ArcTimePath.Vertex> timeSeeker = new BinarySearchSeeker<>(
				arcTimePathComponent::getVertex,
				PointPath.Vertex::getY,
				arcTimePathComponent.size());
			
			SeekResult<Double, ArcTimePath.Vertex> stStart =
				timeSeeker.seekFloor(fromD);
			SeekResult<Double, ArcTimePath.Vertex> stFinish =
				timeSeeker.seekFloor(toD);
			
			minArc = Double.POSITIVE_INFINITY;
			maxArc = Double.NEGATIVE_INFINITY;
			
			// determine if there are even lower/greater arc between from and to
			for (int i = stStart.getIndex(); i <= stFinish.getIndex(); ++i) {
				double arc = arcTimePathComponent.getPoint(i).getX();
				
				minArc = min(minArc, arc);
				maxArc = max(maxArc, arc);
			}
		}
		
		// interpolate min and max arc locations

		Seeker<Double, SpatialPath.Vertex> arcSeeker = new BinarySearchSeeker<>(
			spatialPathComponent::getVertex,
			SpatialPath.Vertex::getArc,
			spatialPathComponent.size());
		
		SeekResult<Double, SpatialPath.Vertex> xyStart =
			arcSeeker.seekFloor(minArc);
		SeekResult<Double, SpatialPath.Vertex> xyFinish=
			arcSeeker.seekCeiling(maxArc);
		
		ImmutablePoint floorMinArcLocation = xyStart.get().getPoint();
		ImmutablePoint ceilMaxArcLocation = xyFinish.get().getPoint();

		// check location at 'minArc' and 'maxArc'
		
		if (!floorMinArcLocation.equals(ceilMaxArcLocation))
			return false;
		
		// check locations between 'minArc' and 'maxArc'
		
		for (int i = xyStart.getIndex(); i <= xyFinish.getIndex(); ++i) {
			ImmutablePoint location = spatialPathComponent.getPoint(i);
			
			if (!location.equals(floorMinArcLocation))
				return false;
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public Iterator<Vertex> vertexIterator() {
		return composed().vertexIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public Iterator<Segment> segmentIterator() {
		return composed().segmentIterator();
	}

	/**
	 * The cached composed trajectory.
	 */
	private transient SoftReference<SimpleTrajectory> composedTrajectoryCache = null;

	/**
	 * @return the composed trajectory.
	 */
	public SimpleTrajectory composed() {
		SimpleTrajectory composed = null;
		
		if (composedTrajectoryCache != null)
			composed = composedTrajectoryCache.get();
		
		if (composed == null) {
			composed = TrajectoryComposer.compose(this);
			composedTrajectoryCache = new SoftReference<SimpleTrajectory>(composed);
		}
	
		return composed;
	}

	/**
	 * @return
	 * @see world.SimpleTrajectory#size()
	 */
	@Override
	public int size() {
		return composed().size();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#length()
	 */
	@Override
	public double length() {
		return getArcTimePathComponent().length();
	}
	
	/**
	 * Caches the trace.
	 */
	private transient SoftReference<Geometry> traceCache = null;

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#trace()
	 */
	@Override
	public Geometry trace() {
		if (isEmpty())
			return immutableLineString();
		
		Geometry trace = null;
		
		if (traceCache != null)
			trace = traceCache.get();
		
		if (trace == null) {
			if (isComposed()) {
				trace = composed().trace();
			} else {
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
			
			traceCache = new SoftReference<>(trace);
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
		
		int timeCmpStart = time.compareTo(getStartTime());
		int timeCmpFinish = time.compareTo(getFinishTime());
		
		// time < getStartTime() || time > getFinishTime()
		if (timeCmpStart < 0 || timeCmpFinish > 0)
			throw new IllegalArgumentException("time must be covered by this trajectory");
		
		// short cut for start and finish time
		if (timeCmpStart  == 0) // time == getStartTime()
			return getStartLocation();
		if (timeCmpFinish == 0) // time == getFinishTime()
			return getFinishLocation();
		
		if (isComposed()) {
			return composed().interpolateLocation(time);
		} else {
			double t = durationToSeconds(Duration.between(getBaseTime(), time));
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
		return composed().concat(other);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#subPath(double, double)
	 */
	@Override
	public SimpleTrajectory subPath(double startPosition, double finishPosition) {
		// TODO decompose trajectory
		
		return composed().subPath(startPosition, finishPosition);
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
		double t1 = durationToSeconds(Duration.between(baseTime, startTime ));
		double t2 = durationToSeconds(Duration.between(baseTime, finishTime));
		
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
		
		double offset = durationToSeconds( Duration.between(baseTime, ownBaseTime) );
		
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
		return composed().toString();
	}

}
