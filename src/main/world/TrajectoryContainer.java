package world;

import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryContainer {
	
	private final TreeMap<LocalDateTime, Trajectory> trajectories = new TreeMap<>();
	
	public boolean isEmpty() {
		return trajectories.isEmpty();
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
	
	public LocalDateTime getStartTime() {
		return getFirstTrajectory().getStartTime();
	}
	
	public LocalDateTime getFinishTime() {
		return getLastTrajectory().getFinishTime();
	}
	
	public Trajectory getTrajectory(LocalDateTime time) {
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
	
	public void update(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (trajectory.isEmpty())
			throw new IllegalArgumentException("trajectory is empty");
		
		LocalDateTime startTime = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();
		
		if (!isEmpty() && (
			startTime .isAfter ( getFinishTime() ) ||
			finishTime.isBefore( getStartTime () )))
		{
			throw new IllegalArgumentException("incompatible trajectory");
		}
		
		// examine left and right neighbors
		Trajectory left  = getTrajectory(startTime);
		Trajectory right = getTrajectory(finishTime);
		
		// check locations
		if (left != null) {
			Point leftLocation = left.interpolateLocation(startTime);
			
			if (!trajectory.getStartLocation().equals(leftLocation))
				throw new IllegalArgumentException("incompatible start location");
		}
		// FIXME tail should be modifiable
		if (right != null) {
			Point rightLocation = right.interpolateLocation(finishTime);
			
			if (!trajectory.getFinishLocation().equals(rightLocation))
				throw new IllegalArgumentException("incompatible finish location");
		}
		
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

}
