package matchers;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.BOUNDARY;
import static com.vividsolutions.jts.geom.Location.INTERIOR;
import static java.util.Collections.singleton;

import java.time.LocalDateTime;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import tasks.WorkerUnit;
import world.ArcTimePath;
import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;
import world.pathfinder.ForbiddenRegion;
import world.pathfinder.ForbiddenRegionBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class WorkerUnitCollidesWithDynamicObstacle extends TypeSafeDiagnosingMatcher<WorkerUnit> {
	
	private final DynamicObstacle obstacle;
	
	public WorkerUnitCollidesWithDynamicObstacle(DynamicObstacle obstacle) {
		this.obstacle = obstacle;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected boolean matchesSafely(WorkerUnit item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is colliding with ")
			.appendValue(obstacle);
		
		// checks if the arc time trace of the worker intersects with any
		// forbidden region
		
		LocalDateTime baseTime = item.getInitialTime();
		Trajectory trajectory = item.calcMergedTrajectory();
		SpatialPath spatialPath = trajectory.getSpatialPath();
		ArcTimePath arcTimePath = trajectory.calcArcTimePath(baseTime);
		Geometry arcTimeTrace = arcTimePath.trace();
		
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(spatialPath);
		builder.setDynamicObstacles(singleton(obstacle));
		
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
