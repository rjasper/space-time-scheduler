package scheduler.constraints;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Variable {
	
	// TODO remove
	private static int idCounter = 0;
	
	// TODO remove
	private int id = ++idCounter;
	
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
	
	// TODO remove
	public String getName() {
		return String.format("$%d", id);
	}

	/**
	 * @return the domain
	 */
	public RealSet getDomain() {
		if (!isReady())
			throw new VariableReadyError();
		if (!isComplete())
			throw new VariableIncompleteError();
		
		return domain;
	}
	
	private void setDomain(RealSet set) {
		domain = set;
	}

//	public void constrain(RealSet set) {
//		constrain(Collections.singleton(set));
//	}
	
	public void constrain(RealSet... sets) {
		constrain(Arrays.asList(sets));
	}
	
	public void constrain(Collection<RealSet> sets) {
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
		// TODO does not work with relative sets referencing incomplete variables
		RealSet intersection = constraints.stream().reduce(RealSets.fullRealSet(),
			(a, b) -> a.intersect(b)
		);
		
		setDomain(intersection.normalize());
	}
	
	@Override
	public String toString() {
		if (isComplete()) {
			return String.format("%s = %s", getName(), getDomain().toString());
		} else {
			String constraintStr;
			if (constraints.isEmpty()) {
				constraintStr = RealSets.fullRealSet().toString();
			} else {
				constraintStr = new String();
				
				for (RealSet c : constraints) {
					if (!constraintStr.isEmpty())
						constraintStr += " \u2229 ";
					
					constraintStr += c;
				}
			}
			
			return String.format("%s = %s", getName(), constraintStr);
		}
	}

}
