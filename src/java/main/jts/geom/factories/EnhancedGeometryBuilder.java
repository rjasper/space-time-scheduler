package jts.geom.factories;

import static jts.geom.factories.StaticJtsFactories.*;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections4.iterators.ArrayIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

public class EnhancedGeometryBuilder extends org.geotools.geometry.jts.GeometryBuilder {
	
	private static EnhancedGeometryBuilder instance;
	
	public static EnhancedGeometryBuilder getInstance() {
		if (instance == null)
			instance = new EnhancedGeometryBuilder();
		
		return instance;
	}

	public EnhancedGeometryBuilder() {
		super(geomFactory());
	}
	
	public EnhancedGeometryBuilder(GeometryFactory factory) {
		super(factory);
	}
	
	public LineString lineString(Collection<Point> points) {
		return lineString( ordinates(points) );
	}
	
	public LineString lineString(Point ...points) {
		return lineString( ordinates(points) );
	}
	
	public LineString lineString(double[] x, double[] y) {
		return lineString( ordinates(x, y) );
	}
	
	public LineString lineString(double[] x, double[] y, int n) {
		return lineString( ordinates(x, y, n) );
	}
	
	public LinearRing linearRing(Point ...points) {
		return linearRing( ordinates(points) );
	}
	
	public LinearRing linearRing(double[] x, double[] y) {
		return linearRing( ordinates(x, y) );
	}
	
	public GeometryCollection geometryCollection(Collection<Geometry> geometries) {
		Geometry[] array = geometries.stream().toArray(Geometry[]::new);
		
		return geometryCollection(array);
	}
	
	public double[] ordinates(Iterator<Point> iterator, int n) {
		double[] ordinates = new double[2*n];
		
		int i = 0;
		while (iterator.hasNext()) {
			Point p = iterator.next();
			Coordinate coord = p.getCoordinate();
			
			ordinates[i++] = coord.x;
			ordinates[i++] = coord.y;
		}
		
		return ordinates;
	}
	
	public double[] ordinates(Point ...points) {
		return ordinates(new ArrayIterator<Point>(points), points.length);
	}
	
	public double[] ordinates(Collection<Point> points) {
		return ordinates(points.iterator(), points.size());
	}
	
	public double[] ordinates(double[] x, double[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("ordinate arrays have different lengths");
		
		int n = x.length;
		
		return ordinates(x, y, n);
	}
	
	public double[] ordinates(double[] x, double[] y, int n) {
		if (x.length < n || y.length < n)
			throw new IllegalArgumentException("ordinate arrays have different lengths");
		
		double[] ordinates = new double[2*n];
		
		for (int i = 0, j = 0; i < n; ++i) {
			ordinates[j++] = x[i];
			ordinates[j++] = y[i];
		}
		
		return ordinates;
	}
	
}
