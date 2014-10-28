package world.pathfinder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import world.DynamicObstacle;
import world.LocalDateTimeFactory;
import world.Trajectory;
import world.TrajectoryFactory;
import world.util.SpatialPathSegmentIterable.SpatialPathSegmentIterator;

public class FixTimeVelocityPathfinderImpl extends FixTimeVelocityPathfinder {
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder =
		new ForbiddenRegionBuilder();
	
	private ArcTimeMeshBuilder arcTimeMeshBuilder =
		new ArcTimeMeshBuilder();

	// TODO set base time
	private LocalDateTime baseTime;

	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}

	private ArcTimeMeshBuilder getArcTimeMeshBuilder() {
		return arcTimeMeshBuilder;
	}

	private LocalDateTime getBaseTime() {
		return baseTime;
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	@Override
	protected boolean calculatePathImpl() {
		Collection<ForbiddenRegion> forbiddenRegions =
			calculateForbiddenRegions();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		List<Point> arcTimePath =
			calculateArcTimePath(mesh);
		
		Trajectory trajectory =
			calculateTrajectory(arcTimePath);
		
		setResultTrajectory(trajectory);
		
		return trajectory != null;
	}

	private Collection<ForbiddenRegion> calculateForbiddenRegions() {
		LocalDateTime baseTime = getBaseTime();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		LineString spatialPath = getSpatialPath();
		
		ForbiddenRegionBuilder builder = getForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setDynamicObstacles(dynamicObstacles);
		builder.setSpatialPath(spatialPath);
		
		builder.calculate();
		
		return builder.getResultForbiddenRegions();
	}

	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions)
	{
		double maxSpeed = getMaxSpeed();
		double maxArc = getSpatialPath().getLength();
		Point startPoint = getStartPoint();
		Point finishPoint = getFinishPoint();
		
		ArcTimeMeshBuilder builder = getArcTimeMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMaxArc(maxArc);
		builder.setStartPoint(startPoint);
		builder.setFinishPoint(finishPoint);
		
		builder.build();
		
		return builder.getResultMesh();
	}

	private List<Point> calculateArcTimePath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh)
	{
		Point startPoint = getStartPoint();
		Point finishPoint = getFinishPoint();
		
		DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstra =
			new DijkstraShortestPath<>(mesh, startPoint, finishPoint);
		
		GraphPath<Point, DefaultWeightedEdge> graphPath =
			dijkstra.getPath();
		
		if (graphPath == null) {
			return null;
		} else {
			Stream<Point> sourcePoints = graphPath.getEdgeList().stream()
				.map(mesh::getEdgeSource);
			Stream<Point> endPoint = Stream.of(graphPath.getEndVertex());
			
			return Stream.concat(sourcePoints, endPoint)
				.collect(Collectors.toList());
		}
	}

	private Trajectory calculateTrajectory(List<Point> arcTimePath) {
		LineString spatialPath = getSpatialPath();
		
		int nSpatial = spatialPath.getNumPoints();
		int nArcTime = arcTimePath.size();
		double[] xSpatial = new double[nSpatial];
		double[] ySpatial = new double[nSpatial];
		double[] sSpatial = new double[nSpatial];
		double[] sArcTime = new double[nArcTime];
		double[] tArcTime = new double[nArcTime];
		
		// copy spatial xys-coordinates into arrays
		double arcAcc = 0.0;
		SpatialPathSegmentIterator spsit = new SpatialPathSegmentIterator(spatialPath);
		for (int i = 0; i < nSpatial; ++i) {
			Coordinate c = spatialPath.getCoordinateN(i);
			xSpatial[i] = c.x;
			ySpatial[i] = c.y;
			sSpatial[i] = arcAcc;
			
			// if not the last coordinate
			if (i < nSpatial-1)
				arcAcc += spsit.next().getLength();
		}
		
		// copy arc-time st-coordinates into arrays
		Iterator<Point> it = arcTimePath.iterator();
		for (int i = 0; i < nArcTime; ++i) {
			Point p = it.next();
			
			sArcTime[i] = p.getX();
			tArcTime[i] = p.getY();
		}

		double[] tSpatial = new double[nSpatial];
		double[] xArcTime = new double[nArcTime];
		double[] yArcTime = new double[nArcTime];

		// interpolate spatial time-ordinates
		for (int i = 0, j = 0; i < nSpatial; ++i) {
			while (j < nSpatial-1 && sSpatial[i] > sArcTime[j])
				++j;
			
			
			if (sSpatial[i] == sArcTime[j]) {
				tSpatial[i] = tArcTime[j];
			} else { // sArcTime[j-1] < sSpatial[i] < sArcTime[j]
				// linear interpolation of time
				
				double s = sSpatial[i];
				double s1 = sArcTime[j-1];
				double s2 = sArcTime[j];
				double t1 = tArcTime[j-1];
				double t2 = tArcTime[j];
				
				double alpha = (s - s1)/(s2 - s1);
				tSpatial[i] = t1 + alpha*(t2 - t1);
			}
		}
		
		// interpolate arc-time spatial-coordinates
		for (int i = 0, j = 0; i < nArcTime; ++i) {
			while (j < nArcTime-1 && sArcTime[i] > sSpatial[j])
				++j;
			
			
			if (sArcTime[j] == sSpatial[i]) {
				xArcTime[j] = xSpatial[i];
				yArcTime[j] = ySpatial[i];
			} else { // sSpatial[j-1] < sArcTime[i] < sSpatial[j]
				// linear interpolation of time
				
				double s = sArcTime[i];
				double s1 = sSpatial[j-1];
				double s2 = sSpatial[j];
				double x1 = xSpatial[j-1];
				double x2 = xSpatial[j];
				double y1 = ySpatial[j-1];
				double y2 = ySpatial[j];
				
				double alpha = (s - s1)/(s2 - s1);
				xSpatial[i] = x1 + alpha*(x2 - x1);
				ySpatial[i] = y1 + alpha*(y2 - y1);
			}
		}
		
		// the regular and maximum number of trajectory vertices
		// might be less
		int n = nSpatial + nArcTime - 2;
		double[] x = new double[n];
		double[] y = new double[n];
		double[] t = new double[n];
		
		// merge and sort by arc
		for (int i = 0, j = 0, k = 0; i < nSpatial && j < nArcTime;) {
			if (sSpatial[i] == sArcTime[j]) {
				x[k] = xSpatial[i];
				y[k] = ySpatial[i];
				t[k] = tArcTime[j];
				
				++i; ++j;
			} else if (sSpatial[i] < sArcTime[j]) {
				x[k] = xSpatial[i];
				y[k] = ySpatial[i];
				t[k] = tSpatial[i];
				
				++i;
			} else { // sSpatial[i] > sArcTime[j]
				x[k] = xArcTime[j];
				y[k] = yArcTime[j];
				t[k] = tArcTime[j];
				
				++j;
			}
			
			++k;
		}
		
		// build trajectory
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		LocalDateTimeFactory timeFact = new LocalDateTimeFactory(getBaseTime());
		TrajectoryFactory trajFact = new TrajectoryFactory(geomBuilder, timeFact);
		
		return trajFact.trajectory(x, y, t);
	}

}
