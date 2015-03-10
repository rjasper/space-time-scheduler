package world;

import static java.util.Collections.*;
import static util.Comparables.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.IntervalSet;
import scheduler.util.IntervalSet.Interval;
import scheduler.util.MappedIntervalSet;

public class TrajectoryContainer {
	
	private final TreeMap<LocalDateTime, Trajectory> trajectories = new TreeMap<>();
	
	public boolean isEmpty() {
		return trajectories.isEmpty();
	}
	
	public boolean isContinuous() {
		if (isEmpty())
			return true;
		
		Iterator<Trajectory> it = trajectories.values().iterator();
		
		Trajectory last = it.next();
		
		while (it.hasNext()) {
			Trajectory curr = it.next();
			
			if (!curr.getStartLocation().equals(last.getFinishLocation()))
				return false;
			if (!curr.getStartTime().equals(last.getFinishTime()))
				return false;
			
			last = curr;
		}
		
		return true;
	}
	
	public Collection<Trajectory> getTrajectories() {
		return unmodifiableCollection(trajectories.values());
	}
	
	public Collection<Trajectory> getTrajectories(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		
		if (from.isAfter(to))
			throw new IllegalArgumentException("invalid time interval");
		
		Collection<Trajectory> overlapping = trajectories
			.subMap(overlappingStartTime(from), to)
			.values();
		
		return unmodifiableCollection(overlapping);
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
	
	public IntervalSet<LocalDateTime> getTrajectoryIntervals() {
		return new MappedIntervalSet<>(
			trajectories,
			t -> new Interval<>(t.getStartTime(), t.getFinishTime()));
	}
	
	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		if (!from.isBefore(to))
			throw new IllegalArgumentException("invalid time interval");
		
		// short cut if there is no overlapping interval
		if (isEmpty()                         ||
			!from.isBefore( getFinishTime() ) ||
			!to  .isAfter ( getStartTime () ))
		{
			return true;
		}
		
		return getTrajectories(from, to).stream()
			.allMatch(t -> {
				LocalDateTime start  = max(from, t.getStartTime ());
				LocalDateTime finish = min(to  , t.getFinishTime());
				
				return t.isStationary(start, finish);
			});
	}

	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		Trajectory trajectory = getTrajectory(time);
		
		return trajectory.interpolateLocation(time);
	}
	
	public void update(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (trajectory.isEmpty())
			throw new IllegalArgumentException("trajectory is empty");
		
		LocalDateTime startTime = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();
		
		// examine left and right neighbors
		Trajectory left  = getTrajectoryOrNull(startTime);
		Trajectory right = getTrajectoryOrNull(finishTime);
		
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
		// overwrite left neighbor
		if (cutLeft)
			put( left.subPath(left.getStartTime(), startTime) );
		// replace core
		put(trajectory);
		// add new right neighbor
		if (cutRight)
			put( right.subPath(finishTime, right.getFinishTime()) );
	}
	
	public void update(TrajectoryContainer container) {
		for (Trajectory t : container.trajectories.values())
			update(t);
	}
	
	private void put(Trajectory trajectory) {
		trajectories.put(trajectory.getStartTime(), trajectory);
	}
	
	public void deleteBefore(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		Entry<LocalDateTime, Trajectory> lowerEntry = trajectories.lowerEntry(time);
		
		// nothing to remove
		if (lowerEntry == null)
			return;
		
		LocalDateTime lowerTime = lowerEntry.getKey();
		Trajectory lowerTraj = lowerEntry.getValue();
		
		LocalDateTime lowestKey = lowerTraj.getFinishTime().isAfter(time)
			? lowerTime // keeps lower trajectory
			: time;     // deletes lower trajectory
		
		// remove trajectories not finishing after 'time'
		trajectories.headMap(lowestKey)
			.clear();
	}
	
	private LocalDateTime overlappingStartTime(LocalDateTime time) {
		Entry<LocalDateTime, Trajectory> lowerEntry = trajectories.lowerEntry(time);
		
		boolean includeLower = lowerEntry != null &&
			lowerEntry.getValue().getFinishTime().isAfter(time);
		
		return includeLower ? lowerEntry.getKey() : time;
	}
	
	public Trajectory calcTrajectory() {
		// TODO inefficient
		if (!isContinuous())
			throw new IllegalStateException("trajectory not continuous");

		// TODO inefficient and inaccurate (concat does not connect properly)
		return trajectories.values().stream()
			.reduce((u, v) -> u.concat(v))
			.orElse(SimpleTrajectory.empty());
	}

}
