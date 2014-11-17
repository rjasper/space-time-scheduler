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
		// TODO check if polygon or trajectory is empty
		
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

	public Point getStartLocation() {
		return trajectory.getStartLocation();
	}

	public Point getFinishLocation() {
		return trajectory.getFinishLocation();
	}

	public List<LocalDateTime> getTimes() {
		return trajectory == null
			? Collections.emptyList()
			: trajectory.getTimes();
	}

	public LocalDateTime getStartTime() {
		return trajectory.getStartTime();
	}

	public LocalDateTime getFinishTime() {
		return trajectory.getFinishTime();
	}

	@Override
	public String toString() {
		return "DynamicObstacle ["
			+ "shape=" + getShape() + ", "
			+ "spatialPath="    + getSpatialPath()    + ", "
			+ "times="   + getTimes()   + "]";
	}

}
