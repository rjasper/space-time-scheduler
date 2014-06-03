package scheduler.constraints;

public class RelativeRealSet implements RealSet {
	
	private final Variable reference;
	
	private final RealSet offset;
	
	private transient RealSet calculatedSet = null;

	public RelativeRealSet(Variable reference, RealSet offset) {
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
	
	public RealSet getCalculatedSet() {
		if (!reference.isComplete())
			throw new VariableIncompleteError();
		
		if (calculatedSet == null)
			calculatedSet = reference.getDomain().add(offset);
		
		return calculatedSet;
	}

	@Override
	public RealSet neg() {
		return getCalculatedSet().neg();
	}

	@Override
	public RealSet add(RealSet set) {
		if (reference.isComplete())
			return getCalculatedSet().add(set);
		else
			return new RelativeRealSet(reference, offset.add(set));
	}

	@Override
	public RealSet intersect(RealSet set) {
		return getCalculatedSet().neg();
	}

	@Override
	public boolean contains(double value) {
		return getCalculatedSet().contains(value);
	}

}
