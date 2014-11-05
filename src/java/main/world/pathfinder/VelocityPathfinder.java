package world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

import util.DurationConv;
import world.DynamicObstacle;
import world.Trajectory;
import world.TrajectoryBuilder;

public abstract class VelocityPathfinder {
	
	protected static final double MIN_ARC = 0.0;
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder = new ForbiddenRegionBuilder();

	private TrajectoryBuilder trajBuilder = new TrajectoryBuilder();

	private double maxArc;

	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private LineString spatialPath = null;
	
	private double maxSpeed = 0.0;
	
	private Trajectory resultTrajectory = null;
	
	private List<DynamicObstacle> resultEvadedObstacles = null;

	public boolean isReady() {
		return spatialPath != null
			&& maxSpeed > 0.0;
	}
	
	protected abstract LocalDateTime getBaseTime();

	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}

	private TrajectoryBuilder getTrajectoryBuilder() {
		return trajBuilder;
	}
	
	protected double getMinArc() {
		return MIN_ARC;
	}

	protected double getMaxArc() {
		return maxArc;
	}

	protected void updateMaxArc() {
		maxArc = getSpatialPath().getLength();
	}

	protected List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	protected LineString getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(LineString spatialPath) {
		if (spatialPath == null)
			throw new NullPointerException("path cannot be null");
		if (spatialPath.getCoordinateSequence().getDimension() != 2)
			throw new IllegalArgumentException("invalid path dimension");
		if (spatialPath.getNumPoints() < 2)
			throw new IllegalArgumentException("path too short");
		
		this.spatialPath = spatialPath;
	}

	protected double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		if (maxSpeed <= 0.0 || !Double.isFinite(maxSpeed))
			throw new IllegalArgumentException("invalid maximum speed value");
		
		this.maxSpeed = maxSpeed;
	}

	public Trajectory getResultTrajectory() {
		return resultTrajectory;
	}

	private void setResultTrajectory(Trajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	public List<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	}

	private void setResultEvadedObstacles(List<DynamicObstacle> resultEvadedObstacles) {
		this.resultEvadedObstacles = resultEvadedObstacles;
	}
	
	public final boolean calculateTrajectory() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		updateMaxArc();
		
		Collection<ForbiddenRegion> forbiddenRegions =
			calculateForbiddenRegions();
		
		LineString arcTimePath = calculateArcTimePath(forbiddenRegions);
		
		boolean reachable = arcTimePath != null;
		
		Trajectory trajectory = reachable
			? buildTrajectory(arcTimePath)
			: null;
		
		setResultTrajectory(trajectory);
		
		if (!reachable)
			setResultEvadedObstacles(null);
		
		return reachable;
	}
	
	private Collection<ForbiddenRegion> calculateForbiddenRegions() {
		LocalDateTime baseTime = getBaseTime();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		LineString spatialPath = getSpatialPath();
		
		ForbiddenRegionBuilder builder = getForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setDynamicObstacles(dynamicObstacles);
		builder.setSpatialPath(spatialPath);
		
		builder.calculate();
		
		return builder.getResultForbiddenRegions();
	}

	protected abstract LineString calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions);

	private Trajectory buildTrajectory(LineString arcTimePath) {
		LocalDateTime baseTime = getBaseTime();
		LineString spatialPath = getSpatialPath();
		TrajectoryBuilder trajBuilder = getTrajectoryBuilder();

		trajBuilder.setBaseTime(baseTime);
		trajBuilder.setSpatialPath(spatialPath);
		trajBuilder.setArcTimePath(arcTimePath);
		
		trajBuilder.build();
		
		return trajBuilder.getResultTrajectory();
	}

	protected double inSeconds(LocalDateTime time) {
		Duration duration = Duration.between(getBaseTime(), time);
		
		return DurationConv.inSeconds(duration);
	}
	
}
