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
import java.util.Objects;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;
import util.CollectionsRequire;
import util.DurationConv;
import util.PathOperations;
import world.DecomposedTrajectory;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Point;

/**
 * The {@code VelocityPathfinder} is the abstract base class for velocity path
 * finders. A velocity path finder determines the arc-time mapping for
 * trajectories while avoiding any dynamic obstacles.
 * 
 * @author Rico
 */
public abstract class VelocityPathfinder {
	
	/**
	 * The start arc.
	 */
	protected static final double START_ARC = 0.0;
	
	/**
	 * The forbidden region builder.
	 */
	private ForbiddenRegionBuilder forbiddenRegionBuilder = new ForbiddenRegionBuilder();

	/**
	 * The finish arc.
	 */
	private double finishArc;

	/**
	 * The dynamic obstacles.
	 */
	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	/**
	 * The spatial path component of the trajectory.
	 */
	private List<Point> spatialPath = null;
	
	/**
	 * The maximum speed.
	 */
	private double maxSpeed = 0.0;
	
	/**
	 * The calculated trajectory.
	 */
	private DecomposedTrajectory resultTrajectory = null;
	
	/**
	 * The directly evaded dynamic obstacles.
	 */
	private Collection<DynamicObstacle> resultEvadedObstacles = null;

	/**
	 * @return {@code true} if all parameters are set.
	 */
	public boolean isReady() {
		return spatialPath != null
			&& maxSpeed > 0.0;
	}
	
	/**
	 * @return the base time for the arc-time component.
	 */
	protected abstract LocalDateTime getBaseTime();

	/**
	 * @return the forbidden region builder.
	 */
	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}

	/**
	 * @return the start arc.
	 */
	protected double getStartArc() {
		return START_ARC;
	}

	/**
	 * @return the finish arc.
	 */
	protected double getFinishArc() {
		return finishArc;
	}

	/**
	 * Recalculates the finish arc by calculating the total length of the
	 * spatial path.
	 */
	protected void updateFinishArc() {
		finishArc = PathOperations.length( getSpatialPath() );
	}

	/**
	 * @return the dynamic obstacles.
	 */
	protected List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	/**
	 * Sets the dynamic obstacles.
	 * 
	 * @param dynamicObstacles
	 * @throws NullPointerException
	 *             if dynamicObstacles is {@code null}.
	 */
	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		CollectionsRequire.requireContainsNonNull(dynamicObstacles, "dynamicObstacles");
		
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	/**
	 * @return the spatial path component of the trajectory to be calculated.
	 */
	protected List<Point> getSpatialPath() {
		return spatialPath;
	}

	/**
	 * Sets the spatial path component of the trajectory to be calculated.
	 * 
	 * @param spatialPath
	 * @throws NullPointerException
	 *             if spatialPath is {@code null}.
	 * @throws IllegalArgumentException
	 *             if spatialPath is empty.
	 */
	public void setSpatialPath(List<Point> spatialPath) {
		Objects.requireNonNull(spatialPath);
		
//		if (spatialPath == null)
//			throw new NullPointerException("path cannot be null");
		if (spatialPath.size() < 2)
			throw new IllegalArgumentException("path too short");
		
		this.spatialPath = immutable(spatialPath);
	}

	/**
	 * @return the maximum speed.
	 */
	protected double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Sets the maximum speed.
	 * 
	 * @param maxSpeed
	 * @throws IllegalArgumentException
	 *             if the maxSpeed is not positive finite.
	 */
	public void setMaxSpeed(double maxSpeed) {
		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0.0)
			throw new IllegalArgumentException("invalid maximum speed value");
		
		this.maxSpeed = maxSpeed;
	}

	/**
	 * @return the calculated trajectory.
	 */
	public DecomposedTrajectory getResultTrajectory() {
		return resultTrajectory;
	}

	/**
	 * Sets the calculated trajectory.
	 * 
	 * @param resultTrajectory
	 */
	private void setResultTrajectory(DecomposedTrajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	/**
	 * @return the evaded dynamic obstacles.
	 */
	public Collection<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	}

	/**
	 * Sets the evaded dynamic obstacles.
	 * 
	 * @param resultEvadedObstacles
	 */
	private void setResultEvadedObstacles(Collection<DynamicObstacle> resultEvadedObstacles) {
		this.resultEvadedObstacles = resultEvadedObstacles;
	}
	
	/**
	 * Calculates the velocity profile and trajectory from start to finish arc
	 * while avoiding all dynamic obstacles.
	 * 
	 * @return true if a trajectory could be calculated.
	 */
	public final boolean calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		updateFinishArc();
		
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
	
	/**
	 * Calculates the forbidden regions.
	 * 
	 * @return the forbidden regions.
	 */
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

	/**
	 * Calculates the arc-time path avoiding the forbidden regions.
	 * 
	 * @param forbiddenRegions
	 * @return the arc-time path.
	 */
	protected abstract List<Point> calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions);
	
	/**
	 * Calculates the evaded dynamic obstacles.
	 * 
	 * @param forbiddenRegions
	 * @param arcTimePath
	 * @return the evaded dynamic obstacles.
	 */
	private Collection<DynamicObstacle> calculateEvadedObstacles(
		Collection<ForbiddenRegion> forbiddenRegions,
		List<Point> arcTimePath)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		// create lookup table to map points to an obstacle
		Map<Point, DynamicObstacle> lookup = forbiddenRegions.stream()
			// TODO remove cast as soon as ECJ is able to infer type (Stream<SimpleEntry<Point, DynamicObstacle>>)
			// for each coordinate of each region
			.flatMap(r -> (Stream<SimpleEntry<Point, DynamicObstacle>>) Arrays.stream(r.getRegion().getCoordinates())
				.map(c -> builder.point(c.x, c.y))                        // map to a point
				.map(p -> new SimpleEntry<>(p, r.getDynamicObstacle())))  // map to an entry
			.collect(toMap(Entry::getKey, Entry::getValue, (u, v) -> u)); // collect map with no-overwrite merge
		
		// return a list of each obstacle met by a point in the path
		return arcTimePath.stream()
			.map(lookup::get)
			.collect(toSet());
	}

	/**
	 * Builds the trajectory with the given velocity profile.
	 * 
	 * @param arcTimePath the velocity profile.
	 * @return the trajectory.
	 */
	private DecomposedTrajectory buildTrajectory(List<Point> arcTimePath) {
		LocalDateTime baseTime = getBaseTime();
		List<Point> spatialPath = getSpatialPath();
		
		return new DecomposedTrajectory(baseTime, spatialPath, arcTimePath);
	}

	/**
	 * Converts the given time into a double value of seconds.
	 * 
	 * @param time
	 * @return the seconds.
	 */
	protected double inSeconds(LocalDateTime time) {
		Duration duration = Duration.between(getBaseTime(), time);
		
		return DurationConv.inSeconds(duration);
	}
	
}
