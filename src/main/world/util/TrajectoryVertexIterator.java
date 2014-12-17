package world.util;

import static com.vividsolutions.jts.operation.distance.DistanceOp.*;
import static util.DurationConv.*;
import static jts.geom.immutable.ImmutableGeometries.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryVertexIterator implements Iterator<TrajectoryVertexIterator.TrajectoryVertex> {
	
	private final LocalDateTime baseTime;
	
	private final Iterator<Point> spatialPath;
	
	private final Iterator<LocalDateTime> times;
	
	private TrajectoryVertex previous = null;
	
	public TrajectoryVertexIterator(Trajectory trajectory, LocalDateTime baseTime) {
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
		double s = previous == null ? 0.0
			: previous.s + distance(previous.location, location);
		double t = inSeconds(Duration.between(baseTime, time));
		TrajectoryVertex vertex = new TrajectoryVertex(previous, s, t, location, time);
		
		previous = vertex;
		
		return vertex;
	}

	public class TrajectoryVertex {
		
		private final TrajectoryVertex predecessor;
		
		private final double s;
		private final double t;
		private final Point location;
		private final LocalDateTime time;
		
		public TrajectoryVertex(
			TrajectoryVertex predecessor,
			double s,
			double t,
			Point location,
			LocalDateTime time)
		{
			this.predecessor = predecessor;
			this.s = s;
			this.t = t;
			this.location = immutable(location);
			this.time = time;
		}
	
		public TrajectoryVertex getPredecessor() {
			return predecessor;
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
