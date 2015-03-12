package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometryIterator;

public class GeometryIteratorTest {

	@Test
	public void testPoint() {
		Point point = point(0, 0);
		GeometryIterator iterable = new GeometryIterator(point);
		
		assertThat(iterable.next(), equalTo(point));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testLineString() {
		LineString lineString = lineString(0, 0, 1, 1);
		GeometryIterator iterable = new GeometryIterator(lineString);
		
		assertThat(iterable.next(), equalTo(lineString));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testLinearRing() {
		LinearRing lineString = linearRing(0, 0, 1, 1, 1, 0, 0, 0);
		GeometryIterator iterable = new GeometryIterator(lineString);
		
		assertThat(iterable.next(), equalTo(lineString));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testPolygon() {
		Polygon polygon = polygon(0, 0, 1, 1, 1, 0, 0, 0);
		GeometryIterator iterable = new GeometryIterator(polygon);
		
		assertThat(iterable.next(), equalTo(polygon));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testPolygonComponents() {
		LinearRing shell = linearRing(0, 0, 10, 0, 10, 10, 0, 10, 0, 0);
		LinearRing hole = linearRing(2, 2, 8, 2, 8, 8, 2, 8, 2, 2);
		Polygon polygon = polygon(shell, hole);
		GeometryIterator iterable = new GeometryIterator(polygon, false, true, false);
		
		assertThat(iterable.next(), equalTo(polygon));
		assertThat(iterable.next(), equalTo(shell));
		assertThat(iterable.next(), equalTo(hole));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testPolygonComponentsSkipPolygon() {
		LinearRing shell = linearRing(0, 0, 10, 0, 10, 10, 0, 10, 0, 0);
		LinearRing hole = linearRing(2, 2, 8, 2, 8, 8, 2, 8, 2, 2);
		Polygon polygon = polygon(shell, hole);
		GeometryIterator iterable = new GeometryIterator(polygon, false, true, true);
		
		assertThat(iterable.next(), equalTo(shell));
		assertThat(iterable.next(), equalTo(hole));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testGeometryCollection() {
		Point point = point(0, 0);
		LineString lineString = lineString(1, 1, 2, 2);
		LinearRing linearRing = linearRing(3, 3, 8, 2, 8, 8, 3, 8, 3, 3);
		Polygon polygon = polygon(10, 10, 11, 11, 11, 10, 10, 10);
		GeometryCollection collection =
			geometryCollection(point, lineString, linearRing, polygon);
		
		GeometryIterator iterable = new GeometryIterator(collection);

		assertThat(iterable.next(), equalTo(collection));
		assertThat(iterable.next(), equalTo(point));
		assertThat(iterable.next(), equalTo(lineString));
		assertThat(iterable.next(), equalTo(linearRing));
		assertThat(iterable.next(), equalTo(polygon));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testGeometryCollectionNested() {
		Point point = point(0, 0);
		GeometryCollection inner = geometryCollection(point);
		GeometryCollection outer = geometryCollection(inner);
		
		GeometryIterator iterable = new GeometryIterator(outer);

		assertThat(iterable.next(), equalTo(outer));
		assertThat(iterable.next(), equalTo(inner));
		assertThat(iterable.next(), equalTo(point));
		assertThat(iterable.hasNext(), equalTo(false));
	}
	
	@Test
	public void testGeometryCollectionEmptyOnlyPrimitives() {
		GeometryCollection collection = geometryCollection();
		
		GeometryIterator iterable = new GeometryIterator(collection, true, false, false);
		
		assertThat(iterable.hasNext(), equalTo(false));
	}

}
