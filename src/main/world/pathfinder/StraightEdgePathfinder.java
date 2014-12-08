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

	private double maxConnectionDistance = Double.POSITIVE_INFINITY;

	private PathFinder getPathFinder() {
		return pathFinder;
	}

	@Override
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
	protected List<Point> calculateSpatialPath() {
		PathFinder pf = getPathFinder();
		NodeConnector<PathBlockingObstacle> nodeConnector = getNodeConnector();
		KPoint startPoint = makeKPoint( getStartLocation() );
		KPoint finishPoint = makeKPoint( getFinishLocation() );
		List<PathBlockingObstacle> obstacles = getPathBlockingObstacles();
		double maxDistance = getMaxConnectionDistance();

		PathData pathData = pf.calc(startPoint, finishPoint, maxDistance, nodeConnector, obstacles);

		if (pathData.isError())
			return null;

		List<Point> path = makeSpatialPath(pathData);

		// if valid Path
		if (path.size() >= 2)
			return path;
		else
			return null;
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
