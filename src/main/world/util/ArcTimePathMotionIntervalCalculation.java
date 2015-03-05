package world.util;

import java.util.Objects;

import scheduler.util.SimpleIntervalSet;
import world.ArcTimePath;
import world.util.Seeker.SeekResult;

public class ArcTimePathMotionIntervalCalculation {
	
	public static SimpleIntervalSet<Double> calcMotionIntervals(
		ArcTimePath arcTimePath, double from, double to)
	{
		return new ArcTimePathMotionIntervalCalculation(arcTimePath)
			.calculate(from, to);
	}
	
	private final ArcTimePath arcTimePath;

	public ArcTimePathMotionIntervalCalculation(ArcTimePath arcTimePath) {
		this.arcTimePath = Objects.requireNonNull(arcTimePath, "arcTimePath");
	}
	
	public SimpleIntervalSet<Double> calculate(double from, double to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		
		if (from > to)
			throw new IllegalArgumentException("from is after to");
		
		if (arcTimePath.isEmpty())
			return new SimpleIntervalSet<>();
		
		Seeker<Double, ArcTimePath.Vertex> seeker = new BinarySearchSeeker<>(
			arcTimePath::getVertex,
			ArcTimePath.Vertex::getY,
			arcTimePath.size());

		SeekResult<Double, ArcTimePath.Vertex>
			start = seeker.seekFloor(from),
			finish = seeker.seekCeiling(to);
		
		SimpleIntervalSet<Double> motionIntervals = new SimpleIntervalSet<>();
		
		motionIntervals.add(from, to);
		
		// remove stationary intervals from motionIntervals
		ArcTimePath.Vertex last = start.get();
		for (int i = start.getIndex()+1; i <= finish.getIndex(); ++i) {
			ArcTimePath.Vertex curr = arcTimePath.getVertex(i);
			
			if (curr.getX() == last.getX())
				motionIntervals.remove(last.getY(), curr.getY());
			
			last = curr;
		}
		
		return motionIntervals;
	}

}
