package world.pathfinder;

import static java.util.stream.Collectors.toList;
import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutableOrClone;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import world.DynamicObstacle;
import world.util.SpatialPathSegmentIterable;
import world.util.SpatialPathSegmentIterable.SpatialPathSegment;
import world.util.TrajectorySegmentIterable;
import world.util.TrajectorySegmentIterable.TrajectorySegment;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class ForbiddenRegionBuilder {

	/**
	 * used to buffer masks along the spatial path
	 */
	private static final double BUFFER_FACTOR = 0.1;

	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();

	private List<Point> spatialPath = null;

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

	private List<Point> getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(List<Point> spatialPath) {
		this.spatialPath = immutable(spatialPath);
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

		List<Point> spatialPath = getSpatialPath();
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

					Geometry region;

					// if there is no unit vector (stationary segment)
					if (arcUnitVector == null) {
						region = calcStationaryCase(
							spatialPathSegment,
							obstacleTrajectorySegment,
							obstacleShape);
					} else {
						Matrix arcVelocityBase = makeArcVelocityBase(arcUnitVector, obstacleTrajectorySegment);
						Matrix transformationMatrix = calcTransformationMatrix(arcVelocityBase);

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
					}

					if (!region.isEmpty())
						subregions.add(region);
				}
			}

			Geometry region = builder.geometryCollection(subregions).union();
			region.normalize();

			if (!region.isEmpty())
				forbiddenRegions.add(new ForbiddenRegion(region, obstacle));
		}

		setResultForbiddenRegions(forbiddenRegions);
	}

	private static Geometry calcStationaryCase(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Polygon obstacleShape)
	{
		boolean first = spatialPathSegment.isFirst();
		boolean last = spatialPathSegment.isLast();

		// background:
		//
		// This case normally results in only less than 2-dimensional regions.
		// Since those are irrelevant as a forbidden region they could be
		// disregarded. However, for the first and last segment it is important
		// to include a small buffer area to the sides. Otherwise the
		// ArcTimeMesher could connect vertices through moving obstacles.
		//
		// If the obstacle's trajectory segment is stationary the result
		// would be a time parallel line at most which can be disregarded.

		// if there is no buffer necessary then return an empty polygon
		if (obstacleTrajectorySegment.isStationary() || (!first && !last))
			return EnhancedGeometryBuilder.getInstance().polygon();

		// make mask
		LineString mask = makePointTraceMask(
			spatialPathSegment,
			obstacleTrajectorySegment);

		// translate obstacle
		Polygon moved = translateGeometry(
			obstacleShape,
			obstacleTrajectorySegment.getStartPoint());

		// boundary is expected to be composed of 1-dimensional components
		Geometry boundary = moved.getBoundary();

		// calculate intersection with obstacle's interior

		// A = mask, B = movedObstacleShape
		// I* = A \cap int(B) = (A \cap B) \ (A \cap bdy(B))
		// I = I* \wedge bdy(I*)

		Geometry fullIntersection = moved.intersection(mask); // (A \cap B)
		// JTS' difference operation can't handle general GeometryCollections
		// therefore, remove points
		Geometry boundaryIntersection = onlyLines( boundary.intersection(mask) ); // (A \cap bdy(B))
		// JTS geometries always include own boundary
		Geometry masked = fullIntersection.difference(boundaryIntersection); // I

		if (masked.isEmpty())
			return masked; // empty geometry

		// transform intersection to forbidden region
		Geometry transformed = transformStationaryObstacle(
			spatialPathSegment,
			obstacleTrajectorySegment,
			masked);

		// buffer region
		Geometry buffered = bufferLineStrings(transformed, first, last);

		return buffered;
	}

	private static LineString makePointTraceMask(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment)
	{
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		// first and last point are expected to be the same
		Point spatialPathPoint = spatialPathSegment.getStartPoint();
		Vector s = makeVector(spatialPathPoint);
		// first and last point are expected to differ
		Vector vt = makeVector(
			obstacleTrajectorySegment.getStartPoint(),
			obstacleTrajectorySegment.getFinishPoint());

		return geomBuilder.lineString(
			spatialPathPoint,
			makePoint( s.subtract(vt) ));
	}

	private static Geometry transformStationaryObstacle(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Geometry maskedMovedObstacleShape)
	{
		Point xy0 = spatialPathSegment.getStartPoint();
		Point vt1 = obstacleTrajectorySegment.getStartPoint();
		Point vt2 = obstacleTrajectorySegment.getFinishPoint();
		// note that the start is vt2 and finish vt1
		Matrix pointTraceUnitRowMatrix = makeUnitVector(vt2, vt1).toRowMatrix();

		// origin
		double x0 = xy0.getX();
		double y0 = xy0.getY();
		double s0 = spatialPathSegment.getStartArc();
		double t0 = obstacleTrajectorySegment.getStartTime();

		// TODO might not be necessary to clone
		Geometry transformed = (Geometry) maskedMovedObstacleShape.clone();

		transformed.apply(new CoordinateFilter() {
			@Override
			public void filter(Coordinate c) {
				// translated xy-vector with (x0, y1) as origin
				Vector xyT = new BasicVector(new double[] {c.x - x0, c.y - y0});

				// s = s0
				c.x = s0;
				// t = t0 + vt*(xy - xy0)
				c.y = t0 + pointTraceUnitRowMatrix.multiply(xyT).get(0);
			}
		});

		return transformed;
	}

	private static Geometry bufferLineStrings(Geometry geometry, boolean left, boolean right) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		@SuppressWarnings("unchecked") // getLines returns raw List
		Collection<LineString> lines = LineStringExtracter.getLines(geometry);

		Collection<Polygon> buffered = lines.stream()
			.map(l -> bufferLineString(l, left, right))
			.collect(toList());

		return geomBuilder.geometryCollection(buffered);
	}

	private static Polygon bufferLineString(LineString lineString, boolean left, boolean right) {
		// left and right are not expected to be both unset
		if (!left && !right)
			return EnhancedGeometryBuilder.getInstance().polygon();

		int n = lineString.getNumPoints();
		int m = 2*n + 1;
		Coordinate[] lineStringCoords = lineString.getCoordinates();

		// calculate buffer

		// left side
		Coordinate[] leftCoords;
		if (left) {
			leftCoords = Arrays.stream(lineStringCoords)
				.map(c -> new Coordinate(leftBuffer(c.x), c.y))
				.toArray(Coordinate[]::new);
		} else {
			leftCoords = lineStringCoords;
		}

		// right side
		Coordinate[] rightCoords;
		if (right) {
			rightCoords = Arrays.stream(lineStringCoords)
				.map(c -> new Coordinate(rightBuffer(c.x), c.y))
				.toArray(Coordinate[]::new);
		} else {
			rightCoords = lineStringCoords;
		}

		// construct coordinate array

		Coordinate[] bufferedCoords = new Coordinate[m];
		int k = 0;

		// left
		for (int i = 0; i < n; ++i)
			bufferedCoords[k++] = leftCoords[i];
		// right
		for (int i = n-1; i >= 0; --i) // reversed
			bufferedCoords[k++] = rightCoords[i];
		// connect start and end
		bufferedCoords[m-1] = bufferedCoords[0];

		// construct polygon

		GeometryFactory geomFact = StaticJtsFactories.geomFactory();

		return geomFact.createPolygon(bufferedCoords);
	}

	private static Geometry calcParallelCase(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Vector arcUnitVector,
		Polygon obstacleShape)
	{
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		LineString spatialMask = makeSpatialLineMask(
			spatialPathSegment,
			obstacleTrajectorySegment);
		Polygon arcMask = makeArcRectangularMask(
			spatialPathSegment,
			obstacleTrajectorySegment);

		Polygon movedObstacleShape = translateGeometry(
			obstacleShape,
			obstacleTrajectorySegment.getStartPoint());
		Geometry obstaclePathIntersection = movedObstacleShape.intersection(spatialMask);

		if (obstaclePathIntersection.isEmpty())
			return geomBuilder.polygon();

		Geometry transformedObstacleShape = transformParallelObstacle(
			spatialPathSegment,
			arcUnitVector,
			obstacleTrajectorySegment,
			obstaclePathIntersection);
		Geometry region = transformedObstacleShape.intersection(arcMask);

		return region;
	}

	private static LineString makeSpatialLineMask(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment)
	{
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();

		Vector s1 = makeVector(spatialPathSegment.getStartPoint());
		Vector s2 = makeVector(spatialPathSegment.getFinishPoint());
		Vector s = s2.subtract(s1);
		Vector vt = makeVector(
			obstacleTrajectorySegment.getStartPoint(),
			obstacleTrajectorySegment.getFinishPoint());

		double snorm = spatialPathSegment.getLength();

		// calculate relative buffer size
		// the buffer will have the size of the obstacle trajectory segment

		// alpha = abs( s * vt / ||s||^2 )
		double alpha = Math.abs( s.toRowMatrix().multiply(vt).get(0) / (snorm*snorm) );
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

		if (spatialPathSegment.isFirst())
			smin = leftBuffer(smin);
		if (spatialPathSegment.isLast())
			smax = rightBuffer(smax);

		return builder.box(smin, tmin, smax, tmax);
	}

	private static Geometry transformParallelObstacle(
		SpatialPathSegment spatialPathSegment,
		Vector arcUnitVector,
		TrajectorySegment obstacleTrajectorySegment,
		Geometry obstaclePathIntersection)
	{
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();

		int n = obstaclePathIntersection.getNumGeometries();
		Matrix arcUnitRowMatrix = arcUnitVector.toRowMatrix();
		Point xy0 = spatialPathSegment.getStartPoint();
		Point vt1 = obstacleTrajectorySegment.getStartPoint();
		Point vt2 = obstacleTrajectorySegment.getFinishPoint();
		Vector vt = makeVector(vt1, vt2);

		// origin
		double x0 = xy0.getX();
		double y0 = xy0.getY();
		double s0 = spatialPathSegment.getStartArc();
		double t0 = obstacleTrajectorySegment.getStartTime();

		double duration = obstacleTrajectorySegment.getDuration();
		double displacement = arcUnitRowMatrix.multiply(vt).get(0);

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
				Vector coordVector = new BasicVector(new double[] {c.x - x0, c.y - y0});

				double s1 = s0 + arcUnitRowMatrix.multiply(coordVector).get(0);
				double s2 = s1 + displacement;
				double t1 = t0;
				double t2 = t1 + duration;

				regionCoords[k] = new Coordinate(s1, t1);
				regionCoords[2*m-k-1] = new Coordinate(s2, t2);
			}

			regionCoords[regionCoords.length-1] = regionCoords[0];

