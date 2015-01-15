package jts.geom.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
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
			.flatMap(NON_GEOMETRY_COLLECTION_SPLITTER::give)
			.map(s -> new CoordinateIterable(s, true))
			.flatMap(GeometrySequencer::makeStream)
			.toArray(n -> new Coordinate[n]);
		
		return new CoordinateArraySequence(coordinates);
	}
	
	private static <T> Stream<T> makeStream(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	private static final GeometrySplitter<Stream<CoordinateSequence>> NON_GEOMETRY_COLLECTION_SPLITTER =
		new GeometrySplitter<Stream<CoordinateSequence>>() {
			@Override
			protected Stream<CoordinateSequence> take(Point point) {
				return Stream.of(point.getCoordinateSequence());
			}
	
			@Override
			protected Stream<CoordinateSequence> take(LineString lineString) {
				return Stream.of(lineString.getCoordinateSequence());
			}
	
			@Override
			protected Stream<CoordinateSequence> take(Polygon polygon) {
				Stream.Builder<CoordinateSequence> builder = Stream.builder();
				
				builder.add(polygon.getExteriorRing().getCoordinateSequence());
				
				int n = polygon.getNumInteriorRing();
				for (int i = 0; i < n; ++i)
					builder.add(polygon.getInteriorRingN(i).getCoordinateSequence());
				
				return builder.build();
			}
		};
	
}
