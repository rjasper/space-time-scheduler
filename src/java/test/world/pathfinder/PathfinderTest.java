package world.pathfinder;

import jts.geom.factories.StaticJtsFactories;
import matlab.MatlabAccess;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.io.WKTReader;

public class PathfinderTest {
	
	private static MatlabProxy proxy;
	
	private MatlabProxy getProxy() {
		return proxy;
	}
	
	private static WKTReader wkt() {
		return StaticJtsFactories.wktReader();
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		proxy = MatlabAccess.getProxy();
	}
	
	@After
	public void tearDown() throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("clear", "all");
//		m.disconnect();
	}

//	@Test
//	public void test() throws ParseException {
//		Point startingPoint = (Point) wkt().read("POINT (0 0)");
//		Point finishPoint = (Point) wkt().read("POINT (8 6)");
//		LocalDateTime startingTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0, 0);
//		LocalDateTime finishTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0, 12);
//		Duration duration = Duration.ofSeconds(5);
//		double maxSpeed = 1.4;
//		
//		Polygon rectangle = (Polygon) wkt().read("POLYGON ((1 1, 4 1, 4 3, 1 3, 1 1))");
//		Polygon square = (Polygon) wkt().read("POLYGON ((5 4, 7 4, 7 6, 5 6, 5 4))");
//		Polygon rhombus = (Polygon) wkt().read("POLYGON ((0 -1, 1 0, 0 1, -1 0, 0 -1))");
//		LineString path = (LineString) wkt().read("LINESTRING (3 5, 8 0)");
//		List<LocalDateTime> times = Arrays.asList(
//			LocalDateTime.of(2000, JANUARY, 1, 0, 0,  0),
//			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 12)
//		);
//		DynamicObstacle dynamicObstacle = new DynamicObstacle(rhombus, path, times);
//		
//		MatlabProxy m = getProxy();
//		
//		MatlabPathfinder pf = new MatlabPathfinder(m);
//		
////		pf.useSpecifiedFinishTime();
//		pf.useMinimumFinishTime();
//		
//		pf.setStartingPoint(startingPoint);
//		pf.setFinishPoint(finishPoint);
//		pf.setStartingTime(startingTime);
//		pf.setEarliestFinishTime(finishTime);
//		pf.setLatestFinishTime(finishTime);
//		pf.setSpareTime(duration);
//		pf.setMaxSpeed(maxSpeed);
//		pf.addStaticObstacle(rectangle);
//		pf.addStaticObstacle(square);
//		pf.addDynamicObstacle(dynamicObstacle);
//		
//		pf.calculatePath();
//		
//		Trajectory trajectory = pf.getResultTrajectory();
//		List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
//		
//		// TODO implement proper assertions
//		
//		System.out.println(trajectory);
//		System.out.println(evadedObstacles);
//	}

}
