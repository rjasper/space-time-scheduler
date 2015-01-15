package jts.geom.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Enhances the {@link GeometryBuilder} by some convenience methods.
 *
 * @author Rico Jasper
 */
public class EnhancedGeometryBuilder extends org.geotools.geometry.jts.GeometryBuilder {

	/**
	 * A static instance of this class.
	 */
	private static EnhancedGeometryBuilder instance;

	/**
	 * @return a static instance of this class.
	 */
	public static EnhancedGeometryBuilder getInstance() {
		if (instance == null)
			instance = new EnhancedGeometryBuilder();

		return instance;
	}

	/**
	 * Constructs a new object using a static instance of a
	 * {@link GeometryFactory}.
	 */
	public EnhancedGeometryBuilder() {
		super(new GeometryFactory());
	}

	/**
	 * Constructs a new object using the provided factory.
	 *
	 * @param factory
	 */
	public EnhancedGeometryBuilder(GeometryFactory factory) {
		super(factory);
	}

	/**
	 * Creates a list of 2-dimensional points.
	 *
	 * @param ordinates
	 * @return the list.
	 */
	public List<Point> points(double... ordinates) {
		if (ordinates.length % 2 != 0)
			throw new IllegalArgumentException("illegal number of ordinates");

		int n = ordinates.length / 2;

		ArrayList<Point> points = new ArrayList<>(n);

		int k = 0;
		for (int i = 0; i < n; ++i) {
			double x = ordinates[k++];
			double y = ordinates[k++];

			points.add(point(x, y));
		}

		return points;
	}

	/**
	 * Creates a line string defined by the given points.
	 *
	 * @param points
	 * @return the line string.
	 */
	public LineString lineString(Collection<Point> points) {
		return lineString( ordinates(points) );
	}

	/**
	 * Creates a line string defined by the given points.
	 *
	 * @param points
	 * @return the line string.
	 */
	public LineString lineString(Point... points) {
		return lineString( ordinates(points) );
	}

	/**
	 * Creates a line string defined by the given x- and y-ordinates.
	 *
	 * @param x x-ordinates
	 * @param y y-ordinates
	 * @return the line string.
	 */
	public LineString lineString(double[] x, double[] y) {
		return lineString( ordinates(x, y) );
	}

	/**
	 * Creates a line string defined by the given x- and y-ordinates but only
	 * using n ordinates per dimension.
	 *
	 * @param x x-ordinates
	 * @param y y-ordinates
	 * @param n number of ordinates per dimension
	 * @return the line string.
	 */
	public LineString lineString(double[] x, double[] y, int n) {
		return lineString( ordinates(x, y, n) );
	}

	/**
	 * Creates a linear ring defined by the given points.
	 *
	 * @param points
	 * @return the linear ring.
	 */
	public LinearRing linearRing(Point... points) {
		return linearRing( ordinates(points) );
	}

	/**
	 * Creates a linear ring defined by the given x- and y-ordinates.
	 *
	 * @param x x-ordinates
	 * @param y y-ordinates
	 * @return the linear ring.
	 */
	public LinearRing linearRing(double[] x, double[] y) {
		return linearRing( ordinates(x, y) );
	}

	/**
	 * Creates a geometry collection defined by the given geometries.
	 *
	 * @param geometries
	 * @return the geometry collection.
	 */
	public GeometryCollection geometryCollection(Collection<? extends Geometry> geometries) {
		Geometry[] array = geometries.toArray(new Geometry[geometries.size()]);

		return geometryCollection(array);
	}

	/**
	 * Creates an ordinate array defined by a point iterator. Uses only n of the
	 * points provided by the iterator.
	 *
	 * @param iterator
	 * @param n
	 * @return the ordinate array.
	 */
	public double[] ordinates(Iterator<Point> iterator, int n) {
		int m = 2*n; // number of ordinates
		double[] ordinates = new double[m];

		int i = 0;
		while (i < m) {
			Point p = iterator.next();
			Coordinate coord = p.getCoordinate();

			ordinates[i++] = coord.x;
			ordinates[i++] = coord.y;
		}

		return ordinates;
	}

	/**
	 * Creates an ordinate array defined by the given points.
	 *
	 * @param points
	 * @return the ordinate array.
	 */
	public double[] ordinates(Point... points) {
		return ordinates(new ArrayIterator<Point>(points), points.length);
	}

	/**
	 * Creates an ordinate array defined by the given points.
	 *
	 * @param points
	 * @return the ordinate array.
	 */
	public double[] ordinates(Collection<Point> points) {
		return ordinates(points.iterator(), points.size());
	}

	/**
	 * Creates an ordinate array defined by the given x- and y-ordinates.
	 *
	 * @param x x-ordinates
	 * @param y y-ordinates
	 * @return the ordinate array.
	 */
	public double[] ordinates(double[] x, double[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("ordinate arrays have different lengths");

		int n = x.length;

		return ordinates(x, y, n);
	}

	/**
	 * Creates an ordinate array defined by the given points but only using
	 * n ordinates per dimension.
	 *
	 * @param x x-ordinates
	 * @param y y-ordinates
	 * @param n number of ordinates per dimension
	 * @return the ordinate array.
	 */
	public double[] ordinates(double[] x, double[] y, int n) {
		if (x.length < n || y.length < n)
			throw new IllegalArgumentException("ordinate arrays are to short");

		double[] ordinates = new double[2*n];

		for (int i = 0, j = 0; i < n; ++i) {
			ordinates[j++] = x[i];
			ordinates[j++] = y[i];
		}

		return ordinates;
	}

}
