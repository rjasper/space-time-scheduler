package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import util.DurationConv;

import com.vividsolutions.jts.geom.Point;

public class DecomposedTrajectory implements Trajectory {
	
	private final LocalDateTime baseTime;

	private final List<Point> spatialPathComponent;
	
	private final List<Point> arcTimePathComponent;
	
	private transient SimpleTrajectory composedTrajectory = null;

	public DecomposedTrajectory(
		LocalDateTime baseTime,
		List<Point> spatialPathComponent,
		List<Point> arcTimePathComponent)
	{
		if (spatialPathComponent.size() == 1 || arcTimePathComponent.size() == 1)
			throw new IllegalArgumentException("illegal path component size");
		
		this.baseTime = baseTime;
		this.spatialPathComponent = spatialPathComponent;
		this.arcTimePathComponent = arcTimePathComponent;
	}

	@Override
	public boolean isEmpty() {
		return spatialPathComponent.isEmpty();
	}
	
	public boolean isComposed() {
		return composedTrajectory != null;
	}

	public LocalDateTime getBaseTime() {
		return baseTime;
	}

	public List<Point> getSpatialPathComponent() {
		return spatialPathComponent;
	}

	public List<Point> getArcTimePathComponent() {
		return arcTimePathComponent;
	}

	@Override
	public List<Point> getSpatialPath() {
		return getComposedTrajectory().getSpatialPath();
	}

	@Override
	public List<LocalDateTime> getTimes() {
		return getComposedTrajectory().getTimes();
	}

	@Override
	public Point getStartPoint() {
		if (isEmpty())
			return null;
		
		return getSpatialPathComponent().get(0);
	}

	@Override
	public Point getFinishPoint() {
		if (isEmpty())
			return null;
		
		List<Point> spatialPathComponent = getSpatialPathComponent();
		int n = spatialPathComponent.size();
		
		return spatialPathComponent.get(n-1);
	}

	@Override
	public LocalDateTime getStartTime() {
		if (isEmpty())
			return null;
		if (isComposed())
			return getComposedTrajectory().getStartTime();
		
		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();
		
		double t = arcTimePath.get(0).getY();
		Duration duration = DurationConv.ofSeconds(t);
		
		return baseTime.plus(duration);
	}

	@Override
	public LocalDateTime getFinishTime() {
		if (isEmpty())
			return null;
		if (isComposed())
			return getComposedTrajectory().getFinishTime();
		
		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();
		
		int n = arcTimePath.size();
		double t = arcTimePath.get(n-1).getY();
		Duration duration = DurationConv.ofSeconds(t);
		
		return baseTime.plus(duration);
	}

	public SimpleTrajectory getComposedTrajectory() {
		if (composedTrajectory == null) 
			composedTrajectory = compose();
		
		return composedTrajectory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((arcTimePathComponent == null) ? 0 : arcTimePathComponent.hashCode());
		result = prime * result
			+ ((baseTime == null) ? 0 : baseTime.hashCode());
		result = prime * result
			+ ((spatialPathComponent == null) ? 0 : spatialPathComponent.hashCode());
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
		if (arcTimePathComponent == null) {
			if (other.arcTimePathComponent != null)
				return false;
		} else if (!arcTimePathComponent.equals(other.arcTimePathComponent))
			return false;
		if (baseTime == null) {
			if (other.baseTime != null)
				return false;
		} else if (!baseTime.equals(other.baseTime))
			return false;
		if (spatialPathComponent == null) {
			if (other.spatialPathComponent != null)
				return false;
		} else if (!spatialPathComponent.equals(other.spatialPathComponent))
			return false;
		return true;
	}
	
	private SimpleTrajectory compose() {
		List<Point> spatialPath = getSpatialPathComponent();
		List<Point> arcTimePath = getArcTimePathComponent();
		LocalDateTime baseTime = getBaseTime();
		
		TrajectoryComposer builder = new TrajectoryComposer();
		
		builder.setSpatialPath(spatialPath);
		builder.setArcTimePath(arcTimePath);
		builder.setBaseTime(baseTime);
		
		builder.compose();
		
		return builder.getResultTrajectory();
	}

	@Override
	public String toString() {
		return "DecomposedTrajectory [spatialPath=" + spatialPathComponent
			+ ", arcTimePath=" + arcTimePathComponent + ", baseTime=" + baseTime + "]";
	}

}
