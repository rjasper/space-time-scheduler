package tasks;

import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pickers.LocationIteratorFactory;
import pickers.WorkerUnitSlotIterator.WorkerUnitSlot;
import pickers.WorkerUnitSlotIteratorFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import world.World;

public class Scheduler {
	
	public static final int MAX_LOCATION_PICKS = 10;
	
	private final World world;
	
	private final List<WorkerUnit> workers;

	public Scheduler(World world, Collection<WorkerUnit> workers) {
		if (!world.isReady())
			throw new IllegalStateException("world must be ready");
		if (workers == null)
			throw new NullPointerException("workers cannot be null");
		
		this.world = world;
		this.workers = new ArrayList<>(workers);
	}
	
	private World getWorld() {
		return world;
	}

	private List<WorkerUnit> getWorkers() {
		return workers;
	}

	public boolean schedule(Specification spec) {
		World world = getWorld();
		List<WorkerUnit> workers = getWorkers();
		Collection<Polygon> obstacles = world.getPolygonMap();
		Geometry locationSpace = world.space(spec.getLocationSpace());
		LocalDateTime earliest = spec.getEarliestStartTime();
		LocalDateTime latest = spec.getLatestStartTime();
		Duration duration = spec.getDuration();

		TaskPlanner tp = new TaskPlanner();
		
		tp.setWorkerPool(workers);
		tp.setStaticObstacles(obstacles);
		tp.setDuration(duration);
		
		LocationIteratorFactory locations =
			new LocationIteratorFactory(locationSpace, MAX_LOCATION_PICKS);
		
		for (Point loc : locations) {
			WorkerUnitSlotIteratorFactory workerSlots =
				new WorkerUnitSlotIteratorFactory(workers, loc, earliest, latest, duration);
			
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
				
				if (status) {
					w.addTask(tp.getResultTask(), tp.getResultToTask(), tp.getResultFromTask());
					return true;
				}
			}
		}
		
		return false;
	}

}
