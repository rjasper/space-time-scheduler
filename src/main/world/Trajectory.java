package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public interface Trajectory {

	public abstract boolean isEmpty();

	public abstract List<Point> getSpatialPath();

	public abstract List<LocalDateTime> getTimes();

	public abstract LocalDateTime getFinishTime();

	public abstract LocalDateTime getStartTime();

	public abstract Point getFinishLocation();

	public abstract Point getStartLocation();

	public abstract Duration getDuration();

	public abstract double getLength();

	public abstract Geometry getTrace();

	public default Trajectory merge(Trajectory rhs) {
		List<Point> lhsSpatialPath = getSpatialPath();
		List<Point> rhsSpatialPath = rhs.getSpatialPath();
		List<LocalDateTime> lhsTimes = getTimes();
		List<LocalDateTime> rhsTimes = rhs.getTimes();

		int n = lhsSpatialPath.size() + rhsSpatialPath.size();

		List<Point> spatialPath = new ArrayList<>(n);
		List<LocalDateTime> times = new ArrayList<>(n);

		spatialPath.addAll(lhsSpatialPath);
		spatialPath.addAll(rhsSpatialPath);

		times.addAll(lhsTimes);
		times.addAll(rhsTimes);

		return new SimpleTrajectory(spatialPath, times);
	};

}