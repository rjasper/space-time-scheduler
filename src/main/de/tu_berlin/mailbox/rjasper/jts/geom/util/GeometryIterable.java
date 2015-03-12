package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;
import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Provides a {@link GeometryIterator} and {@link Spliterator} for the
 * encapsulated {@link Geometry}.
 * 
 * @author Rico Jasper
 */
public class GeometryIterable implements Iterable<Geometry> {
	
	private final Geometry geometry;
	
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
	 * Constructs a new {@code GeometryIterable} for the given {@code Geometry}.
	 * Does not iterate over the components of a {@code Polygon} but over the
	 * {@code Polygon} itself.
	 * 
	 * @param geometry
	 */
	public GeometryIterable(Geometry geometry) {
		this(geometry, false, false, false);
	}
	
	/**
	 * Constructs a new {@code GeometryIterable} for the given {@code Geometry}.
	 * 
	 * @param geometry
	 * @param overComponents
	 *            whether to iterate over the components of a {@link Polygon}
	 *            rather than the {@code Polygon} itself
	 */
	public GeometryIterable(Geometry geometry, boolean onlyPrimitives, boolean overPolygonComponents, boolean skipPolygons) {
		this.geometry = Objects.requireNonNull(geometry, "geometry");
		
		this.onlyPrimitives = onlyPrimitives;
		this.overPolygonComponents = overPolygonComponents;
		this.skipPolygons = skipPolygons;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Geometry> iterator() {
		return new GeometryIterator(
			geometry,
			onlyPrimitives,
			overPolygonComponents,
			skipPolygons);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#spliterator()
	 */
	@Override
	public Spliterator<Geometry> spliterator() {
		return Spliterators.spliterator(
			iterator(),
			countGeometries(),
			NONNULL | ORDERED | (isImmutable(geometry) ? IMMUTABLE : 0));
	}
	
	/**
	 * @return a sequential stream over the subgeometries of the encapsulated
	 *         {@code Geometry}.
	 */
	public Stream<Geometry> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	/**
	 * Counts the number of geometries to iterate over.
	 * 
	 * @return the number of geometries.
	 */
	private int countGeometries() {
		GeometryCounter filter = new GeometryCounter();
		geometry.apply(filter);
		
		return filter.getCounter();
	}
	
	/**
	 * Helper class to count geometries within an arbitrary geometry.
	 */
	private class GeometryCounter implements GeometryFilter {
		
		/**
		 * The counter.
		 */
		private int counter = 0;

		/**
		 * @return the counter.
		 */
		public int getCounter() {
			return counter;
		}

		/*
		 * (non-Javadoc)
		 * @see com.vividsolutions.jts.geom.GeometryFilter#filter(com.vividsolutions.jts.geom.Geometry)
		 */
		@Override
		public void filter(Geometry geometry) {
			if (geometry instanceof GeometryCollection) {
				if (!onlyPrimitives)
					++counter;
			} else if (geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				
				if (overPolygonComponents)
					counter += 1 + polygon.getNumInteriorRing();
				if (!skipPolygons)
					++counter;
			} else {
				++counter;
			}
		}
	}

}
