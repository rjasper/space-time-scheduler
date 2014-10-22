package jts.geom.factories;

import static jts.geom.factories.StaticJtsFactories.*;

import java.util.Collection;

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
	
	public LineString lineString(Point ...points) {
		return lineString( ordinates(points) );
	}
	
	public LinearRing linearRing(Point ...points) {
		return linearRing( ordinates(points) );
	}
	
	public GeometryCollection geometryCollection(Collection<Geometry> geometries) {
		Geometry[] array = geometries.stream().toArray(Geometry[]::new);
		
		return geometryCollection(array);
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
