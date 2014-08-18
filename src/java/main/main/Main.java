package main;

import static geom.factories.StaticJstFactories.*;

import java.util.LinkedList;
import java.util.List;

import pickers.LocationPicker;
import world.WorldMap;
import geom.factories.StaticJstFactories;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Main {
	
	public static WKTReader wkt() {
		return StaticJstFactories.wktReader();
	}

	public static void main(String[] args) throws ParseException {
		WorldMap world = new WorldMap();
		Polygon o1 = (Polygon) wkt().read("POLYGON ((50 50, 100 50, 100 100, 50 100, 50 50))");
		Polygon mask = (Polygon) wkt().read("POLYGON ((0 0, 150 0, 150 150, 0 150, 0 0))");
		
		world.add(o1);
		world.ready();
		Geometry space = world.space(mask);
		
		System.out.println(space);
		
		LocationPicker picker = new LocationPicker(space, 20);
		
		List<Point> points = new LinkedList<>();
		
		while (!picker.hasNext())
			points.add(picker.next());
		
		Geometry multipoint = geomFactory().createMultiPoint(points.toArray(new Point[points.size()]));
		
		System.out.println(multipoint);
	}

}
