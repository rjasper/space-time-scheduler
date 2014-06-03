package scheduler.constraints;

import static java.lang.Math.*;

public final class RealSets {
	
	private RealSets() {}
	
	public static final EmptyRealSet EMPTY_REAL_SET = new EmptyRealSet();

	public static final class EmptyRealSet implements RealSet {
		private EmptyRealSet() {};
	
		@Override
		public RealSet neg() {
			return emptyRealSet();
		}

		@Override
		public RealSet add(RealSet set) {
			return emptyRealSet();
		}

		@Override
		public RealSet intersect(RealSet constraint) {
			return emptyRealSet();
		}

		@Override
		public boolean contains(double value) {
			return false;
		}
	}
	
	public static EmptyRealSet emptyRealSet() {
		return EMPTY_REAL_SET;
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

}
