package jts.geom;

import jts.geom.factories.StaticJstFactories;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public final class PolygonFixtures {
	
	protected static GeometryFactory geom() {
		return StaticJstFactories.geomFactory();
	}
	
	public static Polygon triangle() {
		return geom().createPolygon(new Coordinate[] {
			new Coordinate( 0,  0),
			new Coordinate(10, 20),
			new Coordinate(20,  0),
			new Coordinate( 0,  0),
		});
	}
	
	public static Polygon square() {
		return geom().createPolygon(new Coordinate[] {
			new Coordinate(30, 30),
			new Coordinate(50, 30),
			new Coordinate(50, 50),
			new Coordinate(30, 50),
			new Coordinate(30, 30),
		});
	}

}
