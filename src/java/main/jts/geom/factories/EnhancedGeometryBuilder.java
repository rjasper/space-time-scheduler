package jts.geom.factories;

import static jts.geom.factories.StaticJtsFactories.*;

import com.vividsolutions.jts.geom.Coordinate;
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
	
	public LineString lineString(Point ...points) {
		return lineString( ordinates(points) );
	}
	
	public LinearRing linearRing(Point ...points) {
		return linearRing( ordinates(points) );
	}
	
	public double[] ordinates(Point ...points) {
		int n = points.length;
		double[] ordinates = new double[2*n];
		
		int i = 0;
		for (Point p : points) {
			Coordinate coord = p.getCoordinate();
			
			ordinates[i++] = coord.x;
			ordinates[i++] = coord.y;
		}
		
		return ordinates;
	}
	
}
