package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Extends the {@code LineString} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * 
 * @author Rico
 */
public class ImmutablePolygon extends Polygon implements ImmutableGeometry {

	private static final long serialVersionUID = -1394928736389998201L;

	/**
	 * Constructs a new {@code Polygon} from the given polygon.
	 * 
	 * @param polygon
	 */
	public ImmutablePolygon(Polygon polygon) {
		this(retrieveShell(polygon), retrieveHoles(polygon), polygon.getFactory(), true);
	}

	/**
	 * Constructs a new {@code ImmutablePolygon} from the given shell and holes.
	 * 
	 * @param shell
	 * @param holes
	 * @param factory
	 */
	public ImmutablePolygon(LinearRing shell, LinearRing[] holes, GeometryFactory factory) {
		super(immutable(shell), immutable(holes), factory);
	}
	
	/**
	 * Constructs a new {@code ImmutablePolygon} from the given linear rings.
	 * Does not make a copy of the given array.
	 * 
	 * @param shell
	 * @param holes
	 * @param factory
	 * @param shared
	 */
	ImmutablePolygon(ImmutableLinearRing shell, ImmutableLinearRing[] holes, GeometryFactory factory, boolean shared) {
		super(shell, holes, factory);
		assert shared;
	}
	
	/**
	 * Retrieves the shell from the given polygon.
	 * 
	 * @param polygon
	 * @return the shell.
	 */
	private static ImmutableLinearRing retrieveShell(Polygon polygon) {
		return immutable((LinearRing) polygon.getExteriorRing());
	}
	
	/**
	 * Retrieves the holes from the given polygon.
	 * 
	 * @param polygon
	 * @return the holes.
	 */
	private static ImmutableLinearRing[] retrieveHoles(Polygon polygon) {
		if (polygon instanceof ImmutablePolygon)
			return (ImmutableLinearRing[]) ((ImmutablePolygon) polygon).holes;
		
		int n = polygon.getNumInteriorRing();
		
		ImmutableLinearRing[] holes = new ImmutableLinearRing[n];
		for (int i = 0; i < n; ++i)
			holes[i] = immutable((LinearRing) polygon.getInteriorRingN(i));
		
		return holes;
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public Polygon getMutable() {
		return new Polygon(mutable(shell), mutable(holes), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("Polygon immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Polygon#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("Polygon immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public Polygon norm() {
		Polygon mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Polygon#reverse()
	 */
	@Override
	public Geometry reverse() {
		int nHoles = this.holes.length;
		LinearRing shell = (LinearRing) this.shell.reverse();
		LinearRing[] holes = new LinearRing[nHoles];
		for (int i = 0; i < nHoles; i++)
			holes[i] = (LinearRing) this.holes[i].reverse();
		
		return getFactory().createPolygon(shell, holes);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Polygon#apply(com.vividsolutions.jts.geom.CoordinateFilter)
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
	 * @see com.vividsolutions.jts.geom.Polygon#apply(com.vividsolutions.jts.geom.CoordinateSequenceFilter)
	 */
	@Override
	public void apply(CoordinateSequenceFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
