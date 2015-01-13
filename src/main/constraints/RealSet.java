package constraints;

public interface RealSet {
	
	public RealSet neg();
	
	public RealSet add(RealSet set);
	
	public default RealSet sub(RealSet set) {
		return add(set.neg());
	}

	public RealSet intersect(RealSet set);
	
	public boolean contains(double value);
	
	public RealSet normalize();
	
	public default boolean equivalent(RealSet other) {
		return normalize().equals(other.normalize());
	}

}
