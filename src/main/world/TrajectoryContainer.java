package world;

import static util.Comparables.*;
import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import jts.geom.immutable.ImmutablePoint;

public class TrajectoryContainer {
	
	private final TreeMap<LocalDateTime, Trajectory> trajectories = new TreeMap<>();
	
	public boolean isEmpty() {
		return trajectories.isEmpty();
	}
	
	public Collection<Trajectory> getTrajectories() {
		return unmodifiableCollection(trajectories.values());
	}
	
	public Trajectory getFirstTrajectory() {
		if (isEmpty())
			throw new NoSuchElementException("container is empty");
		
		return trajectories.firstEntry().getValue();
	}
	
	public Trajectory getLastTrajectory() {
		if (isEmpty())
			throw new NoSuchElementException("container is empty");
		
		return trajectories.lastEntry().getValue();
	}
	
	public Trajectory getTrajectory(LocalDateTime time) {
		Trajectory trajectory = getTrajectoryOrNull(time);
		
		if (trajectory == null)
			throw new IllegalArgumentException("invalid time");
		
		return trajectory;
	}

	public Trajectory getTrajectoryOrNull(LocalDateTime time) {
		Entry<LocalDateTime, Trajectory> entry = trajectories.floorEntry(time);
		
		if (entry == null) {
			return null;
		} else {
			Trajectory trajectory = entry.getValue();
			
			if (!trajectory.getFinishTime().isBefore(time))
				return trajectory;
			else
				return null;
		}
	}

	public LocalDateTime getStartTime() {
		return getFirstTrajectory().getStartTime();
	}
	
	public LocalDateTime getFinishTime() {
		return getLastTrajectory().getFinishTime();
	}
	
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		Trajectory trajectory = getTrajectory(time);
		
		return trajectory.interpolateLocation(time);
	}
	
	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		// FIXME empty regions are not considered
		
		if (isEmpty())
			throw new IllegalStateException("container is empty");
		
		if (!from.isBefore( to              ) ||
			!from.isBefore( getFinishTime() ) ||
			!to  .isAfter ( getStartTime () ))
		{
			throw new IllegalArgumentException("invalid time interval");
		}
		
		return overlappingTrajectories(from, to).stream()
			.allMatch(t -> {
				LocalDateTime start  = max(from, t.getStartTime ());
				LocalDateTime finish = min(to  , t.getFinishTime());
				
				return t.isStationary(start, finish);
			});
	}
	
	public void update(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (trajectory.isEmpty())
			throw new IllegalArgumentException("trajectory is empty");
		
		LocalDateTime startTime = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();
		
		// allow empty regions (WorkerUnitScheduleUpdate)
//		// don't allow empty regions
//		if (!isEmpty() && (
//			startTime .isAfter ( getFinishTime() ) ||
//			finishTime.isBefore( getStartTime () )))
//		{
//			throw new IllegalArgumentException("incompatible trajectory");
//		}
		
		// examine left and right neighbors
		Trajectory left  = getTrajectoryOrNull(startTime);
		Trajectory right = getTrajectoryOrNull(finishTime);
		
		// no location check
//		// check locations
//		if (left != null) {
//			Point leftLocation = left.interpolateLocation(startTime);
//			
//			if (!trajectory.getStartLocation().equals(leftLocation))
//				throw new IllegalArgumentException("incompatible start location");
//		}
//		// fIXME tail should be modifiable
//		if (right != null) {
//			Point rightLocation = right.interpolateLocation(finishTime);
//			
//			if (!trajectory.getFinishLocation().equals(rightLocation))
//				throw new IllegalArgumentException("incompatible finish location");
//		}
		
		// determine necessary cuts
		// left.finish > trajectory.start
		boolean cutLeft = left != null &&
			left.getStartTime().isBefore(startTime) &&
			left.getFinishTime().isAfter(startTime);
		// right.finish > trajectory.finish
		boolean cutRight = right != null &&
			right.getStartTime().isBefore(finishTime) &&
			right.getFinishTime().isAfter(finishTime);
		
		// update trajectories
		
		// startTime <= core.startTime < finishTime
		trajectories.subMap(startTime, finishTime)
			.clear();
		// overwrites left neighbor
		if (cutLeft)
			put( left.subPath(left.getStartTime(), startTime) );
		// replaces core
		put(trajectory);
		// adds new right neighbor
		if (cutRight)
			put( right.subPath(finishTime, right.getFinishTime()) );
	}
	
	private void put(Trajectory trajectory) {
		trajectories.put(trajectory.getStartTime(), trajectory);
	}
	
	public void deleteHead(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		// remove trajectories not finishing after 'time'
		trajectories.headMap(overlappingStartTime(time))
			.clear();
	}
	
	public Collection<Trajectory> overlappingTrajectories(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		
		if (from.isAfter(to))
			throw new IllegalArgumentException("invalid time interval");
		
		Collection<Trajectory> overlapping = trajectories
			.subMap(overlappingStartTime(from), to)
			.values();
		
		return unmodifiableCollection(overlapping);
	}
	
	private LocalDateTime overlappingStartTime(LocalDateTime time) {
		Entry<LocalDateTime, Trajectory> lowerEntry = trajectories.lowerEntry(time);
		
		boolean includeLower = lowerEntry != null &&
			lowerEntry.getValue().getFinishTime().isAfter(time);
		
		return includeLower ? lowerEntry.getKey() : time;
	}
	
	public Trajectory calcTrajectory() {
		// FIXME empty regions will mess this up
		return trajectories.values().stream()
			.reduce((u, v) -> u.concat(v))
			.orElse(SimpleTrajectory.empty());
	}

}
