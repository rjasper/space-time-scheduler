package jts.geom;

import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Point;

public final class LineStringFixtures {
	
	private static EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	public static List<Point> twoPoints() {
		return geomBuilder.points(
			50., 40.,
			30., 30.
		);
	}
	
	public static List<Point> threePoints() {
		return geomBuilder.points(
			 0.,  0.,
			10., 10.,
			10.,  0.
		);
	}

}
