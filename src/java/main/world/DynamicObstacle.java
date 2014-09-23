package world;

import java.time.LocalDateTime;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class DynamicObstacle {
	
	private final Polygon polygon;
	
	private final Trajectory trajectory;

	public DynamicObstacle(Polygon polygon, LineString path, List<LocalDateTime> times) {
		// TODO check if polygon is empty
		// TODO check size of path and times
		
		this.polygon = polygon;
		this.trajectory = new Trajectory(path, times);
	}

	public DynamicObstacle(Polygon polygon, Trajectory trajectory) {
		// TODO check if polygon is empty
		
		this.polygon = polygon;
		this.trajectory = trajectory;
	}

	public Polygon getPolygon() {
		return polygon;
	}
	
	public Trajectory getTrajectory() {
		return trajectory;
	}

	public LineString getPath2d() {
		return trajectory.getPath2d();
	}

	public List<LocalDateTime> getTimes() {
		return trajectory.getTimes();
	}

	@Override
	public String toString() {
		return "DynamicObstacle ["
			+ "polygon=" + getPolygon() + ", "
			+ "path="    + getPath2d()    + ", "
			+ "times="   + getTimes()   + "]";
	}

}
