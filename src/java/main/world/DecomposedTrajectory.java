package world;

import java.time.LocalDateTime;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

public class DecomposedTrajectory {
	
	private final List<Point> spatialPath;
	
	private final List<Point> arcTimePath;
	
	private final LocalDateTime baseTime;
	
	private transient Trajectory composedTrajectory = null;

	public DecomposedTrajectory(
		List<Point> spatialPath,
		List<Point> arcTimePath,
		LocalDateTime baseTime)
	{
		this.spatialPath = spatialPath;
		this.arcTimePath = arcTimePath;
		this.baseTime = baseTime;
	}

	public List<Point> getSpatialPath() {
		return spatialPath;
	}

	public List<Point> getArcTimePath() {
		return arcTimePath;
	}

	public LocalDateTime getBaseTime() {
		return baseTime;
	}
	
	public Trajectory getComposedTrajectory() {
		if (composedTrajectory == null) 
			composedTrajectory = compose();
		
		return composedTrajectory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((arcTimePath == null) ? 0 : arcTimePath.hashCode());
		result = prime * result
			+ ((baseTime == null) ? 0 : baseTime.hashCode());
		result = prime * result
			+ ((spatialPath == null) ? 0 : spatialPath.hashCode());
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
		DecomposedTrajectory other = (DecomposedTrajectory) obj;
		if (arcTimePath == null) {
			if (other.arcTimePath != null)
				return false;
		} else if (!arcTimePath.equals(other.arcTimePath))
			return false;
		if (baseTime == null) {
			if (other.baseTime != null)
				return false;
		} else if (!baseTime.equals(other.baseTime))
			return false;
		if (spatialPath == null) {
			if (other.spatialPath != null)
				return false;
		} else if (!spatialPath.equals(other.spatialPath))
			return false;
		return true;
	}
	
	private Trajectory compose() {
		List<Point> spatialPath = getSpatialPath();
		List<Point> arcTimePath = getArcTimePath();
		LocalDateTime baseTime = getBaseTime();
		
		TrajectoryBuilder builder = new TrajectoryBuilder();
		
		builder.setSpatialPath(spatialPath);
		builder.setArcTimePath(arcTimePath);
		builder.setBaseTime(baseTime);
		
		builder.build();
		
		return builder.getResultTrajectory();
	}

	@Override
	public String toString() {
		return "DecomposedTrajectory [spatialPath=" + spatialPath
			+ ", arcTimePath=" + arcTimePath + ", baseTime=" + baseTime + "]";
	}

}
