package jts.geom.util;

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

public class GeometrySequencer {
	
	public static CoordinateSequence sequence(Geometry... geometries) {
		return sequence(Arrays.asList(geometries));
	}
	
	public static CoordinateSequence sequence(Iterable<? extends Geometry> geometries) {
		GeometrySequencer sequencer = new GeometrySequencer(geometries);
		
		return sequencer.build();
	}
	
	private final Iterable<? extends Geometry> geometries;
	
	public GeometrySequencer(Iterable<? extends Geometry> geometries) {
		this.geometries = Objects.requireNonNull(geometries, "geometries");
	}
	
	public CoordinateSequence build() {
		Coordinate[] coordinates = makeStream(geometries)
			.map(g -> new GeometryIterator(g, true))
			.flatMap(GeometrySequencer::makeStream)
			.map(GEOMETRY_SPLITTER::give)
			.map(s -> new CoordinateIterable(s, true))
			.flatMap(GeometrySequencer::makeStream)
			.toArray(n -> new Coordinate[n]);
		
		return new CoordinateArraySequence(coordinates);
	}
	
	private static <T> Stream<T> makeStream(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	private static <T> Stream<T> makeStream(Iterator<T> iterable) {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(
				iterable, NONNULL | IMMUTABLE | ORDERED),
			false);
	}
	
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
