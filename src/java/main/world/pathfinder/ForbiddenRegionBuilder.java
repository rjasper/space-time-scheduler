package world.pathfinder;

import static java.lang.Math.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.factories.StaticJtsFactories;

import org.la4j.LinearAlgebra;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic1DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import world.DynamicObstacle;
import world.Trajectory;

public class ForbiddenRegionBuilder {
	
	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private LineString spatialPath = null;
	
	private Collection<ForbiddenRegion> resultForbiddenRegions = null;
	
	private LocalDateTime baseTime = null;
	
	public boolean isReady() {
		return spatialPath != null
			&& baseTime != null;
	}

	private List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	private LineString getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(LineString spatialPath) {
		this.spatialPath = spatialPath;
	}

	public Collection<ForbiddenRegion> getResultForbiddenRegions() {
		return resultForbiddenRegions;
	}

	private void setResultForbiddenRegions(List<ForbiddenRegion> resultForbiddenRegions) {
		this.resultForbiddenRegions = new ArrayList<>(resultForbiddenRegions);
	}
	
	private LocalDateTime getBaseTime() {
		return baseTime;
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	public void calculate() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		LineString spatialPath = getSpatialPath();
		List<ForbiddenRegion> forbiddenRegions = new LinkedList<>();
		
		SpatialPathSegmentIterable spatialPathSegments =
			new SpatialPathSegmentIterable(spatialPath);
		
		for (DynamicObstacle obstacle : getDynamicObstacles()) {
			TrajectorySegmentIterable obstacleTrajectorySegments =
				new TrajectorySegmentIterable(obstacle.getTrajectory(), getBaseTime());
			
			Polygon obstacleShape = obstacle.getShape();
			
			List<Geometry> subregions = new LinkedList<>();
			
			for (TrajectorySegment obstacleTrajectorySegment : obstacleTrajectorySegments) {
				for (SpatialPathSegment spatialPathSegment : spatialPathSegments) {
					Vector arcUnitVector = makeUnitVector(
						spatialPathSegment.getStartPoint(),
						spatialPathSegment.getFinishPoint());
					Matrix arcVelocityBase = makeArcVelocityBase(arcUnitVector, obstacleTrajectorySegment);
					Matrix transformationMatrix = calcTransformationMatrix(arcVelocityBase);
					
					Geometry region;
					
					// if arcVelocityBase is not invertible
					// e.g. velocity vector is parallel to arc vector or zero
					if (transformationMatrix == null) {
						region = calcParallelCase(
							spatialPathSegment,
							obstacleTrajectorySegment,
							arcUnitVector,
							obstacleShape);
					} else {
						region = calcRegularCase(
							spatialPathSegment,
							obstacleTrajectorySegment,
							transformationMatrix,
							obstacleShape);
					}
					
					subregions.add(region);
				}
			}
			
			Geometry region = builder.geometryCollection(subregions).union();
			region.normalize();
			
			forbiddenRegions.add(new ForbiddenRegion(region, obstacle));
		}
		
		setResultForbiddenRegions(forbiddenRegions);
	}

	private static Vector makeVector(Point point) {
		return new BasicVector(new double[] {point.getX(), point.getY()});
	}
	
	private static Vector makeVector(Point startPoint, Point finishPoint) {
		double x1 = startPoint.getX();
		double y1 = startPoint.getY();
		double x2 = finishPoint.getX();
		double y2 = finishPoint.getY();
		
		return new BasicVector(new double[] {x2 - x1, y2 - y1});
	}
	
	private static Vector makeUnitVector(Point startPoint, Point finishPoint) {
		Vector vec = makeVector(startPoint, finishPoint);
		double norm = vec.fold(Vectors.mkEuclideanNormAccumulator());
		
		return vec.divide(norm);
	}
	
	private static Vector makeVelocityVector(Point startPoint, Point finishPoint, double duration) {
		Vector vec = makeVector(startPoint, finishPoint);
		
		return vec.divide(duration);
	}
	
	private static Matrix makeArcVelocityBase(
		Vector arcUnitVector,
		TrajectorySegment obstacleTrajectorySegment)
	{
		Point obstacleStartPoint = obstacleTrajectorySegment.getStartPoint();
		Point obstacleFinishPoint = obstacleTrajectorySegment.getFinishPoint();
		double obstacleDuration = obstacleTrajectorySegment.getDuration();

		Vector velocityVector = makeVelocityVector(
			obstacleStartPoint,
			obstacleFinishPoint,
			obstacleDuration);

		Matrix arcVelocityBase = new Basic1DMatrix(2, 2);
		arcVelocityBase.setColumn(0, arcUnitVector);
		arcVelocityBase.setColumn(1, velocityVector.multiply(-1.));
		
		return arcVelocityBase;
	}
	
