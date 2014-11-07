package world.pathfinder;

import java.util.List;

import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class JavaMinimumTimePathfinder extends MinimumTimePathfinder {
	
	private StraightEdgePathfinder spatialPathfinder = new StraightEdgePathfinder();
	
	private MinimumTimeVelocityPathfinder minTimePathfinder = new MinimumTimeVelocityPathfinderImpl();
	
	public JavaMinimumTimePathfinder() {
		spatialPathfinder.setMaxConnectionDistance(Double.POSITIVE_INFINITY);
	}

	@Override
	protected boolean calculatePathImpl() {
		List<Point> spatialPath = calculateSpatialPath();
		
		if (spatialPath == null)
			return false;
		
		Trajectory trajectory = calculateTrajectory(spatialPath);
		
		setResultTrajectory(trajectory);
		
		return trajectory != null;
	}
	
	private List<Point> calculateSpatialPath() {
		spatialPathfinder.setStaticObstacles(getStaticObstacles());
		spatialPathfinder.setStartPoint(getStartPoint());
		spatialPathfinder.setFinishPoint(getFinishPoint());
		
		boolean pathExists = spatialPathfinder.calculatePath();
		
		if (!pathExists)
			return null;
		
		return spatialPathfinder.getResultSpatialPath();
	}
	
	private Trajectory calculateTrajectory(List<Point> spatialPath) {
		minTimePathfinder.setDynamicObstacles(getDynamicObstacles());
		minTimePathfinder.setSpatialPath(spatialPath);
		minTimePathfinder.setMaxSpeed(getMaxSpeed());
		minTimePathfinder.setStartTime(getStartTime());
		minTimePathfinder.setEarliestFinishTime(getEarliestFinishTime());
		minTimePathfinder.setLatestFinishTime(getLatestFinishTime());
		minTimePathfinder.setBufferDuration(getBufferDuration());
		
		boolean pathExists = minTimePathfinder.calculateTrajectory();
		
		if (!pathExists)
			return null;
		
		return minTimePathfinder.getResultTrajectory();
	}

}
