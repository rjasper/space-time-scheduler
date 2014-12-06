package world.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class SpatialPathfinder {

	// TODO initialize with null (also update #isReady)
	private List<Polygon> staticObstacles = Collections.emptyList();

	private Point startLocation = null;

	private Point finishLocation = null;

	private List<Point> resultSpatialPath = null;

	public boolean isReady() {
		return startLocation != null
			&& finishLocation != null;
	}

	protected List<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		this.staticObstacles = new ArrayList<>(staticObstacles);
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
		this.startLocation = startLocation;
	}

	protected Point getFinishLocation() {
		return finishLocation;
	}

	public void setFinishLocation(Point finishLocation) {
		this.finishLocation = finishLocation;
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
