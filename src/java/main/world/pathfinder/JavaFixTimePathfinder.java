package world.pathfinder;

import java.util.List;

import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class JavaFixTimePathfinder extends FixTimePathfinder {
	
	private SpatialPathfinder spatialPathfinder = new StraightEdgePathfinder();
	
	private FixTimeVelocityPathfinder fixTimePathfinder = new FixTimeVelocityPathfinderImpl();

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
		fixTimePathfinder.setDynamicObstacles(getDynamicObstacles());
		fixTimePathfinder.setSpatialPath(spatialPath);
		fixTimePathfinder.setMaxSpeed(getMaxSpeed());
		fixTimePathfinder.setStartTime(getStartTime());
		fixTimePathfinder.setFinishTime(getFinishTime());
		
		boolean pathExists = fixTimePathfinder.calculateTrajectory();
		
		if (!pathExists)
			return null;
		
		return fixTimePathfinder.getResultTrajectory();
	}

}
