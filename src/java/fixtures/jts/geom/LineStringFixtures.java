package jts.geom;

import jts.geom.factories.StaticJstFactories;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public final class LineStringFixtures {
	
	private static GeometryFactory geom() {
		return StaticJstFactories.geomFactory();
	}
	
	private static CoordinateSequenceFactory csFact() {
		return geom().getCoordinateSequenceFactory();
	}
	
	public static LineString twoPoints() {
		return geom().createLineString(csCreate(2,
			50., 40.,
			30., 30.)
		);
	}
	
	public static LineString threePoints() {
		return geom().createLineString(csCreate(2,
			 0.,  0.,
			10., 10.,
			10.,  0.)
		);
	}
	
	private static CoordinateSequence csCreate(int dim, double ...vals) {
		int n = vals.length / dim;
		
		CoordinateSequence cs = csFact().create(n, dim);
		
		int j = 0;
		for (int i = 0; i < n; ++i) {
			for (int d = 0; d < dim; ++d)
				cs.setOrdinate(i, d, vals[j++]);
		}
		
		return cs;
	}

}
