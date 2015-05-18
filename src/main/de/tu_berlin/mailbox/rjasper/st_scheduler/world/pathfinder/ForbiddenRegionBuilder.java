package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.lang.Math.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.la4j.LinearAlgebra;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic1DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

/**
 * The {@code ForbiddenRegion} calculates the forbidden regions for spatial
 * paths introduced by dynamic obstacles.
 *
 * @see ForbiddenRegion
 * @author Rico Jasper
 */
public class ForbiddenRegionBuilder {

	/**
	 * The dynamic obstacles.
	 */
	private Collection<DynamicObstacle> dynamicObstacles = null;

	/**
	 * The spatial path.
	 */
	private SpatialPath spatialPath = null;

	/**
	 * The calculated forbidden regions.
	 */
	private Collection<ForbiddenRegion> resultForbiddenRegions = null;

	/**
	 * The base time.
	 */
	private LocalDateTime baseTime = null;

	/**
	 * @return the dynamic obstacles.
	 */
	private Collection<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	/**
	 * <p>
	 * Sets the dynamic obstacles.
	 * </p>
	 *
	 * <p>
	 * Any obstacle crossing the spatial path introduces a forbidden region.
	 * </p>
	 *
	 * @param dynamicObstacles
	 * @throws NullPointerException
	 *             if {@code dynamicObstacles} is {@code null} or contains
	 *             {@code null}.
	 */
	public void setDynamicObstacles(Collection<? extends DynamicObstacle> dynamicObstacles) {
		CollectionsRequire.requireNonNull(dynamicObstacles, "dynamicObstacles");

		this.dynamicObstacles = unmodifiableCollection(dynamicObstacles);
	}

	/**
	 * @return the spatial path.
	 */
	private SpatialPath getSpatialPath() {
		return spatialPath;
	}

	/**
	 * <p>
	 * Sets the spatial path.
	 * </p>
	 *
	 * <p>
	 * The spatial path might be blocked by dynamic obstacles at certain times.
	 * The times and arc position on the path are represented by forbidden
	 * regions.
	 * </p>
	 *
	 * @param spatialPath
	 * @throws NullPointerException
	 *             if {@code spatialPath} is {@code null}.
	 */
	public void setSpatialPath(SpatialPath spatialPath) {
		this.spatialPath = Objects.requireNonNull(spatialPath, "spatialPath");
	}

	/**
	 * <p>
	 * Returns the calculated forbidden regions.
	 * </p>
	 *
	 * <p>
	 * A forbidden region marks the arc positions of the spatial path which
	 * are blocked by dynamic obstacles at certain times.
	 * </p>
	 *
	 * @return the calculated forbidden regions.
	 */
	public Collection<ForbiddenRegion> getResultForbiddenRegions() {
		return resultForbiddenRegions;
	}

	/**
	 * Sets the calculated forbidden regions.
	 *
	 * @param resultForbiddenRegions
	 */
	private void setResultForbiddenRegions(List<ForbiddenRegion> resultForbiddenRegions) {
		this.resultForbiddenRegions = resultForbiddenRegions;
	}

	/**
	 * @return the base time.
	 */
	private LocalDateTime getBaseTime() {
		return baseTime;
	}

