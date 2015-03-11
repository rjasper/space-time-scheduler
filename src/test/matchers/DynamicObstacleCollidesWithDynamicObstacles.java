package matchers;

import static java.util.stream.Collectors.*;
import static world.util.DynamicCollisionDetector.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import world.DynamicObstacle;

public class DynamicObstacleCollidesWithDynamicObstacles extends TypeSafeMatcher<DynamicObstacle> {
	
	@Factory
	public static Matcher<DynamicObstacle> obstacleCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return new DynamicObstacleCollidesWithDynamicObstacles(obstacles);
	}
	
	protected final Collection<? extends DynamicObstacle> obstacles;

	public DynamicObstacleCollidesWithDynamicObstacles(Collection<? extends DynamicObstacle> obstacles) {
		this.obstacles = Objects.requireNonNull(obstacles, "obstacles");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a dynamic obstacle colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected void describeMismatchSafely(DynamicObstacle item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected boolean matchesSafely(DynamicObstacle item) {
		double radius = item.calcRadius();
		
		Collection<DynamicObstacle> bufferedObstacles = obstacles.stream()
			.map(o -> o.buffer(radius))
			.collect(toList());
		
		return collides(item.getTrajectory(), bufferedObstacles);
	}

//	@Override
//	protected boolean matchesSafely(DynamicObstacle item) {
//		// checks if the arc time trace of the worker intersects with any
//		// forbidden region
//		
//		LocalDateTime baseTime = item.getStartTime();
//		Trajectory trajectory = item.getTrajectory();
//		SpatialPath spatialPath = trajectory.getSpatialPath();
//		ArcTimePath arcTimePath = trajectory.calcArcTimePath(baseTime);
//		Geometry arcTimeTrace = arcTimePath.trace();
//		
//		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
//		
//		builder.setBaseTime(baseTime);
//		builder.setSpatialPath(spatialPath);
//		builder.setDynamicObstacles(obstacles);
//		
//		builder.calculate();
//		
//		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();
//		
//		return regions.stream()
//			.map(ForbiddenRegion::getRegion)
//			.anyMatch(r -> checkCollision(arcTimeTrace, r));
//	}
//	
//	private static boolean checkCollision(Geometry arcTimeTrace, Geometry region) {
//		IntersectionMatrix mat = region.relate(arcTimeTrace);
//		
//		// true if the trace has any points with the region's interior in common
//		return isTrue( mat.get(INTERIOR, INTERIOR) )
//			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
//	}

}
