package world.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

import util.DurationConv;
import world.Trajectory;
import world.util.SpatialPathSegmentIterable.SpatialPathSegment;
import world.util.SpatialPathSegmentIterable.SpatialPathSegmentIterator;

import com.vividsolutions.jts.geom.Point;

public class TrajectorySegmentIterable implements Iterable<TrajectorySegmentIterable.TrajectorySegment> {

	private final Trajectory trajectory;
	private final LocalDateTime baseTime;

	public TrajectorySegmentIterable(Trajectory trajectory, LocalDateTime baseTime) {
		this.trajectory = trajectory;
		this.baseTime = baseTime;
	}

	@Override
	public Iterator<TrajectorySegment> iterator() {
		return new TrajectorySegmentIterator(trajectory, baseTime);
	}

	public static class TrajectorySegment {

		private final Point startPoint;
		private final Point finishPoint;
		private final double startTime;
		private final double duration;

		public TrajectorySegment(Point startPoint, Point finishPoint, double startTime, double duration) {
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
			this.duration = duration;
			this.startTime = startTime;
		}

		public boolean isStationary() {
			return startPoint.equals(finishPoint);
		}

		public Point getStartPoint() {
			return startPoint;
		}

		public Point getFinishPoint() {
			return finishPoint;
		}

		public double getStartTime() {
			return startTime;
		}

		public double getDuration() {
			return duration;
		}

	}

	public static class TrajectorySegmentIterator implements Iterator<TrajectorySegment> {

		private final SpatialPathSegmentIterator spatialPathSegmentIterator;
		private final Iterator<LocalDateTime> timeIterator;

		private LocalDateTime lastTime;
		private double accSeconds;

		public TrajectorySegmentIterator(Trajectory trajectory, LocalDateTime baseTime) {
			this.spatialPathSegmentIterator =
				new SpatialPathSegmentIterator(trajectory.getSpatialPath());
			this.timeIterator = trajectory.getTimes().iterator();

			if (hasNext())
				init(baseTime);
		}

		private void init(LocalDateTime baseTime) {
			lastTime = timeIterator.next();
			Duration duration = Duration.between(baseTime, lastTime);
			accSeconds = DurationConv.inSeconds(duration);
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

			Duration duration = Duration.between(startTime, finishTime);
			double seconds = DurationConv.inSeconds(duration);
			double startSeconds = accSeconds;

			accSeconds += seconds;

			return new TrajectorySegment(startPoint, finishPoint, startSeconds, seconds);
		}

	}

}