	/**
	 * <p>
	 * Sets the base time.
	 * </p>
	 *
	 * <p>
	 * The base time is used represent time in seconds as a double value.
	 * Forbidden regions use seconds instead of {@link LocalDateTime} to allow
	 * regular number arithmetic.
	 * </p>
	 *
	 * @param baseTime
	 */
	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 *
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	private void checkParameters() {
		if (dynamicObstacles == null ||
			spatialPath      == null ||
			baseTime         == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
	}

	/**
	 * <p>
	 * Calculates the forbidden regions.
	 * </p>
	 *
	 * <p>
	 * Any dynamic obstacle which crosses the spatial path will result in one
	 * forbidden region. Such a region marks the position and the time where and
	 * when the spatial path is blocked.
	 * </p>
	 */
	public void calculate() {
		checkParameters();

		SpatialPath spatialPath = getSpatialPath();
		List<ForbiddenRegion> forbiddenRegions = new LinkedList<>();

		Iterable<SpatialPath.Segment> spatialPathSegments = () ->
			spatialPath.segmentIterator();

		// for each dynamic obstacle its forbidden region
		for (DynamicObstacle obstacle : getDynamicObstacles()) {
			if (!quickObstacleEnvelopeCheck(obstacle))
				continue;

			Iterable<Trajectory.Segment> obstacleTrajectorySegments = () ->
				obstacle.getTrajectory().segmentIterator();

			Polygon obstacleShape = obstacle.getShape();

			// Stores the sub regions of a forbidden region. Each trajectory
			// segment might produce a sub region.
			List<Geometry> subregions = new LinkedList<>();
			// for each trajectory segment and spatial path segment pair
			for (Trajectory.Segment obstacleTrajectorySegment : obstacleTrajectorySegments) {
				if (!quickObstacleTrajectorySegmentCheck(obstacle, obstacleTrajectorySegment))
					continue;

				for (SpatialPath.Segment spatialPathSegment : spatialPathSegments) {
					if (!quickSpatialPathSegmentCheck(obstacle, obstacleTrajectorySegment, spatialPathSegment))
						continue;

					// The current spatial path segments unit vector.
					// Also the direction and unit length of the arc dimension.
					Vector arcUnitVector = makeUnitVector(
						spatialPathSegment.getStartPoint(),
						spatialPathSegment.getFinishPoint());

					// current subregion to be calculated
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
						if (transformationMatrix == null || isParallel(arcVelocityBase)) {
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

			Geometry region = geometry(subregions).union();

			if (!region.isEmpty())
				forbiddenRegions.add(new ForbiddenRegion(region.norm(), obstacle));
		}

		setResultForbiddenRegions(forbiddenRegions);
	}

	/**
	 * Checks if the obstacle's envelope intersects with the spatial path's
	 * envelope.
	 *
	 * @param obstacle
	 * @return {@code true} if the envelopes intersect.
	 */
	private boolean quickObstacleEnvelopeCheck(DynamicObstacle obstacle) {
		Envelope obstacleTrajectoryEnvelope = obstacle.getSpatialPath().getEnvelope();
		Envelope obstacleShapeEnvelope = obstacle.getShape().getEnvelopeInternal();
		Envelope spatialPathEnvelope = getSpatialPath().getEnvelope();

		Envelope obstacleEnvelope = convolveEnvelope(
			obstacleTrajectoryEnvelope, obstacleShapeEnvelope);

		return obstacleEnvelope.intersects(spatialPathEnvelope);
	}

	private boolean quickObstacleTrajectorySegmentCheck(
		DynamicObstacle obstacle,
		Trajectory.Segment obstacleTrajectorySegment)
	{
		Envelope obstacleSegmentEnvelope = obstacleTrajectorySegment
			.getSpatialSegment().getEnvelope();
		Envelope obstacleShapeEnvelope = obstacle.getShape().getEnvelopeInternal();
		Envelope spatialPathEnvelope = getSpatialPath().getEnvelope();

		Envelope obstacleEnvelope = convolveEnvelope(
			obstacleSegmentEnvelope, obstacleShapeEnvelope);

		return obstacleEnvelope.intersects(spatialPathEnvelope);
	}

	private boolean quickSpatialPathSegmentCheck(
		DynamicObstacle obstacle,
		Trajectory.Segment obstacleTrajectorySegment,
		SpatialPath.Segment spatialPathSegment)
	{
		Envelope obstacleSegmentEnvelope = obstacleTrajectorySegment
			.getSpatialSegment().getEnvelope();
		Envelope obstacleShapeEnvelope = obstacle.getShape().getEnvelopeInternal();
		Envelope spatialPathSegmentEnvelope = spatialPathSegment.getEnvelope();

		Envelope obstacleEnvelope = convolveEnvelope(
			obstacleSegmentEnvelope, obstacleShapeEnvelope);

		return obstacleEnvelope.intersects(spatialPathSegmentEnvelope);
	}

	private static Envelope convolveEnvelope(Envelope lhs, Envelope rhs) {
		return new Envelope(
			lhs.getMinX() + rhs.getMinX(),
			lhs.getMaxX() + rhs.getMaxX(),
			lhs.getMinY() + rhs.getMinY(),
			lhs.getMaxY() + rhs.getMaxY());
	}

	/**
	 * The threshold used by {@link #isParallel(Matrix)}.
	 */
	private static double PARALLEL_THRESHOLD = 1e-10;

	/**
	 * Determines whether both spanning vectors of the given base are considered
	 * parallel. This function calculates tan(alpha) where alpha is the angle
	 * between both vectors. {@code true} is returned if tan(alpha) is equal or
	 * below {@link #PARALLEL_THRESHOLD}.
	 *
	 * @param base
	 * @return whether the spanning vectors are parallel.
	 */
	private static boolean isParallel(Matrix base) {
		double xArc = base.get(0, 0), yArc = base.get(0, 1);
		double xVelocity = base.get(1, 0), yVelocity = base.get(1, 1);

		double numerator = xArc * yVelocity - yArc * xVelocity;

		if (numerator == 0.0)
			return true;

		double denominator = xArc * xVelocity + yArc * yVelocity;

		return abs(numerator / denominator) <= PARALLEL_THRESHOLD;
	}

	/**
	 * <p>
	 * Calculates the forbidden subregion for the case that the trajectory
	 * segment has no length.
	 * </p>
	 *
	 * <p>
	 * This case results in only less than 2-dimensional regions. Since those
	 * are irrelevant as a forbidden region they could be disregarded. However,
	 * for the first and last segment it is important to include a small buffer
	 * area to the sides. Otherwise the ArcTimeMesher could connect vertices
	 * through moving obstacles.
	 * </p>
	 *
	 * <p>
	 * If the obstacle's trajectory segment is stationary the result would be a
	 * time parallel line at most which can be disregarded.
	 * </p>
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @param obstacleShape
	 * @return the subregion.
	 */
	private Geometry calcStationaryCase(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment,
		Polygon obstacleShape)
	{
		boolean first = spatialPathSegment.isFirst();
		boolean last = spatialPathSegment.isLast();

		// if there is no buffer necessary then return an empty polygon
		if (!first && !last)
			return polygon();

		Polygon moved = translateGeometry(
			obstacleShape,
			obstacleTrajectorySegment.getStartLocation());

		Geometry transformed;
		if (obstacleTrajectorySegment.isStationary()) {
			// check if stationary location is within obstacle
			if (!spatialPathSegment.getStartPoint().within(moved))
				return polygon();

			transformed = lineString(
				spatialPathSegment.getStartVertex().getArc(),
				obstacleTrajectorySegment.getStartTimeInSeconds(getBaseTime()),
				spatialPathSegment.getFinishVertex().getArc(),
				obstacleTrajectorySegment.getFinishTimeInSeconds(getBaseTime()));
		} else {
			LineString mask = makePointTraceMask(
				spatialPathSegment,
				obstacleTrajectorySegment);

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

			transformed = transformStationaryObstacle(
				spatialPathSegment,
				obstacleTrajectorySegment,
				masked);
		}

		Geometry buffered = bufferLineStrings(transformed, first, last);

		return buffered;
	}

	/**
	 * Calculates a mask for a spatial point segment. The point is traced along
	 * the trajectory segment.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @return the mask.
	 */
	private static LineString makePointTraceMask(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment)
	{
		// first and last point are expected to be the same
		// equal to getFinishPoint()
		Point spatialPathPoint = spatialPathSegment.getStartPoint();
		Vector s = makeVector(spatialPathPoint);

		// first and last point are expected to differ
		Vector vt = makeVector(
			obstacleTrajectorySegment.getStartLocation(),
			obstacleTrajectorySegment.getFinishLocation());

		return lineString(
			spatialPathPoint,
			makePoint( s.subtract(vt) ));
	}

	/**
	 * Calculates the subregion by transforming the masked obstacle's shape in
	 * the stationary case.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @param maskedMovedObstacleShape
	 * @return the subregion.
	 */
	private Geometry transformStationaryObstacle(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment,
		Geometry maskedMovedObstacleShape)
	{
		Point xy0 = spatialPathSegment.getStartPoint();
		Point vt1 = obstacleTrajectorySegment.getStartLocation();
		Point vt2 = obstacleTrajectorySegment.getFinishLocation();
		// note that the start is vt2 and finish vt1
		Matrix pointTraceUnitRowMatrix = makeUnitVector(vt2, vt1).toRowMatrix();

		// origin
		double x0 = xy0.getX();
		double y0 = xy0.getY();
		double s0 = spatialPathSegment.getStartVertex().getArc();
		double t0 = obstacleTrajectorySegment.getStartTimeInSeconds(getBaseTime());

		Geometry transformed = (Geometry) maskedMovedObstacleShape.clone();

		transformed.apply((Coordinate c) -> {
			// translated xy-vector with (x0, y0) as origin
			Vector xyT = new BasicVector(new double[] {c.x - x0, c.y - y0});

			// s = s0
			c.x = s0;

			// FIXME formular is wrong; should be t = t0 + vt/vt^2 * (xy - xy0)
			// t = t0 + vt*(xy - xy0)
			c.y = t0 + pointTraceUnitRowMatrix.multiply(xyT).get(0);
		});

		return transformed;
	}

	/**
	 * Buffers a line string to the left and right.
	 *
	 * @param geometry
	 * @param left
	 *            if {@code true} the line is buffered to the left
	 * @param right
	 *            if {@code true} the line is buffered to the right
	 * @return the buffered line
	 */
	private static Geometry bufferLineStrings(Geometry geometry, boolean left, boolean right) {
		// left and right are not expected to be both unset

		@SuppressWarnings("unchecked") // getLines returns raw List
		Collection<LineString> lines = LineStringExtracter.getLines(geometry);

		Collection<Polygon> buffered = lines.stream()
			.map(l -> bufferLineString(l, left, right))
			.collect(toList());

		return geometry(buffered);
	}

	/**
	 * Helper method to buffer a line string.
	 *
	 * @param lineString
	 *            to buffer
	 * @param left
	 *            if {@code true} the line is buffered to the left
	 * @param right
	 *            if {@code true} the line is buffered to the right
	 * @return the buffered geometry
	 * @see #bufferLineStrings(Geometry, boolean, boolean)
	 */
	private static Polygon bufferLineString(LineString lineString, boolean left, boolean right) {
		// left and right are not expected to be both unset

		// However, if they might be in the future, this is guarded since the
		// method would break otherwise.
		if (!left && !right)
			return polygon();

		int n = lineString.getNumPoints(); // line string points
		int m = 2*n + 1;                   // buffered number of points
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
		// first and last coordinate have to be equal
		bufferedCoords[m-1] = (Coordinate) bufferedCoords[0].clone();

		// construct polygon

		return polygon(bufferedCoords);
	}

	/**
	 * Calculates the forbidden subregion for the case that the trajectory
	 * segment and the spatial path segment are parallel to each other.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @param arcUnitVector
	 * @param obstacleShape
	 * @return the subregion
	 */
	private Geometry calcParallelCase(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment,
		Vector arcUnitVector,
		Polygon obstacleShape)
	{
		LineString spatialMask = makeSpatialLineMask(
			spatialPathSegment,
			obstacleTrajectorySegment);
		Polygon arcMask = makeArcRectangularMask(
			spatialPathSegment,
			obstacleTrajectorySegment);

		Polygon movedObstacleShape = translateGeometry(
			obstacleShape,
			obstacleTrajectorySegment.getStartLocation());
		Geometry obstaclePathIntersection = movedObstacleShape.intersection(spatialMask);

		if (obstaclePathIntersection.isEmpty())
			return polygon(); // empty

		Geometry transformedObstacleShape = transformParallelObstacle(
			spatialPathSegment,
			arcUnitVector,
			obstacleTrajectorySegment,
			obstaclePathIntersection);

		// note that the arc mask already includes a buffer along the arc
		Geometry region = transformedObstacleShape.intersection(arcMask);

		return region;
	}

	/**
	 * Calculates the mask in case of a spatial segment parallel to the
	 * trajectory segment. The mask reassembles the spatial segment but is also
	 * extended by the length of the trajectory segment.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @return the mask.
	 */
	private static LineString makeSpatialLineMask(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment)
	{
		Vector s1 = makeVector(spatialPathSegment.getStartPoint());
		Vector s2 = makeVector(spatialPathSegment.getFinishPoint());
		Vector s = s2.subtract(s1);
		Vector vt = makeVector(
			obstacleTrajectorySegment.getStartLocation(),
			obstacleTrajectorySegment.getFinishLocation());

		double snorm = spatialPathSegment.length();

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

		return lineString(x1, y1, x2, y2);
	}

	/**
	 * <p>
	 * Calculates the mask scoping the relevant area in the arc-time plane of
	 * the transformed obstacle shape in the parallel case.
	 * </p>
	 *
	 * <p>
	 * Also includes a buffer if the segment is the first or last one.
	 * </p>
	 *
	 * @param spatialPathSegment
	 * @param obstTrajectorySegment
	 * @return the mask.
	 */
	private Polygon makeArcRectangularMask(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstTrajectorySegment)
	{
		// boundaries
		double smin = spatialPathSegment.getStartVertex().getArc();
		double smax = smin + spatialPathSegment.length();
		double tmin = obstTrajectorySegment.getStartTimeInSeconds(getBaseTime());
		double tmax = tmin + obstTrajectorySegment.durationInSeconds();

		// also include a buffer if the segment is the first or last one.
		if (spatialPathSegment.isFirst())
			smin = leftBuffer(smin);
		if (spatialPathSegment.isLast())
			smax = rightBuffer(smax);

		return box(smin, tmin, smax, tmax);
	}

	/**
	 * Calculates the subregion by transforming the masked obstacle's shape in
	 * the parallel case.
	 *
	 * @param spatialPathSegment
	 * @param arcUnitVector
	 * @param obstacleTrajectorySegment
	 * @param obstaclePathIntersection
	 * @return
	 */
	private Geometry transformParallelObstacle(
		SpatialPath.Segment spatialPathSegment,
		Vector arcUnitVector,
		Trajectory.Segment obstacleTrajectorySegment,
		Geometry obstaclePathIntersection)
	{
		int n = obstaclePathIntersection.getNumGeometries();
		Matrix arcUnitRowMatrix = arcUnitVector.toRowMatrix();
		Point xy0 = spatialPathSegment.getStartPoint();
		Point vt1 = obstacleTrajectorySegment.getStartLocation();
		Point vt2 = obstacleTrajectorySegment.getFinishLocation();
		Vector vt = makeVector(vt1, vt2);

		// origin
		double x0 = xy0.getX();
		double y0 = xy0.getY();
		double s0 = spatialPathSegment.getStartVertex().getArc();
		double t0 = obstacleTrajectorySegment.getStartTimeInSeconds(getBaseTime());

		double duration = obstacleTrajectorySegment.durationInSeconds();
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

			// first and last coordinate have to be equal
			regionCoords[regionCoords.length-1] = (Coordinate) regionCoords[0].clone();

			subregions[i] = polygon(regionCoords);
		}

		return multiPolygon(subregions);
	}

	/**
	 * Calculates the forbidden subregion for the regular case were both the
	 * spatial path segment and the trajectory segment have a positive length
	 * and are not parallel to each other.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @param transformationMatrix
	 * @param obstacleShape
	 * @return the subregion.
	 */
	private Geometry calcRegularCase(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment,
		Matrix transformationMatrix,
		Polygon obstacleShape)
	{
		Polygon mask = makeParallelogramMask(spatialPathSegment, obstacleTrajectorySegment);
		Polygon movedObstacleShape =
			translateGeometry(obstacleShape, obstacleTrajectorySegment.getStartLocation());
		Geometry maskedMovedObstacleShape = movedObstacleShape.intersection(mask);
		Geometry region = transformRegularObstacle(
			spatialPathSegment,
			obstacleTrajectorySegment,
			maskedMovedObstacleShape,
			transformationMatrix);

		return region;
	}

	/**
	 * <p>
	 * Calculates the mask for the obstacle shape in the regular case. The
	 * result is a parallelogram spanned by the spatial path segment and
	 * trajectory segment.
	 * </p>
	 *
	 * <p>
	 * Also includes a buffer if the segment is the first or last one.
	 * </p>
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @return
	 */
	private static Polygon makeParallelogramMask(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment)
	{
		Point s1p = spatialPathSegment.getStartPoint();
		Point s2p = spatialPathSegment.getFinishPoint();

		Vector s1v = makeVector(s1p);
		Vector s2v = makeVector(s2p);
		Vector vtv = makeVector(
			obstacleTrajectorySegment.getStartLocation(),
			obstacleTrajectorySegment.getFinishLocation());

		boolean first = spatialPathSegment.isFirst();
		boolean last = spatialPathSegment.isLast();

		// calculate point vectors
		Vector v1 = first ? leftBuffer (s1v) : s1v;
		Vector v2 = last  ? rightBuffer(s2v) : s2v;
		Vector v3 = v2.subtract(vtv);
		Vector v4 = v1.subtract(vtv);

		// make points
		Point p1 = makePoint(v1);
		Point p2 = makePoint(v2);
		Point p3 = makePoint(v3);
		Point p4 = makePoint(v4);

		// make polygon
		LinearRing shell = linearRing(p1, p2, p3, p4, p1);

		return polygon(shell);
	}

	/**
	 * Calculates the subregion by transforming the masked obstacle's shape in
	 * the regular case.
	 *
	 * @param spatialPathSegment
	 * @param obstacleTrajectorySegment
	 * @param maskedMovedObstacleShape
	 * @param transformationMatrix
	 * @return the subregion
	 */
	private Geometry transformRegularObstacle(
		SpatialPath.Segment spatialPathSegment,
		Trajectory.Segment obstacleTrajectorySegment,
		Geometry maskedMovedObstacleShape,
		Matrix transformationMatrix)
	{
		Geometry region = (Geometry) maskedMovedObstacleShape.clone();
		Vector spatialOffset = makeVector( spatialPathSegment.getStartPoint() );
		Vector arcTimeOffset = new BasicVector(new double[] {
			spatialPathSegment.getStartVertex().getArc(),
			obstacleTrajectorySegment.getStartTimeInSeconds(getBaseTime())
		});

		region.apply((Coordinate c) -> {
			Vector spatialVector = new BasicVector(new double[] {c.x, c.y});

			// ST = M * (XY - XY_0) + ST_0
			Vector arcTimeVector = transformationMatrix
				.multiply( spatialVector.subtract(spatialOffset) )
				.add     ( arcTimeOffset );
			c.x = arcTimeVector.get(0);
			c.y = arcTimeVector.get(1);
		});

		return region;
	}

	/**
	 * Converts a vector to a point.
	 *
	 * @param vector
	 * @return the point.
	 */
	private static Point makePoint(Vector vector) {
		return point(vector.get(0), vector.get(1));
	}

	/**
	 * Converts a point to a vector.
	 *
	 * @param point
	 * @return the vector.
	 */
	private static Vector makeVector(Point point) {
		return new BasicVector(new double[] {point.getX(), point.getY()});
	}

	/**
	 * Converts two point to a vector connecting both points.
	 *
	 * @param startPoint
	 * @param finishPoint
	 * @return the vector.
	 */
	private static Vector makeVector(Point startPoint, Point finishPoint) {
		double x1 = startPoint.getX();
		double y1 = startPoint.getY();
		double x2 = finishPoint.getX();
		double y2 = finishPoint.getY();

		return new BasicVector(new double[] {x2 - x1, y2 - y1});
	}

	/**
	 * Calculates the unit vector between to points.
	 *
	 * @param startPoint
	 * @param finishPoint
	 * @return the unit vector.
	 */
	private static Vector makeUnitVector(Point startPoint, Point finishPoint) {
		Vector vec = makeVector(startPoint, finishPoint);
		double norm = vec.fold(Vectors.mkEuclideanNormAccumulator());

		if (norm == 0.0)
			return null;

		return vec.divide(norm);
	}

	/**
	 * Calculates the velocity vector {@code s/t} where {@code s} is the
	 * distance vector between two points and {@code t} is the duration.
	 *
	 * @param startPoint
	 * @param finishPoint
	 * @param duration
	 * @return the velocity vector.
	 */
	private static Vector makeVelocityVector(Point startPoint, Point finishPoint, double duration) {
		Vector vec = makeVector(startPoint, finishPoint);

		return vec.divide(duration);
	}

	/**
	 * Calculates the arc-velocity base.
	 *
	 * @param arcUnitVector
	 * @param obstacleTrajectorySegment
	 * @return
	 */
	private static Matrix makeArcVelocityBase(
		Vector arcUnitVector,
		Trajectory.Segment obstacleTrajectorySegment)
	{
		Point obstacleStartPoint = obstacleTrajectorySegment.getStartLocation();
		Point obstacleFinishPoint = obstacleTrajectorySegment.getFinishLocation();
		double obstacleDuration = obstacleTrajectorySegment.durationInSeconds();

		Vector velocityVector = makeVelocityVector(
			obstacleStartPoint,
			obstacleFinishPoint,
			obstacleDuration);

		Matrix arcVelocityBase = new Basic1DMatrix(2, 2);
		arcVelocityBase.setColumn(0, arcUnitVector);
		arcVelocityBase.setColumn(1, velocityVector.multiply(-1.));

		return arcVelocityBase;
	}

	/**
	 * Calculates the inverted arc-velocity base.
	 *
	 * @param arcVelocityBase
	 * @return the inverted matrix. {@code null} if no inversion exists.
	 */
	private static Matrix calcTransformationMatrix(Matrix arcVelocityBase) {
		Matrix transformationMatrix;

		try {
			transformationMatrix =
				arcVelocityBase.withInverter(LinearAlgebra.INVERTER).inverse();
		} catch (IllegalArgumentException e) {
			// what an appropriate exception to catch :P

			if (e.getMessage().equals("This matrix is not invertible."))
				transformationMatrix = null;
			else
				throw e;
		}

		return transformationMatrix;
	}

	/**
	 * Translates a geometry by an offset given by a point.
	 *
	 * @param geometry
	 * @param translation
	 * @return the translated geometry.
	 */
	private static <T extends Geometry> T translateGeometry(T geometry, Point translation) {
		T clone = mutableOrClone(geometry);

		double dx = translation.getX();
		double dy = translation.getY();

		clone.apply((Coordinate c) -> {
			c.x += dx;
			c.y += dy;
		});

		return clone;
	}

	/**
	 * Calculates the left buffered value of the given arc.
	 *
	 * @param s the arc value
	 * @return the buffered left side.
	 */
	private static double leftBuffer(double s) {
		return s - ulp(s);
	}

	/**
	 * Calculates the left buffered vector of the given one.
	 *
	 * @param v the vector
	 * @return the buffered left vector.
	 */
	private static Vector leftBuffer(Vector v) {
		double s = v.get(0), t = v.get(1);

		return new BasicVector(new double[] {leftBuffer(s), t});
	}

	/**
	 * Calculates the right buffered value of the given arc.
	 *
	 * @param s the arc value
	 * @return the buffered right side.
	 */
	private static double rightBuffer(double s) {
		return s + ulp(s);
	}

	/**
	 * Calculates the right buffered vector of the given one.
	 *
	 * @param v the vector
	 * @return the buffered right vector.
	 */
	private static Vector rightBuffer(Vector v) {
		double s = v.get(0), t = v.get(1);

		return new BasicVector(new double[] {rightBuffer(s), t});
	}

	/**
	 * Extracts all line strings from a geometry.
	 *
	 * @param geometry
	 * @return the line strings as a single geometry.
	 * @see LineMerger
	 */
	private static Geometry onlyLines(Geometry geometry) {
		LineMerger merger = new LineMerger();

		merger.add(geometry);
		@SuppressWarnings("unchecked") // returns raw type
		Collection<Geometry> lines = merger.getMergedLineStrings();

		return geometry(lines);
	}

}