	private static Matrix calcTransformationMatrix(Matrix arcVelocityBase) {
		Matrix transformationMatrix;
		
		try {
			transformationMatrix = arcVelocityBase.withInverter(LinearAlgebra.INVERTER).inverse();
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("This matrix is not invertible."))
				transformationMatrix = null;
			else
				throw e;
		}
		
		return transformationMatrix;
	}
	
	private static Geometry calcParallelCase(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Vector arcUnitVector,
		Polygon obstacleShape)
	{
		LineString spatialMask = makeSpatialLineMask(
			spatialPathSegment,
			obstacleTrajectorySegment);
		// TODO first and last path segment have to be masked differently
		Polygon arcMask = makeArcRectangularMask(
			spatialPathSegment,
			obstacleTrajectorySegment);
		
		Polygon movedObstacleShape = translateGeometry(
			obstacleShape,
			obstacleTrajectorySegment.getStartPoint());
		Geometry obstaclePathIntersection = movedObstacleShape.intersection(spatialMask);
		Geometry transformedObstacleShape = calcParallelObstacleShape(arcUnitVector, obstacleTrajectorySegment, obstaclePathIntersection);
		Geometry region = transformedObstacleShape.intersection(arcMask);
		
		return region;
	}
	
	private static LineString makeSpatialLineMask(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacTrajectorySegment)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		Vector s1 = makeVector(spatialPathSegment.getStartPoint());
		Vector s2 = makeVector(spatialPathSegment.getFinishPoint());
		Vector s = s2.subtract(s1);
		Vector vt = makeVector(
			obstacTrajectorySegment.getStartPoint(),
			obstacTrajectorySegment.getFinishPoint());
		
		double snorm = spatialPathSegment.getLength();
		
		// extract ordinates
//		double sx = s.get(0), sy = s.get(1), vtx = vt.get(0), vty = vt.get(1);
		
		// calculate relative buffer size
		// the buffer will have the size of the obstacle trajectory segment
