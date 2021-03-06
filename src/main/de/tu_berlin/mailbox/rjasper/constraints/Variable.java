package de.tu_berlin.mailbox.rjasper.constraints;

import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.emptyRealSet;
import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.fullRealSet;
import static fj.data.List.list;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.tu_berlin.mailbox.rjasper.util.NameProvider;


public class Variable {
	
//	private final String name;
	
	private final List<RealSet> constraints = new LinkedList<RealSet>();
	
	private transient RealSet evaluation = null;
	
	private boolean ready = false;
	
	public Variable() {
//		this.name = NameProvider.nameFor(this);
	}
	
//	public Variable(String name) {
//		this.name = name;
//	}
	
	public boolean isReady() {
		return ready;
	}

	public boolean isEvaluated() {
		return getEvaluation() != null;
	}

	private void setEvaluation(RealSet set) {
		evaluation = set;
	}

	private RealSet getEvaluation() {
		return evaluation;
	}

	public String getName() {
		return NameProvider.nameFor(this);
	}

	public List<RealSet> getConstraints() {
		return Collections.unmodifiableList(_getConstraints());
	}
	
	private List<RealSet> _getConstraints() {
		return constraints;
	}

	public void constrain(RealSet... sets) {
		constrain(Arrays.asList(sets));
	}
	
	public void constrain(Collection<RealSet> sets) {
		if (isReady())
			throw new IllegalStateException("variable already ready");
		
		_getConstraints().addAll(sets);
	}
	
	public void ready() {
		ready = true;
	}
	
	public RealSet evaluate() {
		if (!isReady())
			throw new IllegalStateException("variable not ready");
		if (!isEvaluated())
			evaluateImpl(list()); // sets #evaluation
		
		return getEvaluation();
	}

	private List<RealSet> evaluateImpl(fj.data.List<Variable> trace) {
		if (isEvaluated()) {
			return singletonList(getEvaluation());
		} else {
			// collect all constraints including those from relation references
			List<RealSet> all = new LinkedList<>();
			for (RealSet c : _getConstraints())
				all.addAll(evaluateImplHelper(trace, c));
			
			// filter relations
			List<Relation> relations = all.stream()
				.filter((c) -> (c instanceof Relation))
				.map((c) -> (Relation) c)
				.collect(toList());
			
			// Check if there are relations with references to this variable.
			// In such a case the equation is only satisfiable if the offset
			// can be chosen as zero.
			boolean satisfiable = relations.stream()
				.filter((r) -> r.getReference() == this)
				.allMatch((r) -> r.getOffset().contains(0.));
			
			if (!satisfiable) {
				setEvaluation(emptyRealSet());
				
				return singletonList(emptyRealSet());
			}
			
			// check if the variable can be fully evaluated (i.e., no relations
			// with other variables)
			boolean evaluable = relations.stream()
					.allMatch((r) -> r.getReference() == this);
			
			if (evaluable) {
				// merge all constraints together but ignore relations since 
				// they are equal to the intersection of the remaining constraints
				RealSet evaluation = all.stream()
					.filter((c) -> !(c instanceof Relation))
					.reduce((a, b) -> a.intersect(b))
					.orElse(fullRealSet())
					.normalize();
				
				setEvaluation(evaluation);
				
				return singletonList(evaluation);
			} else {
				// return all constraints except relations referencing this
				// variable since they are equal to the intersection of the
				// remaining constraints
				return all.stream()
					.filter((c) -> !(c instanceof Relation)
						|| ((Relation) c).getReference() != this)
					.collect(toList());
			}
		}
	}
	
	private List<RealSet> evaluateImplHelper(fj.data.List<Variable> trace, RealSet constraint) {
		if (constraint instanceof Relation) {
			Relation relation = (Relation) constraint;
			Variable reference = relation.getReference();
			
			if (reference.isEvaluated()) {
				return singletonList(relation.normalize());
			} else {
				RealSet offset = relation.getOffset();
				
				if (!reference.isReady())
					throw new IllegalStateException("reference not ready");
				
				// if reference is already in trace (don't loop)
				if (trace.exists((v) -> v == reference))
					return singletonList(relation);
				else
					return reference.evaluateImpl(trace.cons(this)).stream()
						.map((c) -> c.add(offset))
						.collect(toList());
			}
		} else {
			return singletonList(constraint);
		}
	}
	
	@Override
	public String toString() {
		if (isEvaluated()) {
			return String.format("%s = %s", getName(), getEvaluation().toString());
		} else {
			List<RealSet> constraints = _getConstraints();
			
			String constraintStr;
			if (constraints.isEmpty()) {
				constraintStr = fullRealSet().toString();
			} else {
				constraintStr = new String();
				
				for (RealSet c : constraints) {
					if (!constraintStr.isEmpty())
						constraintStr += " \u2229 "; // \u2229 = $\cap$
					
					constraintStr += c;
				}
			}
			
			return String.format("%s = %s", getName(), constraintStr);
		}
	}

}
