package constraints;

public class Relation implements RealSet {
	
	private final Variable reference;
	
	private final RealSet offset;
	
	private transient RealSet normalized = null;

	public Relation(Variable reference, RealSet offset) {
		if (offset instanceof Relation)
			throw new IncompatibleRealSetError("cannot use a relation as offset");
		
		this.reference = reference;
		this.offset = offset.normalize();
	}

	/**
	 * @return the reference
	 */
	public Variable getReference() {
		return reference;
	}

	/**
	 * @return the offset
	 */
	public RealSet getOffset() {
		return offset;
	}
	
	@Override
	public RealSet neg() {
		return normalize().neg();
	}

	@Override
	public RealSet add(RealSet set) {
		return RealSets.add(this, set);
	}

	@Override
	public RealSet intersect(RealSet set) {
		return RealSets.intersect(this, set);
	}

	public RealSet normalize() {
		if (normalized == null)
			normalized = getReference().evaluate().add(getOffset());
		
		return normalized;
	}

	@Override
	public boolean contains(double value) {
		return normalize().contains(value);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		
		int result = 1;
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Relation other = (Relation) obj;
		
		if (offset == null && other.offset != null)
			return false;
		else if (!offset.equals(other.offset))
			return false;
		if (reference != other.reference)
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s + %s", getReference().getName(), getOffset().toString());
	}

}
