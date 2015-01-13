package world.pathfinder;

import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;

import jts.geom.util.GeometriesRequire;
import util.CollectionsRequire;
import world.SpatialPath;
import world.StaticObstacle;

import com.vividsolutions.jts.geom.Point;

/**
 * The {@code SpatialPathfinder} is the abstract base class for spatial path
 * finders. A spatial path finder searches the path connecting two locations
 * while avoiding any static obstacle.
 * 
 * @author Rico
 */
public abstract class SpatialPathfinder {

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
	 * @return {@code true} if all parameters are set.
	 */
	public boolean isReady() {
		return staticObstacles != null
			&& startLocation != null
			&& finishLocation != null;
	}

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
		CollectionsRequire.requireContainsNonNull(staticObstacles, "staticObstacles");
		
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
	 * Calculates the path between the start and finish location. Avoids the
	 * specified obstacles.
	 * 
	 * @return {@code true} if a path connecting both locations could be found.
	 */
	public final boolean calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		SpatialPath spatialPath = calculateSpatialPath();

		setResultSpatialPath(spatialPath);

		return spatialPath != null;
	}

	/**
	 * The actual implementation to calculate the spatial path.
	 * 
	 * @return the path.
	 */
	protected abstract SpatialPath calculateSpatialPath();

}
