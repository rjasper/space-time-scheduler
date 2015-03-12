package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import de.tu_berlin.kbs.swarmos.st_scheduler.world.ArcTimePath;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.DecomposedTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.util.TrajectoryInterpolator.TrajectoryInterpolation;

public class TrajectoryLengthDurationCalculation {
	
	public static LengthDuration calcLengthDuration(
		Trajectory trajectory, LocalDateTime from, LocalDateTime to)
	{
		return new TrajectoryLengthDurationCalculation(trajectory)
			.calculate(from, to);
	}
	
	public static final class LengthDuration {
		private final double length;
		private final Duration duration;
		
		public LengthDuration(double length, Duration duration) {
			this.length = length;
			this.duration = duration;
		}
		
		public double getLength() {
			return length;
		}
		
		public Duration getDuration() {
			return duration;
		}
	}
	
	private final Trajectory trajectory;

	public TrajectoryLengthDurationCalculation(Trajectory trajectory) {
		this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
	}
	
	public LengthDuration calculate(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");

		if (from.isAfter(to))
			throw new IllegalArgumentException("from is after to");
		
		// short cut
		if (from.isEqual(trajectory.getStartTime()) && to.isEqual(trajectory.getFinishTime()))
			return new LengthDuration(trajectory.length(), trajectory.duration());
		
		if (trajectory instanceof DecomposedTrajectory)
			return calculateDecomposedTrajectory(from, to);
		else
			return calculateNormal(from, to);
	}

	private LengthDuration calculateNormal(LocalDateTime from, LocalDateTime to) {
		Seeker<LocalDateTime, Trajectory.Vertex> seeker = new BinarySearchSeeker<>(
			trajectory::getVertex,
			Trajectory.Vertex::getTime,
			trajectory.size());
		Interpolator<LocalDateTime, TrajectoryInterpolation> interpolator =
			new TrajectoryInterpolator<>(seeker, Interpolators.TIME_RELATOR);
		
		TrajectoryInterpolation
			start = interpolator.interpolate(from).get(),
			finish = interpolator.interpolate(to).get();
		
		double length = finish.getArc() - start.getArc();
		Duration duration = Duration.between(start.getTime(), finish.getTime());
		
		return new LengthDuration(length, duration);
	}

	private LengthDuration calculateDecomposedTrajectory(LocalDateTime from, LocalDateTime to) {
		DecomposedTrajectory dt = (DecomposedTrajectory) trajectory;
		LocalDateTime baseTime = dt.getBaseTime();
		ArcTimePath arcTimePath = dt.getArcTimePathComponent();
		double fromD = timeToSeconds(from, baseTime);
		double toD = timeToSeconds(to, baseTime);
		
		ArcTimePathLengthDurationCalculation.LengthDuration lengthDuration =
			ArcTimePathLengthDurationCalculation.calcLengthDuration(arcTimePath, fromD, toD);
		
		return new LengthDuration(
			lengthDuration.getLength(),
			secondsToDuration(lengthDuration.getDuration()));
	}

}
