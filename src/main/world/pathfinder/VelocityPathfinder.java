package world.pathfinder;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import jts.geom.immutable.ImmutablePoint;
import util.CollectionsRequire;
import util.TimeConv;
import world.ArcTimePath;
import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.SpatialPath;

/**
 * The {@code VelocityPathfinder} is the abstract base class for velocity path
 * finders. A velocity path finder determines the arc-time mapping for
 * trajectories while avoiding any dynamic obstacles.
 * 
 * @author Rico
 */
public abstract class VelocityPathfinder {
	
	/**
	 * The forbidden region builder.
	 */
	private ForbiddenRegionBuilder forbiddenRegionBuilder = new ForbiddenRegionBuilder();

	/**
	 * The minimum arc.
	 */
	private double minArc = Double.NaN;

	/**
	 * The maximum arc.
	 */
	private double maxArc = Double.NaN;

	/**
	 * The start arc.
	 */
	private double startArc = Double.NaN;

	/**
	 * The finish arc.
	 */
	private double finishArc = Double.NaN;

	/**
	 * The dynamic obstacles.
	 */
	private Collection<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	/**
	 * The spatial path component of the trajectory.
	 */
	private SpatialPath spatialPath = null;
	
	/**
	 * The maximum speed.
	 */
	private double maxSpeed = Double.NaN;
	
	/**
	 * The calculated trajectory.
	 */
	private DecomposedTrajectory resultTrajectory = null;
	
	/**
	 * The directly evaded dynamic obstacles.
	 */
	private Collection<DynamicObstacle> resultEvadedObstacles = null;

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
	 * Sets the minimum arc value.
	 * 
	 * @param minArc the startArc to set
	 * @throws IllegalArgumentException
	 *             if the minArc is not non-negative finite.
	 */
	public void setMinArc(double minArc) {
		if (!Double.isFinite(minArc) || minArc < 0.0)
			throw new IllegalArgumentException("startArc is not non-negative finite");
		
		this.minArc = minArc;
	}

	/**
	 * @return the minimum arc.
	 */
	protected double getMinArc() {
		return minArc;
	}

	/**
	 * Sets the maximum arc value.
	 * 
	 * @param maxArc
	 * @throws IllegalArgumentException
	 *             if the maxArc is not non-negative finite.
	 */
	public void setMaxArc(double maxArc) {
		if (!Double.isFinite(maxArc) || maxArc < 0.0)
			throw new IllegalArgumentException("finishArc is not non-negative finite");
		
		this.maxArc = maxArc;
	}

	/**
	 * @return the maximum arc.
	 */
	protected double getMaxArc() {
		return maxArc;
	}
	
	/**
	 * @return the start arc value.
	 */
	protected double getStartArc() {
		return startArc;
	}

	/**
	 * Sets the start arc value.
	 * 
	 * @param startArc the startArc to set
	 * @throws IllegalArgumentException
	 *             if the maxArc is not non-negative finite.
	 */
	public void setStartArc(double startArc) {
		if (!Double.isFinite(startArc) || startArc < 0.0)
			throw new IllegalArgumentException("startArc is not non-negative finite");
		
		this.startArc = startArc;
	}

	/**
	 * @return the finish arc value.
	 */
	protected double getFinishArc() {
		return finishArc;
	}

	/**
	 * Sets the finish arc value.
	 * 
	 * @param finishArc the startArc to set
	 * @throws IllegalArgumentException
	 *             if the maxArc is not non-negative finite.
	 */
	public void setFinishArc(double finishArc) {
		if (!Double.isFinite(finishArc) || finishArc < 0.0)
			throw new IllegalArgumentException("startArc is not non-negative finite");
		
		this.finishArc = finishArc;
	}

	/**
	 * @return the unmodifiable dynamic obstacles.
	 */
	protected Collection<DynamicObstacle> getDynamicObstacles() {
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
		CollectionsRequire.requireNonNull(dynamicObstacles, "dynamicObstacles");
		
		this.dynamicObstacles = unmodifiableCollection(dynamicObstacles);
	}