//		double alpha = max(abs(sx / vtx), abs(sy / vty));
		// alpha = abs( s * vt / ||s||^2 )
		double alpha = Math.abs( s.toRowMatrix().multiply(vt).get(0) / (snorm*snorm) );
		// calculate buffer vector
		Vector salpha = s.multiply(alpha);
		
		// buffer path segment to create mask
		Vector p1 = s1.subtract(salpha);
		Vector p2 = s2.add(salpha);
		
		// extract mask ordinates
		double x1 = p1.get(0), y1 = p1.get(1), x2 = p2.get(0), y2 = p2.get(1);
		
		return builder.lineString(x1, y1, x2, y2);
	}
	
	private static Polygon makeArcRectangularMask(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstTrajectorySegment)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		double smin = spatialPathSegment.getStartArc();
		double smax = smin + spatialPathSegment.getLength();
		double tmin = obstTrajectorySegment.getStartTime();
		double tmax = tmin + obstTrajectorySegment.getDuration();
		
		return builder.box(smin, tmin, smax, tmax);
	}

	private static Geometry calcParallelObstacleShape(
		Vector arcUnitVector,
		TrajectorySegment obstacleTrajectorySegment,
		Geometry obstaclePathIntersection)
	{
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();
		
		int n = obstaclePathIntersection.getNumGeometries();
		Matrix spatialUnitRowMatrix = arcUnitVector.toRowMatrix();
		Point vt1 = obstacleTrajectorySegment.getStartPoint();
		Point vt2 = obstacleTrajectorySegment.getFinishPoint();
		
		Vector vt = makeVector(vt1, vt2);
		double displacement = spatialUnitRowMatrix.multiply(vt).get(0);

		Polygon[] subregions = new Polygon[n];
		// for each line string
		for (int i = 0; i < n; ++i) {
			Geometry g = obstaclePathIntersection.getGeometryN(i);
			
			// g might be a point
			if (!(g instanceof LineString))
				continue;
			
			Coordinate[] coords = g.getCoordinates();
			int m = coords.length;
			Coordinate[] regionCoords = new Coordinate[2*m + 1];
			
			// for each coordinate
			for (int k = 0; k < m; ++k) {
				Coordinate c = coords[k];
				Vector coordVector = new BasicVector(new double[] {c.x, c.y});

				double s1 = spatialUnitRowMatrix.multiply(coordVector).get(0);
				double s2 = s1 + displacement;
				double t1 = obstacleTrajectorySegment.getStartTime();
				double t2 = t1 + obstacleTrajectorySegment.getDuration();
				
				regionCoords[k] = new Coordinate(s1, t1);
				regionCoords[2*m-k-1] = new Coordinate(s2, t2);
			}
			
			regionCoords[regionCoords.length-1] = regionCoords[0];
			
			LinearRing shell = geomFact.createLinearRing(regionCoords);
			subregions[i] = geomFact.createPolygon(shell);
		}
		
		return geomFact.createMultiPolygon(subregions);
	}
	
	private static Geometry calcRegularCase(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Matrix transformationMatrix,
		Polygon obstacleShape)
	{
		// TODO first and last path segment have to be masked differently
		Polygon mask = makeParallelogramMask(spatialPathSegment, obstacleTrajectorySegment);
		Polygon movedObstacleShape = translateGeometry(obstacleShape, obstacleTrajectorySegment.getStartPoint());
		Geometry maskedMovedObstacleShape = movedObstacleShape.intersection(mask);
		Geometry region = calcTransformedObstacleShape(
			spatialPathSegment,
			obstacleTrajectorySegment,
			maskedMovedObstacleShape,
			transformationMatrix);
		
		return region;
	}

	private static Polygon makeParallelogramMask(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		Point s1 = spatialPathSegment.getStartPoint();
		Point s2 = spatialPathSegment.getFinishPoint();
		Point vt1 = obstacleTrajectorySegment.getStartPoint();
		Point vt2 = obstacleTrajectorySegment.getFinishPoint();
		
		double vtx = vt2.getX() - vt1.getX();
		double vty = vt2.getY() - vt1.getY();
		double x3 = s2.getX() - vtx;
		double y3 = s2.getY() - vty;
		double x4 = s1.getX() - vtx;
		double y4 = s1.getY() - vty;
		
		Point p1 = s1;
		Point p2 = s2;
		Point p3 = builder.point(x3, y3);
		Point p4 = builder.point(x4, y4);
		
		LinearRing shell = builder.linearRing(p1, p2, p3, p4, p1);
		
		return builder.polygon(shell);
	}
	
	private static <T extends Geometry> T translateGeometry(T geometry, Point translation) {
		@SuppressWarnings("unchecked")
		T clone = (T) geometry.clone();
		
		final double dx = translation.getX();
		final double dy = translation.getY();
		
		clone.apply(new CoordinateFilter() {
			@Override
			public void filter(Coordinate coord) {
				coord.x += dx;
				coord.y += dy;
			}
		});
		
		return clone;
	}

	private static Geometry calcTransformedObstacleShape(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Geometry maskedMovedObstacleShape,
		Matrix transformationMatrix)
	{
		Geometry region = (Geometry) maskedMovedObstacleShape.clone();
		Vector spatialOffset = makeVector( spatialPathSegment.getStartPoint() );
		Vector arcTimeOffset = new BasicVector(new double[] {
			spatialPathSegment.getStartArc(),
			obstacleTrajectorySegment.getStartTime()
		});
		
		region.apply(new CoordinateFilter() {
			@Override
			public void filter(Coordinate coord) {
				Vector spatialVector = new BasicVector(new double[] {coord.x, coord.y});
				
				// ST = M * (XY - XY_0) + ST_0
				Vector arcTimeVector = transformationMatrix
					.multiply( spatialVector.subtract(spatialOffset) )
					.add     ( arcTimeOffset );
				
				coord.x = arcTimeVector.get(0);
				coord.y = arcTimeVector.get(1);
			}
		});
		
		return region;
	}

	private static class SpatialPathSegment {
		
		private final Point startPoint;
		private final Point finishPoint;
		private final double startArc;
		private final double length;
		
		public SpatialPathSegment(Point startPoint, Point finishPoint, double startArc, double length) {
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
			this.startArc = startArc;
			this.length = length;
		}
		
		public Point getStartPoint() {
			return startPoint;
		}
		
		public Point getFinishPoint() {
			return finishPoint;
		}

		public double getStartArc() {
			return startArc;
		}

		public double getLength() {
			return length;
		}
		
	}
	
	private static class SpatialPathSegmentIterable implements Iterable<SpatialPathSegment> {
		
		private LineString spatialPath;
		
		public SpatialPathSegmentIterable(LineString spatialPath) {
			this.spatialPath = spatialPath;
		}

		@Override
		public Iterator<SpatialPathSegment> iterator() {
			return new SpatialPathSegmentIterator(spatialPath);
		}
		
	}
	
	private static class SpatialPathSegmentIterator implements Iterator<SpatialPathSegment> {

		private final LineString spatialPath;

		private Point lastPosition;
		private double accLength = 0.0;

		private int i = 0;

		public SpatialPathSegmentIterator(LineString spatialPath) {
			this.spatialPath = spatialPath;
			
			if (hasNext())
				init();
		}
		
		private void init() {
			lastPosition = nextPoint();
		}

		private Point nextPoint() {
			return lastPosition = spatialPath.getPointN(i++);
		}

		@Override
		public boolean hasNext() {
			return i < spatialPath.getNumPoints();
		}

		@Override
		public SpatialPathSegment next() {
			Point startPoint = lastPosition;
			Point finishPoint = nextPoint();
			
			double startArc = accLength;
			double length = DistanceOp.distance(startPoint, finishPoint);
			
			accLength += length;
			
			return new SpatialPathSegment(startPoint, finishPoint, startArc, length);
		}
		
	}
	
	private static class TrajectorySegment {
		
		private final Point startPoint;
		private final Point finishPoint;
		private final double startTime;
		private final double duration;
		
		public TrajectorySegment(Point startPoint, Point finishPoint, double startTime, double duration) {
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
			this.duration = duration;
			this.startTime = startTime;
		}

		public Point getStartPoint() {
			return startPoint;
		}

		public Point getFinishPoint() {
			return finishPoint;
		}

		public double getStartTime() {
			return startTime;
		}

		public double getDuration() {
			return duration;
		}
		
	}

	private static class TrajectorySegmentIterable implements Iterable<TrajectorySegment> {
		
		private final Trajectory trajectory;
		private final LocalDateTime baseTime;

		public TrajectorySegmentIterable(Trajectory trajectory, LocalDateTime baseTime) {
			this.trajectory = trajectory;
			this.baseTime = baseTime;
		}

		@Override
		public Iterator<TrajectorySegment> iterator() {
			return new TrajectorySegmentIterator(trajectory, baseTime);
		}
		
	}
	
	private static class TrajectorySegmentIterator implements Iterator<TrajectorySegment> {
		
		private final SpatialPathSegmentIterator spatialPathSegmentIterator;
		private final Iterator<LocalDateTime> timeIterator;
		
		private LocalDateTime lastTime;
		private double accSeconds;
		
		public TrajectorySegmentIterator(Trajectory trajectory, LocalDateTime baseTime) {
			this.spatialPathSegmentIterator =
				new SpatialPathSegmentIterator(trajectory.getPath2d());
			this.timeIterator = trajectory.getTimes().iterator();
			
			if (hasNext())
				init(baseTime);
		}
		
		private void init(LocalDateTime baseTime) {
			lastTime = timeIterator.next();
			accSeconds = durationToSeconds( Duration.between(baseTime, lastTime) );
		}
		
		private LocalDateTime nextTime() {
			return lastTime = timeIterator.next();
		}
		
		@Override
		public boolean hasNext() {
			return timeIterator.hasNext();
		}

		@Override
		public TrajectorySegment next() {
			SpatialPathSegment spatialPathSegment = spatialPathSegmentIterator.next();
			Point startPoint = spatialPathSegment.getStartPoint();
			Point finishPoint = spatialPathSegment.getFinishPoint();
			LocalDateTime startTime = lastTime;
			LocalDateTime finishTime = nextTime();
			
			double seconds = durationToSeconds( Duration.between(startTime, finishTime) );
			double startSeconds = accSeconds;
			
			accSeconds += seconds;
			
			return new TrajectorySegment(startPoint, finishPoint, startSeconds, seconds);
		}
		
		private static double durationToSeconds(Duration duration) {
			long nanos = duration.toNanos();
			double seconds =
				(double)(nanos / 1_000_000_000L) +
				(double)(nanos % 1_000_000_000L) / 1_000_000_000.;
			
			return seconds;
		}
		
	}

}
