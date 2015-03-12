package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * <p>
 * Extends the {@code MultiLineString} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 * 
 * <p>
 * Note that the {@link ImmutableGeometryCollection} is not the super type of
 * {@code ImmutableMultiLineString}.
 * </p>
 * 
 * @author Rico
 */
public class ImmutableMultiLineString extends MultiLineString implements ImmutableGeometry {

	private static final long serialVersionUID = 5998127758585768022L;

	/**
	 * Constructs a new {@code ImmutableMultiLineString} from the given
	 * multi line string.
	 * 
	 * @param multiLineString
	 */
	public ImmutableMultiLineString(MultiLineString multiLineString) {
		this(retrieveLineStrings(multiLineString), multiLineString.getFactory(), true);
	}

	/**
	 * Constructs a new {@code ImmutableMultiLineString} from the given
	 * line strings.
	 * 
	 * @param lineStrings
	 * @param factory
	 */
	public ImmutableMultiLineString(LineString[] lineStrings, GeometryFactory factory) {
		super(immutable(lineStrings), factory);
	}

	/**
	 * Constructs a new {@code ImmutableMultiLineString} from the given
	 * line strings. Does not make a copy of the given array.
	 * 
	 * @param lineStrings
	 * @param factory
	 * @param shared
	 *            has to be {@code true}
	 */
	ImmutableMultiLineString(ImmutableLineString[] lineStrings, GeometryFactory factory, boolean shared) {
		super(lineStrings, factory);
		assert shared;
	}

	/**
	 * Retrieves the line strings from the given multi line string.
	 * 
	 * @param multiLineString
	 * @return the line strings.
	 */
	private static ImmutableLineString[] retrieveLineStrings(MultiLineString multiLineString) {
		if (multiLineString instanceof ImmutableMultiLineString)
			return (ImmutableLineString[]) ((ImmutableMultiLineString) multiLineString).geometries;
		
		int n = multiLineString.getNumGeometries();
		
		ImmutableLineString[] lineStrings = new ImmutableLineString[n];
		for (int i = 0; i < n; ++i)
			lineStrings[i] = immutable((LineString) multiLineString.getGeometryN(i));
		
		return lineStrings;
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public MultiLineString getMutable() {
		LineString[] lineStrings = (LineString[]) geometries;
		
		return new MultiLineString(mutable(lineStrings), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("MultiLineString immutable");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiLineString immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public MultiLineString norm() {
		MultiLineString mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#apply(com.vividsolutions.jts.geom.CoordinateFilter)
	 */
	@Override
	public void apply(CoordinateFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#apply(com.vividsolutions.jts.geom.CoordinateSequenceFilter)
	 */
	@Override
	public void apply(CoordinateSequenceFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
