package world;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class DynamicObstacle {
	
	private final Polygon shape;
	
	private final Trajectory trajectory;

	public DynamicObstacle(Polygon shape, Trajectory trajectory) {
		// TODO check if polygon is empty
		
		this.shape = shape;
		this.trajectory = trajectory;
	}

	public Polygon getShape() {
		return shape;
	}
	
	public Trajectory getTrajectory() {
		return trajectory;
	}

	public List<Point> getSpatialPath() {
		return trajectory == null
			? null
			: trajectory.getSpatialPath();
	}

	public List<LocalDateTime> getTimes() {
		return trajectory == null
			? Collections.emptyList()
			: trajectory.getTimes();
	}

	@Override
	public String toString() {
		return "DynamicObstacle ["
			+ "shape=" + getShape() + ", "
			+ "spatialPath="    + getSpatialPath()    + ", "
			+ "times="   + getTimes()   + "]";
	}

}
