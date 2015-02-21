package scheduler;

import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import world.Trajectory;

/**
 * A reference to the {@link WorkerUnit} representation maintained by the
 * scheduler.
 * 
 * @author Rico
 * @see WorkerUnit
 * @see Scheduler
 */
public class WorkerUnitReference  {
	
	/**
	 * The referenced worker.
	 */
	private final WorkerUnit worker;

	/**
	 * Constructs a new {@code WorkerUnitReference} of the given worker.
	 * 
	 * @param worker
	 */
	public WorkerUnitReference(WorkerUnit worker) {
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	/**
	 * @return the worker's ID.
	 */
	public String getId() {
		return worker.getId();
	}
	
	/**
	 * @return the actual worker.
	 */
	WorkerUnit getActual() {
		return worker;
	}

	/**
	 * @return the physical shape of the worker.
	 */
	public ImmutablePolygon getShape() {
		return worker.getShape();
	}

	/**
	 * @return the radius of this worker's shape.
	 */
	public double getRadius() {
		return worker.getRadius();
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return worker.getMaxSpeed();
	}

	/**
	 * @return the initial location of the worker where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return worker.getInitialLocation();
	}

	/**
	 * @return the initial time of the worker when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return worker.getInitialTime();
	}

	/**
	 * Calculates a trajectory from all obstacle segments merged together.
	 * 
	 * @return the merged trajectory.
	 */
	public Trajectory calcTrajectory() {
		return worker.calcTrajectory();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return worker.toString();
	}
	
}
