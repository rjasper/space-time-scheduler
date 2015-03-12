package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.ArcTimePath;

public class ArcTimePathLengthDurationCalculation {
	
	public static LengthDuration calcLengthDuration(
		ArcTimePath arcTimePath, double from, double to)
	{
		return new ArcTimePathLengthDurationCalculation(arcTimePath)
			.calculate(from, to);
	}
	
	public static final class LengthDuration {
		private final double length;
		private final double duration;
		
		public LengthDuration(double length, double duration) {
			this.length = length;
			this.duration = duration;
		}
		
		public double getLength() {
			return length;
		}
		
		public double getDuration() {
			return duration;
		}
	}
	
	private final ArcTimePath arcTimePath;

	public ArcTimePathLengthDurationCalculation(ArcTimePath arcTimePath) {
		this.arcTimePath = Objects.requireNonNull(arcTimePath, "trajectory");
	}

	public LengthDuration calculate(double from, double to) {
		if (!Double.isFinite(from) || !Double.isFinite(to))
			throw new IllegalArgumentException("invalid interval");
		if (from > to)
			throw new IllegalArgumentException("from is after to");
		
		// short cut
		if (from == 0.0 && to == arcTimePath.durationInSeconds())
			return new LengthDuration(arcTimePath.length(), arcTimePath.durationInSeconds());
		
		Seeker<Double, ArcTimePath.Vertex> seeker = new BinarySearchSeeker<>(
			arcTimePath::getVertex,
			ArcTimePath.Vertex::getY,
			arcTimePath.size());
		Interpolator<Double, ImmutablePoint> interpolator = new PointPathInterpolator<>(seeker);
		
		ImmutablePoint
			start = interpolator.interpolate(from).get(),
			finish = interpolator.interpolate(to).get();
		
		double length = finish.getX() - start.getX();
		double duration = finish.getY() - start.getY();
		
		return new LengthDuration(length, duration);
	}
	
}
