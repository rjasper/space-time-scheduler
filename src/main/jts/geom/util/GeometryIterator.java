package jts.geom.util;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Stack;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

// TODO implement test
public class GeometryIterator implements Iterator<Geometry> {
	
	private final Stack<Iterator<Geometry>> stack = new Stack<>();
	
	public GeometryIterator(Geometry geometry) {
		Objects.requireNonNull(geometry, "geometry");
		
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
			} else {
				return geometry;
			}
		}
	}
	
	private static Iterator<Geometry> makeIterator(Geometry geometry) {
		return new HelperIterator(geometry);
	}
	
	private static class HelperIterator implements Iterator<Geometry> {
		
		private final Geometry geometry;
		
		private int i = 0;

		public HelperIterator(Geometry geometry) {
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

}
