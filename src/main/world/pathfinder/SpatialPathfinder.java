package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jts.geom.util.GeometriesRequire;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
	private List<Polygon> staticObstacles = null;

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
	private List<Point> resultSpatialPath = null;

	/**
	 * @return {@code true} if all parameters are set.
	 */
	public boolean isReady() {
		return staticObstacles != null
			&& startLocation != null
			&& finishLocation != null;
	}

	/**
	 * @return the static obstacles to avoid.
	 */
	protected List<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	/**
	 * Sets the static obstacles.
	 * 
	 * @param staticObstacles
	 * @throws NullPointerException
	 *             if staticObstacles is {@code null}.
	 */
	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		Objects.requireNonNull(staticObstacles, "staticObstacles");
		
		this.staticObstacles = unmodifiableList(immutable(staticObstacles));
	}

	/**
	 * @return the calculated spatial path.
	 */
	public List<Point> getResultSpatialPath() {
		return resultSpatialPath;
	}

	/**
	 * Sets the calculated spatial path.
	 * 
	 * @param resultSpatialPath
	 */
	protected void setResultSpatialPath(List<Point> resultSpatialPath) {
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
		
		this.startLocation = immutable(startLocation);
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
		
		this.finishLocation = immutable(finishLocation);
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

		List<Point> spatialPath = calculateSpatialPath();

		setResultSpatialPath(spatialPath);

		return spatialPath != null;
	}

	/**
	 * The actual implementation to calculate the spatial path.
	 * 
	 * @return the path.
	 */
	protected abstract List<Point> calculateSpatialPath();

}
