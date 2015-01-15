package jts.geom.util;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Stack;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

// TODO implement test
public class GeometryIterator implements Iterator<Geometry> {
	
	private final Stack<Iterator<Geometry>> stack = new Stack<>();
	
	private final boolean overComponents;
	
	public GeometryIterator(Geometry geometry) {
		this(geometry, false);
	}
	
	public GeometryIterator(Geometry geometry, boolean overComponents) {
		Objects.requireNonNull(geometry, "geometry");
		
		this.overComponents = overComponents;
		
		stack.push(makeIterator(geometry));
	}

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public Geometry next() {
		// breaks by returning a non geometry collection
		while (true) {
			Iterator<Geometry> it;
			
			try {
				it = stack.peek();
			} catch (EmptyStackException e) {
				throw new NoSuchElementException();
			}
			
			Geometry geometry = it.next();
			
			// remove finished iterators
			if (!it.hasNext())
				stack.pop();
	
			// if collection then go in "recursion" until non-collection was
			// found
			if (geometry instanceof GeometryCollection) {
				stack.push(makeIterator(geometry));
			} else if (overComponents && geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				
				stack.push(new PolygonComponentIterator(polygon));
			} else {
				return geometry;
			}
		}
	}
	
	private static Iterator<Geometry> makeIterator(Geometry geometry) {
		return new SubGeometryIterator(geometry);
	}
	
	private static class SubGeometryIterator implements Iterator<Geometry> {
		private final Geometry geometry;
		
		private int i = 0;

		public SubGeometryIterator(Geometry geometry) {
			this.geometry = geometry;
		}

		@Override
		public boolean hasNext() {
			return i < geometry.getNumGeometries();
		}

		@Override
		public Geometry next() {
			return geometry.getGeometryN(i++);
		}
	}
	
	private static class PolygonComponentIterator implements Iterator<Geometry> {
		
		private final Polygon polygon;
		
		/**
		 * Indicates if the exterior ring still has to be iterated over.
		 */
		private boolean exterior = false;
		
		private int interior = 0;

		public PolygonComponentIterator(Polygon polygon) {
			this.polygon = polygon;
			this.exterior = !polygon.isEmpty();
		}

		@Override
		public boolean hasNext() {
			return exterior || interior < polygon.getNumInteriorRing();
		}

		@Override
		public Geometry next() {
			if (exterior) {
				exterior = false;
				return polygon.getExteriorRing();
			} else {
				try {
					return polygon.getInteriorRingN(interior++);
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}
		}
		
	}

}
