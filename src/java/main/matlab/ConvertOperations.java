package matlab;

import java.util.Collection;

import jts.geom.factories.StaticJstFactories;
import util.Factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public final class ConvertOperations {
	
	protected static GeometryFactory geom() {
		return StaticJstFactories.geomFactory();
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

	public static Object[] j2mStaticObstaclesData(Collection<Polygon> obstacles) {
		int n = obstacles.size();
		
		double[][] data = new double[n][];
		
		int i = 0;
		for (Polygon o : obstacles)
			data[i++] = j2mPolygon(o);
		
		return data;
	}
	
	public static Collection<Polygon> m2jStaticObstacles(Factory<? super Collection<Polygon>> factory, Object[] data) {
		int n = data.length;
		
		@SuppressWarnings("unchecked")
		Collection<Polygon> obstacles = (Collection<Polygon>) factory.create();
		
		for (int i = 0; i < n; ++i)
			obstacles.add(m2jPolygon((double[]) data[i]));
		
		return obstacles;
	}

}