//			LinearRing shell = geomFact.createLinearRing(regionCoords);
//			subregions[i] = geomFact.createPolygon(shell);
			subregions[i] = geomFact.createPolygon(regionCoords);
		}

		return geomFact.createMultiPolygon(subregions);
	}

	private static Geometry calcRegularCase(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Matrix transformationMatrix,
		Polygon obstacleShape)
	{
		Polygon mask = makeParallelogramMask(spatialPathSegment, obstacleTrajectorySegment);
		Polygon movedObstacleShape = translateGeometry(obstacleShape, obstacleTrajectorySegment.getStartPoint());
		Geometry maskedMovedObstacleShape = movedObstacleShape.intersection(mask);
		Geometry region = transformRegularObstacle(
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
		Point s1p = spatialPathSegment.getStartPoint();
		Point s2p = spatialPathSegment.getFinishPoint();

		Vector s1v = makeVector(s1p);
		Vector s2v = makeVector(s2p);
		Vector vtv = makeVector(
			obstacleTrajectorySegment.getStartPoint(),
			obstacleTrajectorySegment.getFinishPoint());

		boolean first = spatialPathSegment.isFirst();
		boolean last = spatialPathSegment.isLast();

		// used to buffer along s direction if needed
		Vector buffer = first || last ?
			// BUFFER_FACTOR * (s2 - s1)
			s2v.subtract(s1v).multiply(BUFFER_FACTOR) : null;

		// calculate point vectors
		Vector v1 = first ? s1v.subtract(buffer) : s1v;
		Vector v2 = last  ? s2v.add     (buffer) : s2v;
		Vector v3 = v2.subtract(vtv);
		Vector v4 = v1.subtract(vtv);

		// make points
		Point p1 = makePoint(v1);
		Point p2 = makePoint(v2);
		Point p3 = makePoint(v3);
		Point p4 = makePoint(v4);

		// make polygon

		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();

		LinearRing shell = builder.linearRing(p1, p2, p3, p4, p1);

		return builder.polygon(shell);
	}

	private static Geometry transformRegularObstacle(
		SpatialPathSegment spatialPathSegment,
		TrajectorySegment obstacleTrajectorySegment,
		Geometry maskedMovedObstacleShape,
		Matrix transformationMatrix)
	{
		// TODO might not be necessary to clone
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

	private static Point makePoint(Vector vector) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		return geomBuilder.point(vector.get(0), vector.get(1));
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

		if (norm == 0.0)
			return null;

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

	private static <T extends Geometry> T translateGeometry(T geometry, Point translation) {
		T clone = mutableOrClone(geometry);

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

	private static double leftBuffer(double s) {
		return (1.0-BUFFER_FACTOR) * s;
	}

	private static double rightBuffer(double s) {
		return (1.0+BUFFER_FACTOR) * s;
	}

	private static Geometry onlyLines(Geometry geometry) {
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();

		LineMerger merger = new LineMerger();

		merger.add(geometry);
		Collection<?> lines = merger.getMergedLineStrings();

		return geomFact.buildGeometry(lines);
	}

}
