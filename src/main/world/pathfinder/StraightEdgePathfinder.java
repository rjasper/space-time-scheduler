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

/**
 * The {@code StraightEdgePathfinder} is a {@link SpatialPathfinder} which
 * implements a minimum distance path finder. It wraps a {@link PathFinder}
 * of the StraightEdge library.
 * 
 * @author Rico
 */
public class StraightEdgePathfinder extends SpatialPathfinder {

	/**
	 * The underlying path finder implementation.
	 */
	private PathFinder pathFinder = new PathFinder();

	/**
	 * The node connector.
	 */
	private NodeConnector<PathBlockingObstacle> nodeConnector = new NodeConnector<>();

	/**
	 * The path blocking obstacles.
	 */
	private ArrayList<PathBlockingObstacle> pathBlockingObstacles = new ArrayList<>();

	/**
	 * The maximum connection distance.
	 */
	private double maxConnectionDistance = Double.POSITIVE_INFINITY;

	/**
	 * @return the path finder.
	 */
	private PathFinder getPathFinder() {
		return pathFinder;
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.SpatialPathfinder#setStaticObstacles(java.util.Collection)
	 */
	@Override
	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		super.setStaticObstacles(staticObstacles);
		
		// Convertes the static obstacles to PathBlockingObstacles and
		// configures the node connector.
		
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
		
		setNodeConnector(nc);
		setPathBlockingObstacles(pathBlockingObstacles);
	}

	/**
	 * @return the node connector.
	 */
	private NodeConnector<PathBlockingObstacle> getNodeConnector() {
		return nodeConnector;
	}

	/**
	 * Sets the node connector.
	 * 
	 * @param nodeConnector
	 */
	private void setNodeConnector(NodeConnector<PathBlockingObstacle> nodeConnector) {
		this.nodeConnector = nodeConnector;
	}

	/**
	 * @return the path blocking obstacles.
	 */
	private ArrayList<PathBlockingObstacle> getPathBlockingObstacles() {
		return pathBlockingObstacles;
	}

	/**
	 * Sets the path blocking obstacles.
	 * 
	 * @param pathBlockingObstacles
	 */
	private void setPathBlockingObstacles(
		ArrayList<PathBlockingObstacle> pathBlockingObstacles) {
		this.pathBlockingObstacles = pathBlockingObstacles;
	}

	/**
	 * @return maximum connection distance.
	 */
	private double getMaxConnectionDistance() {
		return maxConnectionDistance;
	}

	/**
	 * <p>
	 * Sets the maximum connection distance.
	 * </p>
	 * 
	 * <p>
	 * The default value is {@link Double#POSITIVE_INFINITY}.
	 * </p>
	 * 
	 * @param maxConnectionDistance
	 * @throws IllegalArgumentException
	 *             if maxConnectionDistance is not positive.
	 */
	public void setMaxConnectionDistance(double maxConnectionDistance) {
		if (Double.isNaN(maxConnectionDistance) || maxConnectionDistance <= 0.0)
			throw new IllegalArgumentException("maxConnectionDistance is not positive");
		
		this.maxConnectionDistance = maxConnectionDistance;
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.SpatialPathfinder#calculateSpatialPath()
	 */
	@Override
	protected List<Point> calculateSpatialPath() {
		PathFinder pf = getPathFinder();
		NodeConnector<PathBlockingObstacle> nodeConnector = getNodeConnector();
		KPoint startPoint = makeKPoint( getStartLocation() );
		KPoint finishPoint = makeKPoint( getFinishLocation() );
		List<PathBlockingObstacle> obstacles = getPathBlockingObstacles();
		double maxDistance = getMaxConnectionDistance();

		PathData pathData = pf.calc(startPoint, finishPoint, maxDistance, nodeConnector, obstacles);

		// TODO don't use nulls
		
		if (pathData.isError())
			return null;

		List<Point> path = makeSpatialPath(pathData);

		// if valid Path
		if (path.size() >= 2)
			return path;
		else
			return null;
	}

	/**
	 * Converts a JTS Point to a KPoint.
	 * 
	 * @param point the JTS Point
	 * @return the KPoint
	 */
	private static KPoint makeKPoint(Point point) {
		return new KPoint(point.getX(), point.getY());
	}

	/**
	 * Converts a KPoint to a JTS Point
	 * 
	 * @param point the KPoint
	 * @return the JTS Point
	 */
	private static Point makeJtsPoint(KPoint point) {
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();

		return builder.point(point.getX(), point.getY());
	}

	/**
	 * Converts the PathData to a path.
	 * 
	 * @param path
	 * @return the converted path
	 */
	private List<Point> makeSpatialPath(PathData path) {
		List<KPoint> points = path.getPoints();
		List<Point> jtsPoints = points.stream()
			.map((p) -> makeJtsPoint(p))
			.collect(Collectors.toList());

		return jtsPoints;
	}

}
