package matlab;

import static java.util.stream.Collectors.toCollection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Vector;

import jts.geom.factories.StaticJstFactories;
import matlab.data.DynamicObstacleData;
import matlab.data.LineStringData;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class ConvertOperations {
	
	protected static GeometryFactory geom() {
		return StaticJstFactories.geomFactory();
	}
	
	protected static CoordinateSequenceFactory csFact() {
		return geom().getCoordinateSequenceFactory();
	}
	
	public static double[] j2mLocalDateTime(LocalDateTime time) {
		Instant instant = time.toInstant(ZoneOffset.UTC);
		
		double millis = instant.toEpochMilli();
		
		// in seconds
		return new double[] {millis * 1e-3};
	}
	
	public static LocalDateTime m2jLocalDateTime(double[] data) {
		double millis = data[0] * 1e3;
		Instant instant = Instant.ofEpochMilli((long) millis);
		
		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
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
	
	public static LineStringData j2mLineString(LineString lineString, int dim) {
//		Coordinate[] coords = lineString.getCoordinates();
		CoordinateSequence coords = lineString.getCoordinateSequence();
		int n = lineString.getNumPoints();
//		int dim = coords.getDimension();
		
		double[] data = new double[dim * n];
		
		int j = 0;
		for (int i = 0; i < n; ++i) {
			for (int d = 0; d < dim; ++d)
				data[j++] = coords.getOrdinate(i, d);
		}
		
//		return data;
		return new LineStringData(data, dim);
	}
	
//	public static LineString m2jLineString(LineStringData lsData) {
//		double[] data = lsData.getData();
//		int dim = lsData.getDimension();
//		
//		return m2jLineString(data, dim);
//	}
	
	public static LineString m2jLineString(double[] data, int dim) {
		int n = data.length / dim;
		CoordinateSequence coords = csFact().create(n, dim);
		
		int j = 0;
		for (int i = 0; i < n; ++i) {
			for (int d = 0; d < dim; ++d)
				coords.setOrdinate(i, d, data[j++]);
		}
		
		LineString lineString = geom().createLineString(coords);
		
		return lineString;
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
		Polygon polygon = obstacle.getPolygon();
		LineString path = obstacle.getPath();
		
		double[] polygonData = j2mPolygon(polygon);
		double[] pathData = j2mLineString(path, 3).getData();
		
		DynamicObstacleData data = new DynamicObstacleData(polygonData, pathData);
		
		return data;
	}
	
	public static DynamicObstacle m2jDynamicObstacle(DynamicObstacleData data) {
		double[] polygonData = data.getPolygonData();
		double[] pathData = data.getPathData();
		
		Polygon polygon = m2jPolygon(polygonData);
		LineString path = m2jLineString(pathData, 2);
		
		DynamicObstacle obstacle = new DynamicObstacle(polygon, path);
		
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
