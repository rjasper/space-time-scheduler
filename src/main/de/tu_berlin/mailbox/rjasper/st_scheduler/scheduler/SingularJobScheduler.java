package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.LeastDetourNodeSlotIterator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.LocationIterator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.NodeSlotIterator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.NodeSlotIterator.NodeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspective;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;

public class SingularJobScheduler {

	private World world = null;

	private WorldPerspectiveCache perspectiveCache = null;

	private LocalDateTime frozenHorizonTime = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	private JobSpecification jobSpec = null;

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

	public void setSpecification(JobSpecification jobSpec) {
		this.jobSpec = Objects.requireNonNull(jobSpec, "jobSpec");
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
		Objects.requireNonNull(jobSpec, "jobSpec");

		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");
	}

	public boolean schedule() {
		checkParameters();

		Geometry locationSpace = world.space(jobSpec.getLocationSpace());
		UUID jobId = jobSpec.getJobId();
		LocalDateTime earliest = max(
			jobSpec.getEarliestStartTime(), frozenHorizonTime);
		LocalDateTime latest = jobSpec.getLatestStartTime();
		Duration duration = jobSpec.getDuration();

		if (latest.isBefore(frozenHorizonTime))
			return false;

		JobPlanner tp = new JobPlanner();

		tp.setSchedule(schedule);
		tp.setScheduleAlternative(alternative);
		tp.setJobId(jobId);
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
				alternative,
				frozenHorizonTime,
				location,
				earliest, latest, duration);

			Iterable<NodeSlot> leastDetour = () ->
				new LeastDetourNodeSlotIterator(nodeSlots, location);

			for (NodeSlot ns : leastDetour) {
				Node n = ns.getNode();
				SpaceTimeSlot s = ns.getSlot();
				WorldPerspective perspective = perspectiveCache.getPerspectiveFor(n);

				NavigableMap<LocalDateTime, Job> wJobs = n.getNavigableJobs();
				// true if there is one job after s.finish
				boolean fixedEnd = !wJobs.isEmpty() &&
					!wJobs.lastKey().isBefore( s.getFinishTime() );

				tp.setFixedEnd(fixedEnd);
				tp.setWorldPerspective(perspective);
				tp.setNode(n);
				tp.setNodeSlot(s);
				tp.setEarliestStartTime(earliest);
				tp.setLatestStartTime(latest);

				// plan the routes of affected nodes and schedule job
				boolean status = tp.plan();

				if (status)
					return true;
			}
		}

		// all possible variable combinations are exhausted without being able
		// to schedule a job
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
