package world.pathfinder;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;
import util.DurationConv;
import util.PathOperations;
import world.DecomposedTrajectory;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Point;

public abstract class VelocityPathfinder {
	
	protected static final double MIN_ARC = 0.0;
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder = new ForbiddenRegionBuilder();

	private double maxArc;

	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private List<Point> spatialPath = null;
	
	private double maxSpeed = 0.0;
	
	private DecomposedTrajectory resultTrajectory = null;
	
	private Collection<DynamicObstacle> resultEvadedObstacles = null;

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
		
		this.spatialPath = immutable(spatialPath);
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

	public Collection<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	}

	private void setResultEvadedObstacles(Collection<DynamicObstacle> resultEvadedObstacles) {
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
			
		Collection<DynamicObstacle> evasions = reachable
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
	
	private Collection<DynamicObstacle> calculateEvadedObstacles(
		Collection<ForbiddenRegion> forbiddenRegions,
		List<Point> arcTimePath)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		// create lookup table to map points to an obstacle
		Map<Point, DynamicObstacle> lookup = forbiddenRegions.stream()
			// TODO remove cast as soon as ECJ is able to infer type (Stream<SimpleEntry<Point, DynamicObstacle>>)
			// for each coordinate of each region
			.flatMap(fr -> (Stream<SimpleEntry<Point, DynamicObstacle>>) Arrays.stream(fr.getRegion().getCoordinates())
				.map(c -> builder.point(c.x, c.y))                        // map to a point
				.map(p -> new SimpleEntry<>(p, fr.getDynamicObstacle()))) // map to an entry
			.collect(toMap(Entry::getKey, Entry::getValue, (u, v) -> u)); // collect map with no-overwrite merge
		
		// return a list of each obstacle met by a point in the path
		return arcTimePath.stream()
			.map(lookup::get)
			.collect(toSet());
	}

	private DecomposedTrajectory buildTrajectory(List<Point> arcTimePath) {
		LocalDateTime baseTime = getBaseTime();
		List<Point> spatialPath = getSpatialPath();
		
		return new DecomposedTrajectory(baseTime, spatialPath, arcTimePath);
	}

	protected double inSeconds(LocalDateTime time) {
		Duration duration = Duration.between(getBaseTime(), time);
		
		return DurationConv.inSeconds(duration);
	}
	
}
