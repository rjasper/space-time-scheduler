package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import java.time.LocalDateTime;
import java.util.Objects;

import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util.SimpleIntervalSet;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.util.Seeker.SeekResult;

public class TrajectoryMotionIntervalCalculation {
	
	public static SimpleIntervalSet<LocalDateTime> calcMotionIntervals(
		Trajectory trajectory, LocalDateTime from, LocalDateTime to)
	{
		return new TrajectoryMotionIntervalCalculation(trajectory)
			.calculate(from, to);
	}
	
	private final Trajectory trajectory;

	public TrajectoryMotionIntervalCalculation(Trajectory trajectory) {
		this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
	}
	
	public SimpleIntervalSet<LocalDateTime> calculate(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		
		if (from.isAfter(to))
			throw new IllegalArgumentException("from is after to");
		
		if (trajectory.isEmpty())
			return new SimpleIntervalSet<>();
		
		Seeker<LocalDateTime, Trajectory.Vertex> seeker = new BinarySearchSeeker<>(
			trajectory::getVertex,
			Trajectory.Vertex::getTime,
			trajectory.size());
		
		SeekResult<LocalDateTime, Trajectory.Vertex>
			start = seeker.seekFloor(from),
			finish = seeker.seekCeiling(to);
		
		SimpleIntervalSet<LocalDateTime> motionIntervals = new SimpleIntervalSet<>();
		
		motionIntervals.add(from, to);
		
		// remove stationary intervals from motionIntervals
		Trajectory.Vertex last = start.get();
		for (int i = start.getIndex()+1; i <= finish.getIndex(); ++i) {
			Trajectory.Vertex curr = trajectory.getVertex(i);
			
			if (curr.getLocation().equals(last.getLocation()))
				motionIntervals.remove(last.getTime(), curr.getTime());
			
			last = curr;
		}
		
		return motionIntervals;
	}

}
