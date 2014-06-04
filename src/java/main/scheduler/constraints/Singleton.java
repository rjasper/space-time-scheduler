package scheduler.constraints;

public class Singleton implements RealSet {
	
	private final double value;
	
	private transient Singleton negSingleton = null;

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
		if (negSingleton == null) {
			negSingleton = new Singleton(-value);
			negSingleton.negSingleton = this;
		}
		
		return negSingleton;
	}

	@Override
	public RealSet add(RealSet set) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RealSet intersect(RealSet set) {
		return RealSets.intersect(this, set);
	}

	@Override
	public boolean contains(double value) {
		return value == this.value;
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
		return String.format("[%f]", value);
	}

}
