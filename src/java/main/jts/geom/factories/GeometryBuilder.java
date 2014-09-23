package jts.geom.factories;

import static jts.geom.factories.StaticJtsFactories.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class GeometryBuilder extends org.geotools.geometry.jts.GeometryBuilder {
	
	private static GeometryBuilder instance;
	
	public static GeometryBuilder getInstance() {
		if (instance == null)
			instance = new GeometryBuilder();
		
		return instance;
	}

	public GeometryBuilder() {
		super(geomFactory());
	}
	
	public GeometryBuilder(GeometryFactory factory) {
		super(factory);
	}
	
	public LineString lineString(Point ...points) {
		int n = points.length;
		
		double[] ordinates = new double[2*n];
		
		int i = 0;
		for (Point p : points) {
			Coordinate coord = p.getCoordinate();
			
			ordinates[i++] = coord.x;
			ordinates[i++] = coord.y;
		}
		
		return lineString(ordinates);
	}
	
}
