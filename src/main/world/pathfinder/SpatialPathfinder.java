package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class SpatialPathfinder {

	private List<Polygon> staticObstacles = null;

	private Point startLocation = null;

	private Point finishLocation = null;

	private List<Point> resultSpatialPath = null;

	public boolean isReady() {
		return staticObstacles != null
			&& startLocation != null
			&& finishLocation != null;
	}

	protected List<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		this.staticObstacles = immutable(staticObstacles);
	}

	public List<Point> getResultSpatialPath() {
		return resultSpatialPath;
	}

	protected void setResultSpatialPath(List<Point> resultSpatialPath) {
		this.resultSpatialPath = resultSpatialPath;
	}

	protected Point getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(Point startLocation) {
		this.startLocation = immutable(startLocation);
	}

	protected Point getFinishLocation() {
		return finishLocation;
	}

	public void setFinishLocation(Point finishLocation) {
		this.finishLocation = immutable(finishLocation);
	}

	public final boolean calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		List<Point> spatialPath = calculateSpatialPath();

		setResultSpatialPath(spatialPath);

		return spatialPath != null;
	}

	protected abstract List<Point> calculateSpatialPath();

}
