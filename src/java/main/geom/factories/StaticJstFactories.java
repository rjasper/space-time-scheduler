package geom.factories;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

public final class StaticJstFactories {
	
	private static final GeometryFactory FLOAT_GEOMETRY_FACTORY = new GeometryFactory();
	
	private static GeometryFactory defaultGeometryFactory = floatGeometryFactory();
	
	public static GeometryFactory geomFactory() {
		return defaultGeometryFactory;
	}
	
	public static GeometryFactory floatGeometryFactory() {
		return FLOAT_GEOMETRY_FACTORY;
	}
	
	private static final WKTReader FLOAT_WKT_READER = new WKTReader(floatGeometryFactory());
	
	private static WKTReader defaultWktReader = floatWktReader();
	
	public static WKTReader floatWktReader() {
		return FLOAT_WKT_READER;
	}
	
	public static WKTReader wktReader() {
		return defaultWktReader;
	}

}
