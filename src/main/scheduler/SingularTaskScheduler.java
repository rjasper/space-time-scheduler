package scheduler;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import scheduler.pickers.LocationIterator;
import scheduler.pickers.NodeSlotIterator;
import scheduler.pickers.NodeSlotIterator.NodeSlot;
import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class SingularTaskScheduler {
	
	private World world = null;
	
	private WorldPerspectiveCache perspectiveCache = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private TaskSpecification taskSpec = null;
	
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

	public void setSpecification(TaskSpecification taskSpec) {
		this.taskSpec = Objects.requireNonNull(taskSpec, "taskSpec");
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
		Objects.requireNonNull(taskSpec, "taskSpec");
		
		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");
	}

	public boolean schedule() {
		checkParameters();
		
		Geometry locationSpace = world.space(taskSpec.getLocationSpace());
		UUID taskId = taskSpec.getTaskId();
		LocalDateTime earliest = max(
			taskSpec.getEarliestStartTime(), frozenHorizonTime);
		LocalDateTime latest = taskSpec.getLatestStartTime();
		Duration duration = taskSpec.getDuration();

		if (latest.isBefore(frozenHorizonTime))
			return false;
		
		TaskPlanner tp = new TaskPlanner();

		tp.setSchedule(schedule);
		tp.setScheduleAlternative(alternative);
		tp.setTaskId(taskId);
		tp.setDuration(duration);

		// iterate over possible locations

		// TODO prefilter the nodes who have time without considering their location
		// return if no nodes remain
		// TODO nodes which already are in position shouldn't need to move.

		Iterable<Point> locations = locationSpace instanceof Point
			? singleton((Point) locationSpace)
			: () -> new LocationIterator(locationSpace, maxLocationPicks);

		for (Point location : locations) {
			tp.setLocation(location);

			// iterate over possible node time slots.

			// Node units have different perspectives of the world.
			// The LocationIterator might pick a location which is inaccessible
			// for a unit. Therefore, the nodes are filtered by the location
			
			Iterable<NodeSlot> nodeSlots = () -> new NodeSlotIterator(
				filterByLocation(location),
				frozenHorizonTime,
				location,
				earliest, latest, duration);

			for (NodeSlot ws : nodeSlots) {
				Node w = ws.getNode();
				IdleSlot s = ws.getIdleSlot();
				WorldPerspective perspective = perspectiveCache.getPerspectiveFor(w);
				
				Entry<?, Task> lastTaskEntry = w.getNavigableTasks().lastEntry();
				boolean fixedEnd = lastTaskEntry != null &&
					lastTaskEntry.getValue().getStartTime().isBefore( s.getFinishTime() );
				
//				boolean fixedEnd = s.getFinishTime().isBefore(Scheduler.END_OF_TIME);

				tp.setFixedEnd(fixedEnd);
				tp.setWorldPerspective(perspective);
				tp.setNode(w);
				tp.setIdleSlot(s);
				tp.setEarliestStartTime(earliest);
				tp.setLatestStartTime(latest);

				// plan the routes of affected nodes and schedule task
				boolean status = tp.plan();

				if (status)
					return true;
			}
		}

		// all possible variable combinations are exhausted without being able
		// to schedule a task
		return false;
	}
	
	/**
	 * Filters the pool of nodes which are able to reach a location in
	 * regard to their individual size.
	 *
	 * @param location
	 * @return the filtered nodes which are able to reach the location.
	 */
	private Collection<Node> filterByLocation(Point location) {
		return schedule.getNodes().stream()
			.filter(w -> checkLocationFor(location, w))
			.collect(toList());
	}
	
	/**
	 * Checks if a node is able to reach a location in regard to its size.
	 *
	 * @param location
	 * @param node
	 * @return {@code true} iff node is able to reach the location.
	 */
	private boolean checkLocationFor(Point location, Node node) {
		WorldPerspective perspective = perspectiveCache.getPerspectiveFor(node);
		Geometry map = perspective.getView().getMap();
	
		return !map.contains(location);
	}

}
