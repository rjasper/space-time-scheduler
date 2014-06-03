package scheduler.constraints;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Variable {
	
	private final List<RealSet> constraints = new LinkedList<RealSet>();
	
	private RealSet domain = null;
	
	private boolean ready = false;
	
	/**
	 * @return the constraints
	 */
	public List<RealSet> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public boolean isComplete() {
		return domain != null;
	}
	
	public boolean isReady() {
		return ready;
	}

	/**
	 * @return the domain
	 */
	public RealSet getDomain() {
		return domain;
	}

	public void constrain(RealSet set) {
		constrain(Collections.singleton(set));
	}
	
	public void constrain(Set<RealSet> sets) {
		if (isReady())
			throw new VariableReadyError();
		
		constraints.addAll(sets);
	}
	
	public void ready() {
		ready = true;
	}
	
	public void complete() {
		if (isComplete())
			return;
		
		calculateDomain();
	}
	
	private void calculateDomain() {
		// TODO implement
	}

}
