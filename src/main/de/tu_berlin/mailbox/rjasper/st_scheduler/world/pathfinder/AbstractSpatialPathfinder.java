package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;

/**
 * The {@code SpatialPathfinder} is the abstract base class for spatial path
 * finders. A spatial path finder searches the path connecting two locations
 * while avoiding any static obstacle.
 * 
 * @author Rico Jasper
 */
public abstract class AbstractSpatialPathfinder {

	/**
	 * The static obstacles to avoid.
	 */
	private Collection<StaticObstacle> staticObstacles = null;

	/**
	 * The start location.
	 */
	private Point startLocation = null;

	/**
	 * The finish location.
	 */
	private Point finishLocation = null;

	/**
	 * The calculated spatial path.
	 */
	private SpatialPath resultSpatialPath = null;

	/**
	 * @return the unmodifiable static obstacles to avoid.
	 */
	protected Collection<StaticObstacle> getStaticObstacles() {
		return staticObstacles;
	}

	/**
	 * Sets the static obstacles.
	 * 
	 * @param staticObstacles
	 * @throws NullPointerException
	 *             if staticObstacles is {@code null}.
	 */
	public void setStaticObstacles(Collection<StaticObstacle> staticObstacles) {
		CollectionsRequire.requireNonNull(staticObstacles, "staticObstacles");
		
		this.staticObstacles = unmodifiableCollection(staticObstacles);
	}

	/**
	 * @return the calculated spatial path.
	 */
	public SpatialPath getResultSpatialPath() {
		return resultSpatialPath;
	}

	/**
	 * Sets the calculated spatial path.
	 * 
	 * @param resultSpatialPath
	 */
	protected void setResultSpatialPath(SpatialPath resultSpatialPath) {
		this.resultSpatialPath = resultSpatialPath;
	}

	/**
	 * @return the start location.
	 */
	protected Point getStartLocation() {
		return startLocation;
	}

	/**
	 * Sets the start location.
	 * 
	 * @param startLocation
	 * @throws NullPointerException
	 *             if startLocation is {@code null}.
	 */
	public void setStartLocation(Point startLocation) {
		GeometriesRequire.requireValid2DPoint(startLocation, "startLocation");
		
		this.startLocation = startLocation;
	}

	/**
	 * @return the finish location.
	 */
	protected Point getFinishLocation() {
		return finishLocation;
	}

	/**
	 * Sets the finish location.
	 * 
	 * @param finishLocation
	 * @throws NullPointerException
	 *             if finishLocation is {@code null}.
	 */
	public void setFinishLocation(Point finishLocation) {
		GeometriesRequire.requireValid2DPoint(finishLocation, "finishLocation");
		
		this.finishLocation = finishLocation;
	}
	
	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	protected void checkParameters() {
		if (staticObstacles == null ||
			startLocation   == null ||
			finishLocation  == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
	}

	/**
	 * Calculates the path between the start and finish location. Avoids the
	 * specified obstacles.
	 * 
	 * @return {@code true} if a path connecting both locations could be found.
	 */
	public final boolean calculate() {
		checkParameters();
		SpatialPath spatialPath = calculateSpatialPath();
		setResultSpatialPath(spatialPath);

		return !spatialPath.isEmpty();
	}

	/**
	 * The actual implementation to calculate the spatial path.
	 * 
	 * @return the path.
	 */
	protected abstract SpatialPath calculateSpatialPath();

}
