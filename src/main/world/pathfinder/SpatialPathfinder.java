package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The {@code SpatialPathfinder} is the abstract base class for spatial
 * pathfinders. A spatial pathfinder searches the path connecting two locations
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
	 */
	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
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
	 */
	public void setStartLocation(Point startLocation) {
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
	 */
	public void setFinishLocation(Point finishLocation) {
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
