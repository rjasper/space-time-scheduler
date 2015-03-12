package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import static java.util.Spliterator.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Creates a {@link CoordinateSequence} from the coordinates of several
 * geometries while retaining their order.
 * 
 * @author Rico
 */
public class GeometrySequencer {
	
	/**
	 * Creates a {@link CoordinateSequence} from all coordinates of the given
	 * geometries.
	 * 
	 * @param geometries
	 * @return the coordinate sequence.
	 */
	public static CoordinateSequence sequence(Geometry... geometries) {
		return sequence(Arrays.asList(geometries));
	}
	
	/**
	 * Creates a {@link CoordinateSequence} from all coordinates of the given
	 * geometries.
	 * 
	 * @param geometries
	 * @return the coordinate sequence.
	 */
	public static CoordinateSequence sequence(Iterable<? extends Geometry> geometries) {
		GeometrySequencer sequencer = new GeometrySequencer(geometries);
		
		return sequencer.build();
	}
	
	/**
	 * The geometries to extract the coordinates from.
	 */
	private final Iterable<? extends Geometry> geometries;
	
	/**
	 * Constructs a new {@code GeometrySequencer} which builds
	 * {@code CoordinateSequence}s using the coordinates of the given
	 * geometries.
	 * 
	 * @param geometries
	 */
	public GeometrySequencer(Iterable<? extends Geometry> geometries) {
		this.geometries = Objects.requireNonNull(geometries, "geometries");
	}
	
	/**
	 * Builds a new {@code CoordinateSequence} consisting of all coordinates
	 * of the geometries.
	 * 
	 * @return the coordinate sequence.
	 */
	public CoordinateSequence build() {
		Coordinate[] coordinates = makeStream(geometries)
			.map(g -> new GeometryIterator(g, true, true, true))
			.flatMap(GeometrySequencer::makeStream)
			.map(GEOMETRY_SPLITTER::give)
			.map(s -> new CoordinateIterable(s, true))
			.flatMap(GeometrySequencer::makeStream)
			.toArray(n -> new Coordinate[n]);
		
		return new CoordinateArraySequence(coordinates);
	}
	
	/**
	 * Makes a stream of the given {@code Iterable}.
	 * 
	 * @param iterable
	 * @return the stream.
	 */
	private static <T> Stream<T> makeStream(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	/**
	 * Makes a stream of the given {@code Iterator}.
	 * 
	 * @param iterator
	 * @return
	 */
	private static <T> Stream<T> makeStream(Iterator<T> iterator) {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(
				iterator, NONNULL | IMMUTABLE | ORDERED),
			false);
	}
	
	/**
	 * Takes the {@code CoordinateSequence}s of {@code Point}s and
	 * {@code LineString}s.
	 */
	private static final GeometrySplitter<CoordinateSequence> GEOMETRY_SPLITTER =
		new GeometrySplitter<CoordinateSequence>() {
			@Override
			protected CoordinateSequence take(Point point) {
				return point.getCoordinateSequence();
			}
	
			@Override
			protected CoordinateSequence take(LineString lineString) {
				return lineString.getCoordinateSequence();
			}
		};
	
}
