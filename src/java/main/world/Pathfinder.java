package world;

import static matlab.ConvertOperations.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import matlab.AccessOperations;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Pathfinder {
	
	private Point startingPoint = null;
	
	private Point finishPoint = null;
	
	private LocalDateTime startingTime = null;
	
	private LocalDateTime latestFinishTime = null;
	
	private Duration spareTime = null;
	
	private double maxSpeed = 0.;
	
	private List<Polygon> staticObstacles = new LinkedList<>();
	
	private List<DynamicObstacle> dynamicObstacles = new LinkedList<>();
	
	private boolean minimumTime = true;
	
//	private LineString path;
	
	private Trajectory trajectory;
	
	private List<DynamicObstacle> evadedObstacles;
	
	private final MatlabProxy proxy;
	
	private final AccessOperations access;
	
	public Pathfinder(MatlabProxy proxy) {
		this.proxy = proxy;
		this.access = new AccessOperations(proxy);
	}
	
	public boolean isReady() {
		return startingPoint != null
			&& finishPoint != null
			&& startingTime != null
			&& latestFinishTime != null
			&& (!minimumTime || spareTime != null)
			&& startingTime.compareTo(latestFinishTime) < 0
			&& maxSpeed > 0.;
	}
	
	public boolean isPathFound() {
		return trajectory != null;
	}

	public boolean isMinimumTime() {
		return minimumTime;
	}

	public void useMinimumFinishTime() {
		minimumTime = true;
	}

	public void useSpecifiedFinishTime() {
		minimumTime = false;
	}

	public Point getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(Point startingPoint) {
		this.startingPoint = startingPoint;
	}

	public Point getFinishPoint() {
		return finishPoint;
	}

	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = finishPoint;
	}

	public LocalDateTime getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(LocalDateTime startingTime) {
		this.startingTime = startingTime;
	}

	public LocalDateTime getLatestFinishTime() {
		return latestFinishTime;
	}

	public void setLatestFinishTime(LocalDateTime latestFinishTime) {
		this.latestFinishTime = latestFinishTime;
	}

	private Duration getSpareTime() {
		return spareTime;
	}

	public void setSpareTime(Duration spareTime) {
		if (spareTime == null)
			throw new NullPointerException("spareTime cannot be null");
		if (spareTime.isNegative())
			throw new IllegalArgumentException("spareTime must be non-negative");
		
		this.spareTime = spareTime;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	private List<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	private List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}
	
	private DynamicObstacle[] createDynamicObstaclesArray() {
		List<DynamicObstacle> obstacles = getDynamicObstacles();
		
		return obstacles.toArray(new DynamicObstacle[obstacles.size()]);
	}

	public void setMaxSpeed(double maxSpeed) {
		if (maxSpeed <= 0.)
			throw new IllegalArgumentException("maximum speed must be positive");
		
		this.maxSpeed = maxSpeed;
	}
	
	public void addStaticObstacle(Polygon obstacle) {
//		if (obstacle == null)
//			throw new NullPointerException("obstacle cannot be null");
		
		Collection<Polygon> collection = getStaticObstacles();
		
		collection.add(obstacle);
	}
	
	public void addAllStaticObstacles(Collection<Polygon> obstacles) {
		Collection<Polygon> collection = getStaticObstacles();
		
		collection.addAll(obstacles);
	}
	
	public void clearStaticObstacles() {
		Collection<Polygon> collection = getStaticObstacles();
		
		collection.clear();
	}
	
	public void addDynamicObstacle(DynamicObstacle obstacle) {
		Collection<DynamicObstacle> collection = getDynamicObstacles();
		
		collection.add(obstacle);
	}
	
	public void addAllDynamicObstacles(Collection<DynamicObstacle> obstacles) {
		Collection<DynamicObstacle> collection = getDynamicObstacles();
		
		collection.addAll(obstacles);
	}
	
	public void clearDynamicObstacles() {
		Collection<DynamicObstacle> collection = getDynamicObstacles();
		
		collection.clear();
	}
	
	public Trajectory getTrajectory() {
		return trajectory;
	}

	private void setTrajectory(Trajectory trajectory) {
		this.trajectory = trajectory;
	}

//	private void setPath(LineString path) {
//		this.path = path;
//	}
	
	public List<DynamicObstacle> getEvadedObstacles() {
		return evadedObstacles;
	}

	private void setEvadedObstacles(List<DynamicObstacle> evadedObstacles) {
		this.evadedObstacles = evadedObstacles;
	}

	private MatlabProxy getProxy() {
		return proxy;
	}

	private AccessOperations getAccess() {
		return access;
	}
	
	public void calculatePath() {
		if (!isReady())
			throw new IllegalStateException("invalid parameters");
		
		Point startingPoint = getStartingPoint();
		Point finishPoint = getFinishPoint();
		LocalDateTime startingTime = getStartingTime();
		LocalDateTime latestFinishTime = getLatestFinishTime();
		Duration spareTime = getSpareTime();
		double maxSpeed = getMaxSpeed();
		Collection<Polygon> staticObstacles = getStaticObstacles();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		
		MatlabProxy m = getProxy();
		AccessOperations acc = getAccess();
		
		try {
			acc.assingPoint("I", j2mPoint(startingPoint, 2));
			acc.assingPoint("F", j2mPoint(finishPoint, 2));
			m.setVariable("v_max", maxSpeed);
			m.setVariable("t_start", j2mLocalDateTime(startingTime));
			m.setVariable("t_end", j2mLocalDateTime(latestFinishTime));
			acc.assignStaticObstacles("Os", j2mStaticObstacles(staticObstacles));
			acc.assignDynamicObstacles("Om", j2mDynamicObstacles(dynamicObstacles));
			
			Object[] result;
			if (isMinimumTime()) {
				m.setVariable("t_spare", j2mDuration(spareTime));
				
				result = m.returningEval("pathfinder_mt(I, F, t_start, t_end, v_max, t_spare, Os, Om)", 2);
			} else {
				result = m.returningEval("pathfinder(I, F, t_start, t_end, v_max, Os, Om)", 2);
			}
			
			double[] trajectoryData = (double[]) result[0];
			double[] evasionsDouble = (double[]) result[1];
			
			int[] evasions = Arrays.stream(evasionsDouble)
				.mapToInt((d) -> (int) d - 1).toArray();

			Trajectory trajectory = m2jTrajectory(trajectoryData);
			setTrajectory(trajectory.isEmpty() ? null : trajectory);
			storeEvasions(evasions);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void storeEvasions(int[] evasions) {
		DynamicObstacle[] obstacles = createDynamicObstaclesArray();
		
		List<DynamicObstacle> evadedObstacles = Arrays.stream(evasions)
			.mapToObj((e) -> obstacles[e]).collect(Collectors.toList());
		
		setEvadedObstacles(evadedObstacles);
	}

}
