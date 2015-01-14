//package jts.geom.immutable;
//
//import util.ArraysClone;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.CoordinateSequence;
//import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
//import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
//import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
//
//public class ImmutableCoordinateSequenceFactory implements CoordinateSequenceFactory {
//	
//	private final CoordinateSequenceFactory internal;
//	
//	private final boolean sharedCoordinates;
//	
//	public ImmutableCoordinateSequenceFactory() {
//		this(CoordinateArraySequenceFactory.instance());
//	}
//
//	public ImmutableCoordinateSequenceFactory(CoordinateSequenceFactory factory) {
//		this(factory, sharesCoordinates(factory));
//	}
//	
//	private ImmutableCoordinateSequenceFactory(CoordinateSequenceFactory factory, boolean sharedCoordinates) {
//		this.internal = factory;
//		this.sharedCoordinates = sharedCoordinates;
//	}
//	
//	protected static boolean sharesCoordinates(CoordinateSequenceFactory factory) {
//		if (factory instanceof CoordinateArraySequenceFactory)
//			return true;
//		else if (factory instanceof PackedCoordinateSequenceFactory)
//			return false;
//		
//		throw new IllegalArgumentException("unknown factory");
//	}
//
//	@Override
//	public ImmutableCoordinateSequence create(Coordinate[] coordinates) {
//		if (sharedCoordinates)
//			coordinates = ArraysClone.deepCloneCopy(coordinates);
//		
//		return new ImmutableCoordinateSequence(internal.create(coordinates), true);
//	}
//	
//	ImmutableCoordinateSequence create(Coordinate[] coordinates, boolean shared) {
//		assert shared;
//		
//		return new ImmutableCoordinateSequence(internal.create(coordinates), true);
//	}
//
//	@Override
//	public ImmutableCoordinateSequence create(CoordinateSequence coordSeq) {
//		return new ImmutableCoordinateSequence(internal.create(coordSeq), true);
//	}
//
//	@Override
//	public ImmutableCoordinateSequence create(int size, int dimension) {
//		// this method doesn't really have much of a use case since the
//		// coordinate sequence cannot be modified once created
//		return new ImmutableCoordinateSequence(internal.create(size, dimension));
//	}
//
//}
