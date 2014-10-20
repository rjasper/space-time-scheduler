package world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import world.DynamicObstacle;
import world.Trajectory;

public class ForbiddenRegionBuilder {
	
	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private LineString spatialPath = null;
	
	private List<ForbiddenRegion> resultForbiddenRegions = null;
	
	private LocalDateTime baseTime;
	
	public boolean isReady() {
		return spatialPath != null;
	}

	private List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	private LineString getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(LineString spatialPath) {
		this.spatialPath = spatialPath;
	}

//	public List<Double> getResultArcOrdinates() {
//		return resultArcOrdinates;
//	}
//
//	private void setResultArcOrdinates(List<Double> resultArcOrdinates) {
//		this.resultArcOrdinates = resultArcOrdinates;
//	}
//
//	public List<LocalDateTime> getResultTimeOrdinates() {
//		return resultTimeOrdinates;
//	}
//
//	private void setResultTimeOrdinates(List<LocalDateTime> resultTimeOrdinates) {
//		this.resultTimeOrdinates = resultTimeOrdinates;
//	}
	
	public void calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		LineString spatialPath = getSpatialPath();
		
		SpatialPathSegmentIterable spatialPathSegments =
			new SpatialPathSegmentIterable(spatialPath);
		
		for (DynamicObstacle o : getDynamicObstacles()) {
			TrajectorySegmentIterable obstacleTrajectorySegments =
				new TrajectorySegmentIterable(o.getTrajectory());
			
			for (TrajectorySegment obstacleTrajectorySegment : obstacleTrajectorySegments) {
				Point obstacleStartPoint = obstacleTrajectorySegment.getStartPoint();
				Point obstacleFinishPoint = obstacleTrajectorySegment.getFinishPoint();
				double obstacleSpeed = obstacleTrajectorySegment.getSpeed();
				
				Vector obstacleVector = new BasicVector();
				
				for (SpatialPathSegment spatialPathSegment : spatialPathSegments) {
					Point spatialPathSegmentStartPoint = spatialPathSegment.getStartPoint();
					Point spatialPathSegmentFinishPoint = spatialPathSegment.getFinishPoint();
					
					// calculate inverse of st-base matrix
					
					// if obstacle and path linear independent (inverse exists)
					//   transform with new base
					// else
					//   union with path, construct parallelogram and cut
				}
			}
		}
	}
	
	private static class SpatialPathSegment {
		
		private final Point startPoint;
		private final Point finishPoint;
		
		public SpatialPathSegment(Point startPoint, Point finishPoint) {
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
		}
		
		public Point getStartPoint() {
			return startPoint;
		}
		
		public Point getFinishPoint() {
			return finishPoint;
		}
		
	}
	
	private static class SpatialPathSegmentIterable implements Iterable<SpatialPathSegment> {
		
		private LineString spatialPath;
		
		public SpatialPathSegmentIterable(LineString spatialPath) {
			this.spatialPath = spatialPath;
		}

		@Override
		public Iterator<SpatialPathSegment> iterator() {
			return new SpatialPathSegmentIterator(spatialPath);
		}
		
	}
	
	private static class SpatialPathSegmentIterator implements Iterator<SpatialPathSegment> {
		
		

		private final LineString spatialPath;

		private Point lastPosition;

		private int i = 0;

		public SpatialPathSegmentIterator(LineString spatialPath) {
			this.spatialPath = spatialPath;
			
			if (hasNext())
				init();
		}
		
		private void init() {
			lastPosition = nextPoint();
		}

		private Point nextPoint() {
			return lastPosition = spatialPath.getPointN(i++);
		}

		@Override
		public boolean hasNext() {
			return i < spatialPath.getNumPoints();
		}

		@Override
		public SpatialPathSegment next() {
			Point startPoint = lastPosition;
			Point finishPoint = nextPoint();
			
			return new SpatialPathSegment(startPoint, finishPoint);
		}
		
	}
	
	private static class TrajectorySegment {
		
		private final double speed;
		private final Point startPoint;
		private final Point finishPoint;
		
		public TrajectorySegment(double speed, Point startPoint, Point finishPoint) {
			this.speed = speed;
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
		}

		public double getSpeed() {
			return speed;
		}

		public Point getStartPoint() {
			return startPoint;
		}

		public Point getFinishPoint() {
			return finishPoint;
		}
		
	}

	private static class TrajectorySegmentIterable implements Iterable<TrajectorySegment> {
		
		private final Trajectory trajectory;

		public TrajectorySegmentIterable(Trajectory trajectory) {
			this.trajectory = trajectory;
		}

		@Override
		public Iterator<TrajectorySegment> iterator() {
			return new TrajectorySegmentIterator(trajectory);
		}
		
	}
	
	private static class TrajectorySegmentIterator implements Iterator<TrajectorySegment> {
		
		private final SpatialPathSegmentIterator spatialPathSegmentIterator;
		private final Iterator<LocalDateTime> timeIterator;
		
		private LocalDateTime lastTime;
		
		public TrajectorySegmentIterator(Trajectory trajectory) {
			this.spatialPathSegmentIterator =
				new SpatialPathSegmentIterator(trajectory.getPath2d());
			this.timeIterator = trajectory.getTimes().iterator();
			
			if (hasNext())
				init();
		}
		
		private void init() {
			lastTime = timeIterator.next();
		}
		
		private LocalDateTime nextTime() {
			return lastTime = timeIterator.next();
		}
		
		@Override
		public boolean hasNext() {
			return timeIterator.hasNext();
		}

		@Override
		public TrajectorySegment next() {
			SpatialPathSegment spatialPathSegment = spatialPathSegmentIterator.next();
			Point startPoint = spatialPathSegment.getStartPoint();
			Point finishPoint = spatialPathSegment.getFinishPoint();
			LocalDateTime startTime = lastTime;
			LocalDateTime finishTime = nextTime();
			
			long millis = Duration.between(startTime, finishTime).toMillis();
			double seconds = (double)(millis / 1000L) + (double)(millis % 1000L) / 1000.;
			double distance = DistanceOp.distance(startPoint, finishPoint);
			double speed = distance / seconds;
			
			return new TrajectorySegment(speed, startPoint, finishPoint);
		}
		
	}

}
