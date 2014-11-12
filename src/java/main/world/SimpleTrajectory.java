package world;

import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

public class SimpleTrajectory implements Trajectory {
	
	private final List<Point> spatialPath;
	
	private final List<LocalDateTime> times;

	public SimpleTrajectory(List<Point> spatialPath, List<LocalDateTime> times) {
		// TODO check sizes
		
		this.spatialPath = spatialPath;
		this.times = Collections.unmodifiableList(new ArrayList<>(times));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spatialPath == null) ? 0 : spatialPath.hashCode());
		result = prime * result + ((times == null) ? 0 : times.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleTrajectory other = (SimpleTrajectory) obj;
		if (spatialPath == null) {
			if (other.spatialPath != null)
				return false;
		} else if (!spatialPath.equals(other.spatialPath))
			return false;
		if (times == null) {
			if (other.times != null)
				return false;
		} else if (!times.equals(other.times))
			return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public List<Point> getSpatialPath() {
		return unmodifiableList(spatialPath);
	}

	@Override
	public List<LocalDateTime> getTimes() {
		return unmodifiableList(times);
	}
	
	public int size() {
		return times.size();
	}

	@Override
	public String toString() {
		return "Trajectory [path2d=" + spatialPath + ", times=" + times + "]";
	}

}
