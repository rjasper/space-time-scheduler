package tasks;

import static java.util.stream.Collectors.toList;
import static util.Comparables.max;
import static util.Comparables.min;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.iterators.IteratorIterable;

import pickers.LocationIterator;
import pickers.WorkerUnitSlotIterator;
import pickers.WorkerUnitSlotIterator.WorkerUnitSlot;
import world.RadiusBasedWorldPerspectiveCache;
import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Scheduler {

	public static final int MAX_LOCATION_PICKS = 10;

	private final World world;

	private final WorldPerspectiveCache perspectiveCache;

	private final List<WorkerUnit> workers;

	public Scheduler(World world, Collection<WorkerUnit> workers) {
		if (workers == null)
			throw new NullPointerException("workers is null");

		this.world = world;
		this.perspectiveCache = new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
		this.workers = new ArrayList<>(workers);
	}

	private World getWorld() {
		return world;
	}

	private WorldPerspectiveCache getPerspectiveCache() {
		return perspectiveCache;
	}

	private List<WorkerUnit> getWorkers() {
		return workers;
	}

	public boolean schedule(Specification spec) {
		World world = getWorld();
		List<WorkerUnit> workers = getWorkers();
		WorldPerspectiveCache perspectiveCache = getPerspectiveCache();
		Geometry locationSpace = world.space(spec.getLocationSpace());
		LocalDateTime earliest = spec.getEarliestStartTime();
		LocalDateTime latest = spec.getLatestStartTime();
		Duration duration = spec.getDuration();

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(workers);
		tp.setPerspectiveCache(perspectiveCache);
		tp.setDuration(duration);

		Iterable<Point> locations = new IteratorIterable<>(
			new LocationIterator(locationSpace, MAX_LOCATION_PICKS));

		for (Point loc : locations) {
			// Worker units have different perspectives of the world.
			// The LocationIterator might pick a location which is inaccessible
			// for a unit. Therefore, the workers are filtered by the location
			Iterable<WorkerUnitSlot> workerSlots = new IteratorIterable<>(
				new WorkerUnitSlotIterator(filterByLocation(loc), loc, earliest, latest, duration));

			tp.setLocation(loc);

			for (WorkerUnitSlot ws : workerSlots) {
				WorkerUnit w = ws.getWorkerUnit();
				IdleSlot s = ws.getIdleSlot();
				LocalDateTime slotStartTime = s.getStartTime();
				LocalDateTime slotFinishTime = s.getFinishTime();

				tp.setWorkerUnit(w);
				tp.setEarliestStartTime( max(earliest, slotStartTime) );
				tp.setLatestStartTime( slotFinishTime == null ? latest : min(latest, slotFinishTime) );

				boolean status = tp.plan();

				if (status)
					return true;
			}
		}

		return false;
	}

	private boolean checkLocationFor(Point location, WorkerUnit worker) {
		WorldPerspectiveCache cache = getPerspectiveCache();
		WorldPerspective perspective = cache.getPerspectiveFor(worker);
		Geometry map = perspective.getWorld().getMap();

		return !map.contains(location);
	}

	private Collection<WorkerUnit> filterByLocation(Point location) {
		Collection<WorkerUnit> workers = getWorkers();

		return workers.stream()
			.filter(w -> checkLocationFor(location, w))
			.collect(toList());
	}

}
