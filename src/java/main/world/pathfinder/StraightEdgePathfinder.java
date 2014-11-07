package world.pathfinder;

import static straightedge.geom.path.PathBlockingObstacleImpl.createObstacleFromInnerPolygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jts.geom.factories.EnhancedGeometryBuilder;
import straightedge.geom.KPoint;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacle;
import straightedge.geom.path.PathData;
import straightedge.geom.path.PathFinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class StraightEdgePathfinder extends SpatialPathfinder {
	
	private PathFinder pathFinder = new PathFinder();

	private NodeConnector<PathBlockingObstacle> nodeConnector = new NodeConnector<>();
	
	private ArrayList<PathBlockingObstacle> pathBlockingObstacles = new ArrayList<>();
	
	private double maxConnectionDistance = 0.0;
	
	public boolean isReady() {
		return super.isReady()
			&& maxConnectionDistance > 0.0;
	}

	private PathFinder getPathFinder() {
		return pathFinder;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		NodeConnector<PathBlockingObstacle> nc = new NodeConnector<>();
		PolygonConverter conv = new PolygonConverter();
		
		double maxConnectionDistance = getMaxConnectionDistance();
		
		ArrayList<PathBlockingObstacle> pathBlockingObstacles = new ArrayList<>(staticObstacles.size());
		
		for (Polygon o : staticObstacles) {
			KPolygon kp = conv.makeKPolygonFromExterior(o);
			PathBlockingObstacle pbo = createObstacleFromInnerPolygon(kp);
			
			pathBlockingObstacles.add(pbo);
			nc.addObstacle(pbo, pathBlockingObstacles, maxConnectionDistance);
		}
		
		super.setStaticObstacles(staticObstacles);
		setNodeConnector(nc);
		setPathBlockingObstacles(pathBlockingObstacles);
	}

	private NodeConnector<PathBlockingObstacle> getNodeConnector() {
		return nodeConnector;
	}

	private void setNodeConnector(NodeConnector<PathBlockingObstacle> nodeConnector) {
		this.nodeConnector = nodeConnector;
	}

	private ArrayList<PathBlockingObstacle> getPathBlockingObstacles() {
		return pathBlockingObstacles;
	}

	private void setPathBlockingObstacles(
		ArrayList<PathBlockingObstacle> pathBlockingObstacles) {
		this.pathBlockingObstacles = pathBlockingObstacles;
	}

	private double getMaxConnectionDistance() {
		return maxConnectionDistance;
	}

	public void setMaxConnectionDistance(double maxConnectionDistance) {
		this.maxConnectionDistance = maxConnectionDistance;
	}

	@Override
	protected boolean calculatePathImpl() {
		PathFinder pf = getPathFinder();
		NodeConnector<PathBlockingObstacle> nodeConnector = getNodeConnector();
		KPoint startPoint = makeKPoint( getStartPoint() );
		KPoint finishPoint = makeKPoint( getFinishPoint() );
		List<PathBlockingObstacle> obstacles = getPathBlockingObstacles();
		double maxDistance = getMaxConnectionDistance();
		
		PathData pathData = pf.calc(startPoint, finishPoint, maxDistance, nodeConnector, obstacles);
		
		if (pathData.isError())
			return false;
		
		List<Point> path = makeSpatialPath(pathData);
		boolean validPath = path.size() >= 2;
		
		setResultSpatialPath(validPath ? path : null);
		
		return validPath;
	}
	
	private KPoint makeKPoint(Point point) {
		return new KPoint(point.getX(), point.getY());
	}
	
	private Point makeJtsPoint(KPoint point) {
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		return builder.point(point.getX(), point.getY());
	}
	
	private List<Point> makeSpatialPath(PathData path) {
		List<KPoint> points = path.getPoints();
		List<Point> jtsPoints = points.stream()
			.map((p) -> makeJtsPoint(p))
			.collect(Collectors.toList());
		
		return jtsPoints;
	}

}
