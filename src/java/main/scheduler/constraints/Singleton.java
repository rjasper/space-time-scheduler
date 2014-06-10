package scheduler.constraints;

import scheduler.constraints.RealSets.EmptyRealSet;
import scheduler.constraints.RealSets.FullRealSet;

public class Singleton implements RealSet {
	
	private final double value;
	
	private transient Singleton negative = null;

	public Singleton(double value) {
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	@Override
	public RealSet neg() {
		if (negative == null) {
			negative = new Singleton(-getValue());
			negative.negative = this;
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
			return RealSets.add(this, (Singleton) set);
		else if (set instanceof Interval)
			return RealSets.add(this, (Interval) set);
		else if (set instanceof Relation)
			return RealSets.add((Relation) set, this);
		else
			throw new IncompatibleRealSetError();
	}

	@Override
	public RealSet intersect(RealSet set) {
		return RealSets.intersect(this, set);
	}

	@Override
	public RealSet normalize() {
		return this;
	}

	@Override
	public boolean contains(double value) {
		return value == getValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
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
		Singleton other = (Singleton) obj;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{%f}", getValue());
	}

}
