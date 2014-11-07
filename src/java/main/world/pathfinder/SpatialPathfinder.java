package world.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class SpatialPathfinder {
	
	private List<Polygon> staticObstacles = Collections.emptyList();
	
	private Point startPoint = null;
	
	private Point finishPoint = null;
	
	private List<Point> resultSpatialPath = null;
	
	public boolean isReady() {
		return startPoint != null
			&& finishPoint != null;
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
	
	protected Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	protected Point getFinishPoint() {
		return finishPoint;
	}

	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = finishPoint;
	}

	public final boolean calculatePath() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		boolean status = calculatePathImpl();
		
		if (!status)
			setResultSpatialPath(null);
		
		return status;
	}
	
	protected abstract boolean calculatePathImpl();

}
