package jts.geom.util;

import static java.util.Collections.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Stack;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Iterates over the contained geometries of a geometry.
 * 
 * @author Rico
 */
public class GeometryIterator implements Iterator<Geometry> {
	
	/**
	 * The stack of geometry iterators.
	 */
	private final Stack<Iterator<Geometry>> stack = new Stack<>();
	
	/**
	 * Whether to iterate over {@code GeometryCollection}s itself or only
	 * primitive geometries.
	 */
	private final boolean onlyPrimitives;

	/**
	 * Whether to iterate over the components of a {@link Polygon} rather than
	 * the {@code Polygon} itself.
	 */
	private final boolean overPolygonComponents;
	
	/**
	 * Whether to skip polygons. Useful if only polygon components are desired.
	 */
	private final boolean skipPolygons;
	
	/**
	 * The next geometry to be yielded.
	 */
	private Geometry next = null;
	
	/**
	 * Constructs a new {@code GeometryIterator} for the given {@code Geometry}.
	 * Does not iterate over the components of a {@code Polygon} but over the
	 * {@code Polygon} itself.
	 * 
	 * @param geometry
	 */
	public GeometryIterator(Geometry geometry) {
		this(geometry, false, false, false);
	}
	
	/**
	 * Constructs a new {@code GeometryIterator} for the given {@code Geometry}.
	 * 
	 * @param geometry
	 * @param onlyPrimitives
	 *            whether to iterate over {@code GeometryCollection}s itself or
	 *            only primitive geometries.
	 * @param overPolygonComponents
	 *            whether to iterate over the components of a {@link Polygon}
	 *            rather than the {@code Polygon} itself
	 * @param skipPolygons
	 *            whether to skip polygons. Useful if only polygon components
	 *            are desired.
	 */
	public GeometryIterator(Geometry geometry, boolean onlyPrimitives, boolean overPolygonComponents, boolean skipPolygons) {
		Objects.requireNonNull(geometry, "geometry");

		this.onlyPrimitives = onlyPrimitives;
		this.overPolygonComponents = overPolygonComponents;
		this.skipPolygons = skipPolygons;
		
		stack.push(singleton(geometry).iterator());
		next = prepareNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return next != null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Geometry next() {
		if (next == null)
			throw new NoSuchElementException();
		
		Geometry current = next;
		next = prepareNext();
		
		return current;
	}
	
	/**
	 * @return the next geometry to be yielded by {@link #next()}.
	 */
	private Geometry prepareNext() {
		// breaks by returning a non geometry collection
		while (true) {
			if (stack.isEmpty())
				return null;

			Iterator<Geometry> it = stack.peek();
			
			// remove finished iterators
			if (!it.hasNext()) {
				stack.pop();
				continue;
			}
			
			Geometry geometry = it.next();
	
			if (geometry instanceof GeometryCollection) {
				stack.push(new SubGeometryIterator(geometry));
				
				if (!onlyPrimitives)
					return geometry;
			} else if (geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				
				if (overPolygonComponents)
					stack.push(new PolygonComponentIterator(polygon));
				if (!skipPolygons)
					return geometry;
			} else {
				return geometry;
			}
		}
	}
	
	/**
	 * Helper {@code Iterator} class to iterate over the geometry components of
	 * a geometry. Does not recursively work on components of components.
	 * 
	 * @author Rico
	 */
	private static class SubGeometryIterator implements Iterator<Geometry> {
		
		/**
		 * The geometry to iterate over.
		 */
		private final Geometry geometry;
		
		/**
		 * The position of the current component.
		 */
		private int i = 0;

		/**
		 * Constructs a new non-recursive geometry component iterator of the
		 * given geometry.
		 * 
		 * @param geometry
		 */
		public SubGeometryIterator(Geometry geometry) {
			this.geometry = geometry;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return i < geometry.getNumGeometries();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Geometry next() {
			return geometry.getGeometryN(i++);
		}
		
	}
	
	/**
	 * Special {@code Iterator} to iterate over the components of a {@link Polygon}
	 * 
	 * @author Rico
	 */
	private static class PolygonComponentIterator implements Iterator<Geometry> {
		
		/**
		 * The polygon to iterate over.
		 */
		private final Polygon polygon;
		
		/**
		 * Indicates if the exterior ring still has to be iterated over.
		 */
		private boolean exterior = true;
		
		/**
		 * The position of the current interior component to be iterated over.
		 */
		private int interior = 0;

		/**
		 * Constructs a new {@code PolygonComponentIterator} of the given
		 * {@code Polygon}.
		 * 
		 * @param polygon
		 */
		public PolygonComponentIterator(Polygon polygon) {
			this.polygon = polygon;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return exterior || interior < polygon.getNumInteriorRing();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
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
