package world.util;

import static util.DurationConv.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.TriFunction;
import world.Trajectory;
import world.Trajectory.Vertex;

// TODO document
public class TrajectoryInterpolator<P extends Comparable<? super P>>
extends AbstractInterpolator<
	P,
	Trajectory.Vertex,
	TrajectoryInterpolator.TrajectoryInterpolation>
{

	// TODO central location for common relators
	public static final
	TriFunction<LocalDateTime, LocalDateTime, LocalDateTime, Double> TIME_RELATOR =
		(t, t1, t2) -> {
			double d1 = inSeconds(Duration.between(t1, t));
			double d12 = inSeconds(Duration.between(t1, t2));
			
			return d1 / d12;
		};

	public static class TrajectoryInterpolation {
		
		private final double subIndex;
		private final ImmutablePoint location;
		private final LocalDateTime time;
		
		public TrajectoryInterpolation(double subIndex, ImmutablePoint location, LocalDateTime time) {
			this.subIndex = subIndex;
			this.location = location;
			this.time = time;
		}

		/**
		 * @return the subIndex
		 */
		public double getSubIndex() {
			return subIndex;
		}

		/**
		 * @return the location
		 */
		public ImmutablePoint getLocation() {
			return location;
		}

		/**
		 * @return the time
		 */
		public LocalDateTime getTime() {
			return time;
		}
		
	}
	
	private final TriFunction<P, P, P, Double> relator;
	
	public TrajectoryInterpolator(
		Seeker<P, ? extends Vertex> seeker,
		TriFunction<P, P, P, Double> relator)
	{
		super(seeker);
		
		this.relator = Objects.requireNonNull(relator, "positionRelator");
	}

	@Override
	protected TrajectoryInterpolation interpolate(P position, int idx1, P p1, Vertex v1, int idx2, P p2, Vertex v2) {
		double alpha = relator.apply(position, p1, p2);
		
		double subIndex = idx1 + alpha;
		
		double x1 = v1.getX(), y1 = v1.getY(), x2 = v2.getX(), y2 = v2.getY();
		ImmutablePoint location = immutablePoint(x1 + alpha*(x2-x1), y1 + alpha*(y2-y1));
		
		LocalDateTime t1 = v1.getTime();
		LocalDateTime t2 = v2.getTime();
		double d = inSeconds(Duration.between(t1, t2));
		double d1 = alpha * d;
		
		LocalDateTime time = t1.plus(ofSeconds(d1));
		
		return new TrajectoryInterpolation(subIndex, location, time);
	}

	@Override
	protected TrajectoryInterpolation onSpot(int index, P position, Vertex vertex) {
		return new TrajectoryInterpolation(index, vertex.getLocation(), vertex.getTime());
	}
	
}
