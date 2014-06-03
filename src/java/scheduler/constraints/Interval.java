package scheduler.constraints;

import scheduler.constraints.RealSets.EmptyRealSet;

public class Interval implements RealSet {
	
	private final double minValue;
	
	private final double maxValue;
	
	private transient Interval negInterval = null;

	public Interval(double minValue, double maxValue) {
		if (minValue > maxValue)
			throw new IllegalArgumentException("minValue cannot be greater than maxValue");
		
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	@Override
	public RealSet neg() {
		if (negInterval == null) {
			negInterval = new Interval(-maxValue, -minValue);
			negInterval.negInterval = this;
		}
		
		return negInterval;
	}

	@Override
	public RealSet add(RealSet set) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RealSet intersect(RealSet set) {
		if (set instanceof EmptyRealSet)
			return RealSets.emptyRealSet();
		else if (set instanceof Singleton)
			return RealSets.intersect((Singleton) set, this);
		else if (set instanceof Interval)
			return RealSets.intersect(this, (Interval) set);
		else
			throw new IncompatibleRealSetError();
	}

	@Override
	public boolean contains(double value) {
		return value >= minValue && value <= maxValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interval other = (Interval) obj;
		if (Double.doubleToLongBits(maxValue) != Double
				.doubleToLongBits(other.maxValue))
			return false;
		if (Double.doubleToLongBits(minValue) != Double
				.doubleToLongBits(other.minValue))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[%f, %f]", minValue, maxValue);
	}

}
