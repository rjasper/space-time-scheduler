package world;

import static matlab.ConvertOperations.j2mDynamicObstacles;
import static matlab.ConvertOperations.j2mLocalDateTime;
import static matlab.ConvertOperations.j2mPoint;
import static matlab.ConvertOperations.j2mStaticObstacles;
import static matlab.ConvertOperations.m2jLineString;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;

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
	
	private double maxSpeed = 0.;
	
	private Collection<Polygon> staticObstacles = new LinkedList<>();
	
	private Collection<DynamicObstacle> dynamicObstacles = new LinkedList<>();
	
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
			&& startingTime.compareTo(latestFinishTime) < 0
			&& maxSpeed > 0.;
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

	public double getMaxSpeed() {
		return maxSpeed;
	}

	private Collection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	private Collection<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
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
	
	private MatlabProxy getProxy() {
		return proxy;
	}

	private AccessOperations getAccess() {
		return access;
	}
	
	public LineString calculatePath() {
		if (!isReady())
			throw new IllegalStateException("invalid parameters");
		
		Point startingPoint = getStartingPoint();
		Point finishPoint = getFinishPoint();
		LocalDateTime startingTime = getStartingTime();
		LocalDateTime latestFinishTime = getLatestFinishTime();
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
			
			Object[] result = m.returningEval("pathfinder(I, F, t_start, t_end, v_max, Os, Om)", 1);
			double[] data = (double[]) result[0];
			
			return m2jLineString(data, 3);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
	}

}
