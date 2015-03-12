package de.tu_berlin.kbs.swarmos.st_scheduler.constraints;

import static java.lang.Math.*;

public final class RealSets {
	
	private RealSets() {}
	
	private static final EmptyRealSet EMPTY_REAL_SET = new EmptyRealSet();
	
	private static final FullRealSet FULL_REAL_SET = new FullRealSet();

	public static final class EmptyRealSet implements RealSet {
		private EmptyRealSet() {}
	
		@Override
		public RealSet neg() {
			return emptyRealSet();
		}

		@Override
		public RealSet add(RealSet set) {
			return RealSets.add(this, set);
		}

		@Override
		public RealSet intersect(RealSet set) {
			return RealSets.intersect(this, set);
		}

		@Override
		public RealSet normalize() {
			return emptyRealSet();
		}

		@Override
		public boolean contains(double value) {
			return false;
		}

		@Override
		public String toString() {
			return "{}";
		}
	}
	
	public static final class FullRealSet implements RealSet {
		private FullRealSet() {}
		
		@Override
		public RealSet neg() {
			return fullRealSet();
		}

		@Override
		public RealSet add(RealSet set) {
			return RealSets.add(this, set);
		}

		@Override
		public RealSet intersect(RealSet set) {
			return RealSets.intersect(this, set);
		}

		@Override
		public RealSet normalize() {
			return fullRealSet();
		}

		@Override
		public boolean contains(double value) {
			return true;
		}

		@Override
		public String toString() {
			return "R";
		}
	}
	
	public static EmptyRealSet emptyRealSet() {
		return EMPTY_REAL_SET;
	}
	
	public static FullRealSet fullRealSet() {
		return FULL_REAL_SET;
	}
	
	public static RealSet intersect(EmptyRealSet s1, RealSet s2) {
		return emptyRealSet();
	}
	
	public static RealSet intersect(FullRealSet s1, RealSet s2) {
		return s2;
	}
	
	public static RealSet intersect(Singleton s1, RealSet s2) {
		if (s2.contains(s1.getValue()))
			return s1;
		else
			return emptyRealSet();
	}

	public static RealSet intersect(Interval s1, Interval s2) {
		double minValue = max(s1.getMinValue(), s2.getMinValue());
		double maxValue = min(s1.getMaxValue(), s2.getMaxValue());
		
		if (minValue > maxValue)
			return emptyRealSet();
		else if (minValue == maxValue)
			return new Singleton(minValue);
		else
			return new Interval(minValue, maxValue);
	}
	
	public static RealSet intersect(Relation s1, RealSet s2) {
		return s1.normalize().intersect(s2);
	}
	
	public static RealSet add(EmptyRealSet s1, RealSet s2) {
		return emptyRealSet();
	}
	
	public static RealSet add(FullRealSet s1, RealSet s2) {
		if (s2 instanceof EmptyRealSet)
			return emptyRealSet();
		else
			return fullRealSet();
	}
	
	public static RealSet add(Singleton s1, Singleton s2) {
		return new Singleton(s1.getValue() + s2.getValue());
	}
	
	public static RealSet add(Singleton s1, Interval s2) {
		double offset = s1.getValue();
		
		if (offset == 0.) {
			return s2;
		} else {
			double minValue = s2.getMinValue() + offset;
			double maxValue = s2.getMaxValue() + offset;
			
			return new Interval(minValue, maxValue);
		}
	}
	
	public static RealSet add(Interval s1, Interval s2) {
		double minValue1 = s1.getMinValue();
		double maxValue1 = s1.getMaxValue();
		double minValue2 = s2.getMinValue();
		double maxValue2 = s2.getMaxValue();
		
		return new Interval(minValue1 + minValue2, maxValue1 + maxValue2);
	}

	public static RealSet add(Relation s1, RealSet s2) {
		Variable reference = s1.getReference();
		
		if (reference.isEvaluated())
			return s1.normalize().add(s2);
		else
			return new Relation(reference, s1.getOffset().add(s2).normalize());
	}

}
