package world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;
import util.DurationConv;
import util.PathOperations;
import world.DecomposedTrajectory;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class VelocityPathfinder {
	
	protected static final double MIN_ARC = 0.0;
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder = new ForbiddenRegionBuilder();

	private double maxArc;

	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private List<Point> spatialPath = null;
	
	private double maxSpeed = 0.0;
	
	private DecomposedTrajectory resultTrajectory = null;
	
	private List<DynamicObstacle> resultEvadedObstacles = null;

	public boolean isReady() {
		return spatialPath != null
			&& maxSpeed > 0.0;
	}
	
	protected abstract LocalDateTime getBaseTime();

	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}

	protected double getMinArc() {
		return MIN_ARC;
	}

	protected double getMaxArc() {
		return maxArc;
	}

	protected void updateMaxArc() {
		maxArc = PathOperations.length( getSpatialPath() );
	}

	protected List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	protected List<Point> getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(List<Point> spatialPath) {
		if (spatialPath == null)
			throw new NullPointerException("path cannot be null");
//		if (spatialPath.getCoordinateSequence().getDimension() != 2)
//			throw new IllegalArgumentException("invalid path dimension");
		if (spatialPath.size() < 2)
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

	public DecomposedTrajectory getResultTrajectory() {
		return resultTrajectory;
	}

	private void setResultTrajectory(DecomposedTrajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	public List<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	}

	private void setResultEvadedObstacles(List<DynamicObstacle> resultEvadedObstacles) {
		this.resultEvadedObstacles = resultEvadedObstacles;
	}
	
	public final boolean calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		updateMaxArc();
		
		Collection<ForbiddenRegion> forbiddenRegions =
			calculateForbiddenRegions();
		
		List<Point> arcTimePath = calculateArcTimePath(forbiddenRegions);
		
		boolean reachable = arcTimePath != null;
		
		DecomposedTrajectory trajectory = reachable
			? buildTrajectory(arcTimePath)
			: null;
			
		List<DynamicObstacle> evasions = reachable
			? calculateEvadedObstacles(forbiddenRegions, arcTimePath)
			: null;
		
		setResultTrajectory(trajectory);
		setResultEvadedObstacles(evasions);
		
		return reachable;
	}
	
	private Collection<ForbiddenRegion> calculateForbiddenRegions() {
		LocalDateTime baseTime = getBaseTime();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		List<Point> spatialPath = getSpatialPath();
		
		ForbiddenRegionBuilder builder = getForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setDynamicObstacles(dynamicObstacles);
		builder.setSpatialPath(spatialPath);
		
		builder.calculate();
		
		return builder.getResultForbiddenRegions();
	}

	protected abstract List<Point> calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions);
	
	private List<DynamicObstacle> calculateEvadedObstacles(
		Collection<ForbiddenRegion> forbiddenRegions,
		List<Point> arcTimePath)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		Map<Point, DynamicObstacle> lookup = new HashMap<>();
		
		for (ForbiddenRegion fr : forbiddenRegions) {
			DynamicObstacle o = fr.getDynamicObstacle();
			Geometry r = fr.getRegion();
			
			for (Coordinate c : r.getCoordinates()) {
				Point p = builder.point(c.x, c.y);
				
				lookup.put(p, o);
			}
		}
		
		return arcTimePath.stream()
			.map(lookup::get)
			.collect(Collectors.toList());
	}

	private DecomposedTrajectory buildTrajectory(List<Point> arcTimePath) {
		LocalDateTime baseTime = getBaseTime();
		List<Point> spatialPath = getSpatialPath();
		
		return new DecomposedTrajectory(spatialPath, arcTimePath, baseTime);
	}

	protected double inSeconds(LocalDateTime time) {
		Duration duration = Duration.between(getBaseTime(), time);
		
		return DurationConv.inSeconds(duration);
	}
	
}
