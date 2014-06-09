package scheduler.constraints;

import static fj.data.List.list;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static scheduler.constraints.RealSets.emptyRealSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import util.NameProvider;


public class Variable {
	
	private final String name;
	
	private final List<RealSet> constraints = new LinkedList<RealSet>();
	
	private RealSet domain = null;
	
	private boolean ready = false;
	
	public Variable() {
		this.name = NameProvider.nameFor(Variable.class);
	}
	
	public Variable(String name) {
		this.name = name;
	}
	
	/**
	 * @return the constraints
	 */
	public List<RealSet> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public boolean isEvaluated() {
		return domain != null;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public String getName() {
		return name;
	}

	private void setDomain(RealSet set) {
		domain = set;
	}
	
	private RealSet getDomain() {
		return domain;
	}
	
	public void constrain(RealSet... sets) {
		constrain(Arrays.asList(sets));
	}
	
	public void constrain(Collection<RealSet> sets) {
		if (isReady())
			throw new IllegalStateException("variable already ready");
		
		constraints.addAll(sets);
	}
	
	public void ready() {
		ready = true;
	}
	
	/**
	 * @return the domain
	 */
	public RealSet evaluate() {
		if (!isReady())
			throw new IllegalStateException("variable not ready");
		
		if (!isEvaluated())
			evaluate(list()); // sets domain
		
		return getDomain();
	}
	
//	public void evaluate() {
//		if (!isReady())
//			throw new IllegalStateException("variable not ready");
//		if (isEvaluated())
//			return;
//		
//		evaluate(list());
//	}

	private List<RealSet> evaluate(fj.data.List<Variable> trace) {
		if (isEvaluated()) {
			return singletonList(evaluate());
		} else {
			List<RealSet> all = new LinkedList<>();
			
			for (RealSet c : constraints)
				all.addAll(evaluateHelper(trace, c));
			
			List<Relation> relations = all.stream()
					.filter((c) -> (c instanceof Relation))
					.map((c) -> (Relation) c)
					.collect(toList());
			
			boolean satisfiable = relations.stream()
					.filter((r) -> r.getReference() == this)
					.allMatch((r) -> r.getOffset().contains(0.));
			
			if (!satisfiable) {
				setDomain(RealSets.emptyRealSet());
				
				return singletonList(emptyRealSet());
			}
			
			boolean evaluatable = relations.stream()
					.allMatch((c) -> ((Relation) c).getReference() == this);
			
			if (evaluatable) {
				RealSet domain = all.stream()
						.filter((c) -> !(c instanceof Relation))
						.reduce(RealSets.fullRealSet(),
							(a, b) -> a.intersect(b));
				
				setDomain(domain);
				
				return singletonList(domain);
			} else {
				return all.stream()
						.filter((c) -> !(c instanceof Relation)
								|| ((Relation) c).getReference() != this)
						.collect(toList());
			}
		}
	}
	
	private List<RealSet> evaluateHelper(fj.data.List<Variable> trace, RealSet constraint) {
		if (constraint instanceof Relation) {
			Relation relation = (Relation) constraint;
			
			if (relation.isEvaluated()) {
				return singletonList(relation.normalize());
			} else {
				Variable reference = relation.getReference();
				RealSet offset = relation.getOffset();
				
				// if reference is already in trace (don't loop)
				if (trace.exists((v) -> v == reference))
					return singletonList(relation);
				else
					return Arrays.asList(reference.evaluate(trace.cons(this)).stream()
							.map((c) -> c.add(offset))
							.toArray((n) -> new RealSet[n]));
			}
		} else {
			return singletonList(constraint);
		}
	}
	
	@Override
	public String toString() {
		if (isEvaluated()) {
			return String.format("%s = %s", getName(), getDomain().toString());
		} else {
			String constraintStr;
			if (constraints.isEmpty()) {
				constraintStr = RealSets.fullRealSet().toString();
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
