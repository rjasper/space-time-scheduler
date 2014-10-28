package world;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

public class Trajectory {
	
	private final LineString spatialPath;
	
	private final List<LocalDateTime> times;

	public Trajectory(LineString spatialPath, List<LocalDateTime> times) {
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
		Trajectory other = (Trajectory) obj;
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

	public boolean isEmpty() {
		return size() == 0;
	}

	public LineString getSpatialPath() {
		return spatialPath;
	}

	public List<LocalDateTime> getTimes() {
		return times;
	}
	
	public LocalDateTime getLastTime() {
		return times.get(size()-1);
	}
	
	public int size() {
		return times.size();
	}

	@Override
	public String toString() {
		return "Trajectory [path2d=" + spatialPath + ", times=" + times + "]";
	}

}
