package world.util;

import static com.vividsolutions.jts.operation.distance.DistanceOp.distance;
import static jts.geom.immutable.ImmutableGeometries.immutable;
import static util.DurationConv.inSeconds;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryVertexIterator implements Iterator<TrajectoryVertexIterator.TrajectoryVertex> {
	
	private final LocalDateTime baseTime;
	
	private final Iterator<Point> spatialPath;
	
	private final Iterator<LocalDateTime> times;
	
	private TrajectoryVertex previous = null;
	
	public TrajectoryVertexIterator(Trajectory trajectory, LocalDateTime baseTime) {
		Objects.requireNonNull(trajectory, "trajectory");
		Objects.requireNonNull(baseTime, "baseTime");
		
		this.baseTime = baseTime;
		this.spatialPath = trajectory.getSpatialPath().iterator();
		this.times = trajectory.getTimes().iterator();
	}
	
	@Override
	public boolean hasNext() {
		return spatialPath.hasNext();
	}

	@Override
	public TrajectoryVertex next() {
		Point location = spatialPath.next();
		LocalDateTime time = times.next();
		double s = previous == null
			? 0.0
			: previous.s + distance(previous.location, location);
		double t = inSeconds(Duration.between(baseTime, time));
		TrajectoryVertex vertex = new TrajectoryVertex(s, t, location, time);
		
		previous = vertex;
		
		return vertex;
	}

	public class TrajectoryVertex {
		
		private final double s;
		private final double t;
		private final Point location;
		private final LocalDateTime time;
		
		public TrajectoryVertex(
			double s,
			double t,
			Point location,
			LocalDateTime time)
		{
			this.s = s;
			this.t = t;
			this.location = immutable(location);
			this.time = time;
		}
	
		public double getX() {
			return location.getX();
		}
	
		public double getY() {
			return location.getY();
		}
	
		public double getArc() {
			return s;
		}
	
		public double getT() {
			return t;
		}
	
		public LocalDateTime getTime() {
			return time;
		}
	
		public Point getLocation() {
			return location;
		}
		
	}

}
