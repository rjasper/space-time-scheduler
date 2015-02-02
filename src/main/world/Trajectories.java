package world;

import static world.Paths.*;

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
			return Paths.emptySpatialPath();
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
		public ArcTimePath calcArcTimePath(LocalDateTime baseTime) {
			return null;
		}
		
	};

	/**
	 * An empty {@code SimpleTrajectory} instance.
	 */
	private static final SimpleTrajectory EMPTY_SIMPLE_TRAJECTORY =
		new SimpleTrajectory(emptySpatialPath(), ImmutableList.of());

	/**
	 * An empty {@code DecomposedTrajectory} instance.
	 */
	private static final DecomposedTrajectory EMPTY_DECOMPOSED_TRAJECTORY =
		new DecomposedTrajectory(LocalDateTime.MIN, emptySpatialPath(), emptyArcTimePath());
	
	/**
	 * @return an empty {@code Trajectory} instance.
	 */
	public static Trajectory emptyTrajectory() {
		return EMPTY_TRAJECTORY;
	}
	
	/**
	 * @return an empty {@code SimpleTrajectory} instance.
	 */
	public static SimpleTrajectory emptySimpleTrajectory() {
		return EMPTY_SIMPLE_TRAJECTORY;
	}

	/**
	 * @return an empty {@code DecomposedTrajectory} instance.
	 */
	public static DecomposedTrajectory emptyDecomposedTrajectory() {
		return EMPTY_DECOMPOSED_TRAJECTORY;
	}
	
}
