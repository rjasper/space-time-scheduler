package de.tu_berlin.mailbox.rjasper.matlab;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.point;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.durationToSeconds;
import static java.util.stream.Collectors.toCollection;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder;
import de.tu_berlin.mailbox.rjasper.matlab.data.DynamicObstacleData;
import de.tu_berlin.mailbox.rjasper.matlab.data.LineStringData;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public final class ConvertOperations {

	private static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	protected static GeometryFactory geom() {
		return StaticGeometryBuilder.getFactoryInstance();
	}

	protected static CoordinateSequenceFactory csFact() {
		return geom().getCoordinateSequenceFactory();
	}

	public static double j2mLocalDateTime(LocalDateTime time) {
//		Instant instant = time.toInstant(ZoneOffset.UTC);
//
//		double millis = instant.toEpochMilli();

		// in seconds
		return durationToSeconds(Duration.between(BASE_TIME, time));
	}

	public static LocalDateTime m2jLocalDateTime(double data) {
		double millis = data * 1e3;
		Instant instant = Instant.ofEpochMilli((long) millis);

		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}

	public static double j2mDuration(Duration duration) {
		return duration.getSeconds();
	}

	public static Duration m2jDuration(double data) {
		return Duration.ofSeconds((long) data);
	}

	public static double[] j2mPoint(Point point, int dim) {
		CoordinateSequence coords = point.getCoordinateSequence();
//		int dim = point.getDimension();
		double[] data = new double[dim];

		for (int d = 0; d < dim; ++d)
			data[d] = coords.getOrdinate(0, d);

		return data;
	}

	public static Point m2jPoint(double[] data) {
		int dim = data.length;
		CoordinateSequence coords = csFact().create(1, dim);

		for (int d = 0; d < dim; ++d)
			coords.setOrdinate(0, d, data[d]);

		Point point = geom().createPoint(coords);

		return point;
	}

	public static double[] j2mPolygon(Polygon polygon) {
		Coordinate[] coords = polygon.norm().getCoordinates();
		int n = polygon.getNumPoints() - 1; // ignores last coordinate
		double[] arr = new double[2*n];

		// for each coordinate in reversed order skipping the last one
		for (int i = n-1, j = 0; i >= 0; --i) {
			Coordinate c = coords[i];

			arr[j++] = c.x;
			arr[j++] = c.y;
		}

		return arr;
	}

	public static Polygon m2jPolygon(double[] data) {
		int n = data.length / 2;

		Coordinate[] coords = new Coordinate[n+1];

		for (int i = n-1, j = 0; i >= 0; --i) {
			double x = data[j++];
			double y = data[j++];

			coords[i] = new Coordinate(x, y);
		}

		// first and last coordinate must be the same
		coords[n] = coords[0];

		Polygon polygon = geom().createPolygon(coords);
		polygon.normalize();

		return polygon;
	}

//	public static LineStringData j2mLineString(LineString lineString, int dim) {
////		Coordinate[] coords = lineString.getCoordinates();
//		CoordinateSequence coords = lineString.getCoordinateSequence();
//		int n = lineString.getNumPoints();
////		int dim = coords.getDimension();
//
//		double[] data = new double[dim * n];
//
//		int j = 0;
//		for (int i = 0; i < n; ++i) {
//			for (int d = 0; d < dim; ++d)
//				data[j++] = coords.getOrdinate(i, d);
//		}
//
////		return data;
//		return new LineStringData(data, dim);
//	}

	public static LineStringData j2mVertices(List<? extends Point> vertices) {
		int n = vertices.size();

		double[] data = new double[2*n];

		int j = 0;
		for (Point v : vertices) {
			data[j++] = v.getX();
			data[j++] = v.getY();
		}

		return new LineStringData(data, 2);
	}

//	public static LineString m2jLineString(LineStringData lsData) {
//		double[] data = lsData.getData();
//		int dim = lsData.getDimension();
//
//		return m2jLineString(data, dim);
//	}

//	public static LineString m2jLineString(double[] data, int dim) {
//		int n = data.length / dim;
//		CoordinateSequence coords = csFact().create(n, dim);
//
//		int j = 0;
//		for (int i = 0; i < n; ++i) {
//			for (int d = 0; d < dim; ++d)
//				coords.setOrdinate(i, d, data[j++]);
//		}
//
//		LineString lineString = geom().createLineString(coords);
//
//		return lineString;
//	}

	public static List<Point> m2jVertices(LineStringData lsData) {
		return m2jVertices(lsData.getData());
	}

	public static List<Point> m2jVertices(double[] data) {
		int n = data.length / 2;

		List<Point> vertices = new ArrayList<>(n);

		int j = 0;
		for (int i = 0; i < n; ++i) {
			double x = data[j++], y = data[j++];
			vertices.add(point(x, y));
		}

		return null;
	}

	public static double[] j2mTrajectory(Trajectory trajectory) {
//		int n = trajectory.size();
		SpatialPath path2d = trajectory.getSpatialPath();
		List<LocalDateTime> times = trajectory.getTimes();

//		double[] path2dData = j2mLineString(path2d, 2).getData();
		double[] path2dData = j2mVertices(path2d.getPoints()).getData();
		double[] timesData = times.stream()
			.filter(ldt -> ldt.isBefore(Scheduler.END_OF_TIME))
			.mapToDouble(ConvertOperations::j2mLocalDateTime)
			.toArray();

		int n = timesData.length;

		double[] data = new double[3*n];

		for (int i = 0, j = 0, k = 0; i < n; ++i) {
			data[k++] = path2dData[j++]; // x
			data[k++] = path2dData[j++]; // y
			data[k++] = timesData[i];    // t
		}

		return data;
	}

	public static Trajectory m2jTrajectory(double[] data) {
		int n = data.length / 3;

		double[] path2dData = new double[2*n];
		double[] timesData = new double[n];

		for (int i = 0, j = 0, k = 0; i < n; ++i) {
			path2dData[j++] = data[k++]; // x
			path2dData[j++] = data[k++]; // y
			timesData[i] = data[k++];    // t
		}

		List<ImmutablePoint> vertices = m2jVertices(path2dData).stream()
			.map(ImmutableGeometries::immutable)
			.collect(Collectors.toList());
		SpatialPath path2d = new SpatialPath(ImmutableList.copyOf(vertices));
		List<LocalDateTime> times = Arrays.stream(timesData)
			.mapToObj(ConvertOperations::m2jLocalDateTime)
			.collect(Collectors.toList());

		return new SimpleTrajectory(path2d, ImmutableList.copyOf(times));
	}

	public static Object[] j2mStaticObstacles(Collection<Polygon> obstacles) {
		int n = obstacles.size();

		double[][] data = new double[n][];

		int i = 0;
		for (Polygon o : obstacles)
			data[i++] = j2mPolygon(o);

		return data;
	}

	public static Collection<Polygon> m2jStaticObstacles(Object[] data) {
		int n = data.length;

		Collection<Polygon> obstacles = new Vector<>(n);

		for (int i = 0; i < n; ++i)
			obstacles.add(m2jPolygon((double[]) data[i]));

		return obstacles;
	}

	public static DynamicObstacleData j2mDynamicObstacle(DynamicObstacle obstacle) {
		Polygon polygon = obstacle.getShape();
//		LineString path = obstacle.getPath2d();
		SpatialPath path = obstacle.getSpatialPath();
		List<LocalDateTime> times = obstacle.getTimes();

		double[] polygonData = j2mPolygon(polygon);
//		double[] pathData = j2mLineString(path, 2).getData();
		double[] pathData = j2mVertices(path.getPoints()).getData();
		double[] timesData = times.stream()
			.mapToDouble(ConvertOperations::j2mLocalDateTime)
			.toArray();

		DynamicObstacleData data = new DynamicObstacleData(polygonData, pathData, timesData);

		return data;
	}

	public static DynamicObstacle m2jDynamicObstacle(DynamicObstacleData data) {
		double[] polygonData = data.getPolygonData();
		double[] pathData = data.getPathData();
		double[] timesData = data.getTimesData();

		Polygon polygon = m2jPolygon(polygonData);
		ImmutablePolygon shape = immutable(polygon);
//		LineString path = m2jLineString(pathData, 2);
		List<ImmutablePoint> vertices = m2jVertices(pathData).stream()
			.map(ImmutableGeometries::immutable)
			.collect(Collectors.toList());
		SpatialPath spatialPath = new SpatialPath(ImmutableList.copyOf(vertices));
		List<LocalDateTime> times = Arrays.stream(timesData)
			.mapToObj(ConvertOperations::m2jLocalDateTime)
			.collect(Collectors.toList());

		Trajectory trajectory = new SimpleTrajectory(spatialPath, ImmutableList.copyOf(times));

//		DynamicObstacle obstacle = new DynamicObstacle(polygon, path, times);
		DynamicObstacle obstacle = new DynamicObstacle(immutable(shape), trajectory);

		return obstacle;
	}

	public static Collection<DynamicObstacleData> j2mDynamicObstacles(Collection<DynamicObstacle> obstacles) {
		final int n = obstacles.size();

		return obstacles.stream()
			.map(ConvertOperations::j2mDynamicObstacle)
			.collect(toCollection(() -> new Vector<DynamicObstacleData>(n)));
	}

	public static Collection<DynamicObstacle> m2jDynamicObstacles(Collection<DynamicObstacleData> data) {
		final int n = data.size();

		return data.stream()
			.map(ConvertOperations::m2jDynamicObstacle)
			.collect(toCollection(() -> new Vector<>(n)));
	}

}
