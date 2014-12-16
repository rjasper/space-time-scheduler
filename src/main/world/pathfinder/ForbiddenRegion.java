package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Geometry;

public class ForbiddenRegion { 
	
	private final Geometry region;
	
	private final DynamicObstacle dynamicObstacle;

	public ForbiddenRegion(Geometry region, DynamicObstacle dynamicObstacle) {
		if (region == null)
			throw new NullPointerException("region cannot be null");
		if (dynamicObstacle == null)
			throw new NullPointerException("dynamicObstacle cannot be null");
		
		this.region = immutable(region);
		this.dynamicObstacle = dynamicObstacle;
	}

	public Geometry getRegion() {
		return region;
	}

	public DynamicObstacle getDynamicObstacle() {
		return dynamicObstacle;
	}

	@Override
	public String toString() {
		return "ForbiddenRegion [region=" + region + ", dynamicObstacle="
			+ dynamicObstacle + "]";
	}

}