	/**
	 * @return the spatial path component of the trajectory to be calculated.
	 */
	protected SpatialPath getSpatialPath() {
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
	public void setSpatialPath(SpatialPath spatialPath) {
		this.spatialPath = Objects.requireNonNull(spatialPath);
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
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	protected void checkParameters() {
		if (spatialPath == null     ||
			Double.isNaN(minArc   ) ||
			Double.isNaN(maxArc   ) ||
			Double.isNaN(startArc ) ||
			Double.isNaN(finishArc) ||
			Double.isNaN(maxSpeed ))
		{
			throw new IllegalStateException("some parameters are not set");
		}
		if (startArc > finishArc || startArc < minArc || finishArc > maxArc)
			throw new IllegalArgumentException("start or finish arc not within valid range");
	}

	/**
	 * Calculates the velocity profile and trajectory from start to finish arc
	 * while avoiding all dynamic obstacles.
	 * 
	 * @return true if a trajectory could be calculated.
	 */
	public final boolean calculate() {
		checkParameters();
		
		Collection<ForbiddenRegion> forbiddenRegions;
		ArcTimePath arcTimePath;
		boolean reachable;
		
		if (getSpatialPath().isEmpty()) {
			// null since never used in this case
			forbiddenRegions = null;
			arcTimePath = null;
			
			reachable = false;
		} else {
			forbiddenRegions = calculateForbiddenRegions();
			arcTimePath = calculateArcTimePath(forbiddenRegions);
			reachable = !arcTimePath.isEmpty();
		}
		
		if (reachable) {
			setResultTrajectory(
				buildTrajectory(arcTimePath));
			setResultEvadedObstacles(
				calculateEvadedObstacles(forbiddenRegions, arcTimePath));
		} else {
			setResultTrajectory(
				DecomposedTrajectory.empty());
			setResultEvadedObstacles(
				emptyList());
		}
		
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
		SpatialPath spatialPath = getSpatialPath();
		
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
	protected abstract ArcTimePath calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions);
	
	/**
	 * Calculates the evaded dynamic obstacles.
	 * 
	 * @param forbiddenRegions
	 * @param arcTimePath
	 * @return the evaded dynamic obstacles.
	 */
	private Collection<DynamicObstacle> calculateEvadedObstacles(
		Collection<ForbiddenRegion> forbiddenRegions,
		ArcTimePath arcTimePath)
	{
		// create lookup table to map points to an obstacle
		Map<ImmutablePoint, DynamicObstacle> lookup = forbiddenRegions.stream()
			// TODO remove cast as soon as ECJ is able to infer type (Stream<SimpleEntry<Point, DynamicObstacle>>)
			// for each coordinate of each region
			.flatMap(r -> (Stream<SimpleEntry<ImmutablePoint, DynamicObstacle>>) Arrays.stream(r.getRegion().getCoordinates())
			.map(c -> immutablePoint(c.x, c.y))                      // map to a point
			.map(p -> new SimpleEntry<>(p, r.getDynamicObstacle()))) // map to an entry
			.collect(toMap(Entry::getKey, Entry::getValue, (u, v) -> u)); // collect map with no-overwrite merge
		
		// return a list of each obstacle met by a point in the path
		return arcTimePath.getPoints().stream()
			.map(lookup::get)
			.collect(toSet());
	}

	/**
	 * Builds the trajectory with the given velocity profile.
	 * 
	 * @param arcTimePath the velocity profile.
	 * @return the trajectory.
	 */
	private DecomposedTrajectory buildTrajectory(ArcTimePath arcTimePath) {
		LocalDateTime baseTime = getBaseTime();
		SpatialPath spatialPath = getSpatialPath();
		
		return new DecomposedTrajectory(baseTime, spatialPath, arcTimePath);
	}

	/**
	 * Converts the given time into a double value of seconds.
	 * 
	 * @param time
	 * @return the seconds.
	 */
	protected double inSeconds(LocalDateTime time) {
//		return TimeConv.timeToSecondsExact(time, getBaseTime());
		return TimeConv.timeToSeconds(time, getBaseTime());
	}
	
}
