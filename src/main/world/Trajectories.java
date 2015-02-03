package world;

import java.time.Duration;
import java.time.LocalDateTime;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Provides static methods that operate on or return trajectories.
 * 
 * @author Rico
 */
public final class Trajectories {
	
	private Trajectories() {}
	
	/**
	 * An empty {@code Trajectory} instance.
	 */
	private static final Trajectory EMPTY_TRAJECTORY = new Trajectory() {
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public SpatialPath getSpatialPath() {
			return SpatialPath.empty();
		}
		
		@Override
		public ImmutableList<LocalDateTime> getTimes() {
			return ImmutableList.of();
		}
		
		@Override
		public ImmutablePoint getStartLocation() {
			return null;
		}
		
		@Override
		public ImmutablePoint getFinishLocation() {
			return null;
		}
		
		@Override
		public LocalDateTime getStartTime() {
			return null;
		}
		
		@Override
		public LocalDateTime getFinishTime() {
			return null;
		}
		
		@Override
		public Duration getDuration() {
			return null;
		}
		
		@Override
		public double length() {
			return Double.NaN;
		}
		
		@Override
		public Geometry trace() {
			return null;
		}
		
		@Override
		public Trajectory subTrajectory(LocalDateTime startTime, LocalDateTime finishTime) {
			return this; // empty
		}

		@Override
		public ArcTimePath calcArcTimePath(LocalDateTime baseTime) {
			return null;
		}
		
	};

	/**
	 * @return an empty {@code Trajectory} instance.
	 */
	public static Trajectory emptyTrajectory() {
		return EMPTY_TRAJECTORY;
	}

}
