package scheduler.constraints;

public class Relation implements RealSet {
	
	private final Variable reference;
	
	private final RealSet offset;
	
	private transient RealSet calculatedSet = null;

	// TODO refuse RelativeRealSet as offset?
	public Relation(Variable reference, RealSet offset) {
		this.reference = reference;
		this.offset = offset;
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
	//		if (!reference.isComplete())
	//			throw new VariableIncompleteError();
			
			if (calculatedSet == null)
				calculatedSet = reference.getDomain().add(offset);
			
			return calculatedSet;
		}

	@Override
	public boolean contains(double value) {
		return normalize().contains(value);
	}
	
	@Override
	public String toString() {
		return String.format("%s + %s", reference.getName(), offset.toString());
	}

}
