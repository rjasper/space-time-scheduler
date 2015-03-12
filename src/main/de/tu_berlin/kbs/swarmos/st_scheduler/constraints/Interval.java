package de.tu_berlin.kbs.swarmos.st_scheduler.constraints;

import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.RealSets.EmptyRealSet;
import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.RealSets.FullRealSet;

public class Interval implements RealSet {
	
	private final double minValue;
	
	private final double maxValue;
	
	/**
	 * Saves the corresponding negative interval.
	 * 
	 * This avoids the creation of multiple redundant interval objects when
	 * calling {@link #neg()}.
	 */
	private transient RealSet negative = null;
	
	private transient RealSet normalized = null;

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
		if (negative == null) {
			RealSet normalized = normalize();
			
			if (normalized instanceof Singleton)
				negative = normalized.neg();
			else {
				Interval interval = new Interval(-getMaxValue(), -getMinValue());
				interval.negative = this;
				
				negative = interval;
			}
		}
		
		return negative;
	}

	@Override
	public RealSet add(RealSet set) {
		if (set instanceof EmptyRealSet)
			return RealSets.add((EmptyRealSet) set, this);
		else if (set instanceof FullRealSet)
			return RealSets.add((FullRealSet) set, this);
		else if (set instanceof Singleton)
			return RealSets.add((Singleton) set, this);
		else if (set instanceof Interval)
			return RealSets.add(this, (Interval) set);
		else if (set instanceof Relation)
			return RealSets.add((Relation) set, this);
		else
			throw new IncompatibleRealSetError();
	}

	@Override
	public RealSet intersect(RealSet set) {
		if (set instanceof EmptyRealSet)
			return RealSets.intersect((EmptyRealSet) set, this);
		else if (set instanceof FullRealSet)
			return RealSets.intersect((FullRealSet) set, this);
		else if (set instanceof Singleton)
			return RealSets.intersect((Singleton) set, this);
		else if (set instanceof Interval)
			return RealSets.intersect(this, (Interval) set);
		else if (set instanceof Relation)
			return RealSets.intersect((Relation) set, this);
		else
			throw new IncompatibleRealSetError();
	}

	@Override
	public RealSet normalize() {
		if (normalized != null)
			return normalized;
		
		double minValue = getMinValue();
		double maxValue = getMaxValue();
		
		if (minValue == maxValue)
			normalized = new Singleton(minValue);
		else
			normalized = this;
			
		return normalized;
	}

	@Override
	public boolean contains(double value) {
		return value >= getMinValue() && value <= getMaxValue();
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
		return String.format("[%f, %f]", getMinValue(), getMaxValue());
	}

}
