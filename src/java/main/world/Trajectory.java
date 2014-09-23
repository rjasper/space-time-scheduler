package world;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

public class Trajectory {
	
	private final LineString path2d;
	
	private final List<LocalDateTime> times;

	public Trajectory(LineString path2d, List<LocalDateTime> times) {
		// TODO check sizes
		
		this.path2d = path2d;
		this.times = Collections.unmodifiableList(new ArrayList<>(times));
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}

	public LineString getPath2d() {
		return path2d;
	}

	public List<LocalDateTime> getTimes() {
		return times;
	}
	
	public int size() {
		return times.size();
	}

	@Override
	public String toString() {
		return "Trajectory [path2d=" + path2d + ", times=" + times + "]";
	}

}
