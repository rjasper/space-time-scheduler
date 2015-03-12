package world.util;

import static com.vividsolutions.jts.geom.IntersectionMatrix.*;
import static com.vividsolutions.jts.geom.Location.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

import util.CollectionsRequire;
import world.ArcTimePath;
import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;
import world.pathfinder.ForbiddenRegion;
import world.pathfinder.ForbiddenRegionBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class DynamicCollisionDetector {
	
	public static boolean collides(Trajectory trajectory, Collection<DynamicObstacle> obstacles) {
		return new DynamicCollisionDetector(trajectory, obstacles)
			.collides();
	}
	
	private final Trajectory trajectory;
	
	private final Collection<DynamicObstacle> obstacles;

	public DynamicCollisionDetector(Trajectory trajectory, Collection<DynamicObstacle> obstacles) {
		this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
		this.obstacles = CollectionsRequire.requireNonNull(obstacles, "obstacles");
		
		if (trajectory.isEmpty())
			throw new IllegalArgumentException("empty trajectory");
	}

	public boolean collides() {
		// checks if the arc time trace of the node intersects with any
		// forbidden region
		
		LocalDateTime baseTime = trajectory.getStartTime();
		SpatialPath spatialPath = trajectory.getSpatialPath();
		ArcTimePath arcTimePath = trajectory.calcArcTimePath(baseTime);
		Geometry arcTimeTrace = arcTimePath.trace();
		
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(spatialPath);
		builder.setDynamicObstacles(obstacles);
		
		builder.calculate();
		
		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();
		
		return regions.stream()
			.map(ForbiddenRegion::getRegion)
			.anyMatch(r -> checkCollision(arcTimeTrace, r));
	}
	
	private static boolean checkCollision(Geometry arcTimeTrace, Geometry region) {
		IntersectionMatrix mat = region.relate(arcTimeTrace);
		
		// true if the trace has any points with the region's interior in common
		return isTrue( mat.get(INTERIOR, INTERIOR) )
			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
	}
}
