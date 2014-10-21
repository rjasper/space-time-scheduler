package world;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class DynamicObstacle {
	
	private final Polygon shape;
	
	private final Trajectory trajectory;

	public DynamicObstacle(Polygon shape, LineString path, List<LocalDateTime> times) {
		// TODO check if polygon is empty
		// TODO check size of path and times
		
		this.shape = shape;
		this.trajectory = new Trajectory(path, times);
	}

	public DynamicObstacle(Polygon polygon, Trajectory trajectory) {
		// TODO check if polygon is empty
		
		this.shape = polygon;
		this.trajectory = trajectory;
	}

	public Polygon getShape() {
		return shape;
	}
	
	public Trajectory getTrajectory() {
		return trajectory;
	}

	public LineString getPath2d() {
		return trajectory == null
			? null
			: trajectory.getPath2d();
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
			+ "path="    + getPath2d()    + ", "
			+ "times="   + getTimes()   + "]";
	}

}
