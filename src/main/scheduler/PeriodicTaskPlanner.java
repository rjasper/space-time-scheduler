package scheduler;

import static jts.geom.immutable.ImmutableGeometries.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import scheduler.pickers.LocationIterator;
import world.World;
import world.WorldPerspectiveCache;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PeriodicTaskPlanner {
	
	private World world = null;
	
	private WorldPerspectiveCache perspectiveCache = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private PeriodicTaskSpecification periodicSpec = null;
	
	private int maxLocationPicks = 0;
	
	public void setWorld(World world) {
		this.world = Objects.requireNonNull(world, "world");
	}

	public void setPerspectiveCache(WorldPerspectiveCache perspectiveCache) {
		this.perspectiveCache = Objects.requireNonNull(perspectiveCache, "perspectiveCache");
	}

	public void setFrozenHorizonTime(LocalDateTime frozenHorizonTime) {
		this.frozenHorizonTime = Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setSpecification(PeriodicTaskSpecification periodicSpec) {
		this.periodicSpec = Objects.requireNonNull(periodicSpec, "periodicSpec");
	}

	public void setMaxLocationPicks(int maxLocationPicks) {
		if (maxLocationPicks <= 0)
			throw new IllegalArgumentException("invalid number of picks");
		
		this.maxLocationPicks = maxLocationPicks;
	}

	private void checkParameters() {
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(perspectiveCache, "perspectiveCache");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(periodicSpec, "taskSpec");
		
		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");
	}
	
	public boolean schedule() {
		checkParameters();
		
		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();
		
		// short cut if first task cannot be scheduled due to frozen horizon
		// startTime + period < frozenHorizonTime + duration
		if (startTime.plus(period) .isBefore( frozenHorizonTime.plus(duration) ))
			return false;
		
		if (periodicSpec.isSameLocation())
			return scheduleSameLocation();
		else
			return scheduleIndependentLocation();
	}
	
	private boolean scheduleSameLocation() {
		Collection<UUID> taskIds = periodicSpec.getTaskIds();
		Geometry locationSpace = world.space(periodicSpec.getLocationSpace());
		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();
		
		SingularTaskPlanner sc = new SingularTaskPlanner();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setMaxLocationPicks(1); // using external location picker
		
		Iterable<Point> locations = () -> new LocationIterator(
			locationSpace, maxLocationPicks);

		for (Point location : locations) {
			ScheduleAlternative branch = alternative.branch();
			sc.setAlternative(branch);

			LocalDateTime periodStart = startTime;
			boolean noBreak = true;
			for (UUID taskId : taskIds) {
				LocalDateTime periodFinish = periodStart.plus(period);
				TaskSpecification taskSpec = new TaskSpecification(
					taskId, immutable(location), periodStart, periodFinish, duration);
				
				sc.setSpecification(taskSpec);
				
				boolean status = sc.schedule();
				
				if (!status) {
					noBreak = false;
					break;
				}
				
				periodStart = periodFinish;
			}
			
			// indicates successful scheduling of all tasks
			if (noBreak) {
				branch.merge();
				return true;
			} else {
				branch.delete();
			}
		}
		
		return false;
	}
	
	private boolean scheduleIndependentLocation() {
		Collection<UUID> taskIds = periodicSpec.getTaskIds();
		Geometry locationSpace = world.space(periodicSpec.getLocationSpace());
		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();
		
		SingularTaskPlanner sc = new SingularTaskPlanner();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setMaxLocationPicks(maxLocationPicks);

		LocalDateTime periodStart = startTime;
		boolean noBreak = true;
		for (UUID taskId : taskIds) {
			LocalDateTime periodFinish = periodStart.plus(period);
			TaskSpecification taskSpec = new TaskSpecification(
				taskId, immutable(locationSpace), periodStart, periodFinish, duration);
			
			sc.setSpecification(taskSpec);
			
			boolean status = sc.schedule();
			
			if (!status) {
				noBreak = false;
				break;
			}
			
			periodStart = periodFinish;
		}

		// indicates successful scheduling of all tasks
		return noBreak;
	}

}
