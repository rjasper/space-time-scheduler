package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

public interface Trajectory {

	public abstract boolean isEmpty();

	public abstract List<Point> getSpatialPath();

	public abstract List<LocalDateTime> getTimes();

	public default Point getStartLocation() {
		if (isEmpty())
			return null;

		List<Point> spatialPath = getSpatialPath();

		return spatialPath.get(0);
	};

	public default Point getFinishLocation() {
		if (isEmpty())
			return null;

		List<Point> spatialPath = getSpatialPath();
		int n = spatialPath.size();

		return spatialPath.get(n-1);
	};

	public default LocalDateTime getStartTime() {
		if (isEmpty())
			return null;

		List<LocalDateTime> times = getTimes();

		return times.get(0);
	};

	public default LocalDateTime getFinishTime() {
		if (isEmpty())
			return null;

		List<LocalDateTime> times = getTimes();
		int n = times.size();

		return times.get(n-1);
	};

	public default Duration getDuration() {
		return Duration.between(getStartTime(), getFinishTime());
	}

	public abstract double getLength();

}