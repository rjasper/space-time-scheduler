package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import util.DurationConv;
import util.PathOperations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class DecomposedTrajectory extends CachedTrajectory {

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
	public Point getStartLocation() {
		if (isEmpty())
			return null;

		return getSpatialPathComponent().get(0);
	}

	@Override
	public Point getFinishLocation() {
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
	protected double calcLength() {
		return PathOperations.length( getSpatialPathComponent() );
	}

	@Override
	protected Geometry calcTrace() {
		return PathOperations.calcTrace( getSpatialPathComponent() );
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
