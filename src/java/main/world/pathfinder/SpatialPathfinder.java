package world.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class SpatialPathfinder {
	
	private List<Polygon> staticObstacles = Collections.emptyList();
	
	private Point startPoint = null;
	
	private Point finishPoint = null;
	
	private LineString resultSpatialPath = null;
	
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

	public LineString getResultSpatialPath() {
		return resultSpatialPath;
	}

	protected void setResultSpatialPath(LineString resultSpatialPath) {
		this.resultSpatialPath = resultSpatialPath;
	}
	
	public boolean calculatePath() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		boolean status = calculatePathImpl();
		
		if (!status)
			setResultSpatialPath(null);
		
		return status;
	}
	
	protected abstract boolean calculatePathImpl();

}
