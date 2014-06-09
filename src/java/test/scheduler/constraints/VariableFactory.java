package scheduler.constraints;

public final class VariableFactory {
	
	public static Variable emptyVariable() {
		Variable var = new Variable();
		
		var.constrain(RealSets.emptyRealSet());
		var.ready();
		var.eval();
		
		return var;
	}
	
	public static Variable realVariable() {
		Variable var = new Variable();
		
		var.ready();
		var.eval();
		
		return var;
	}
	
	public static Variable singletonVariable(double value) {
		Variable var = new Variable();
		
		var.constrain(new Singleton(value));
		var.ready();
		var.eval();
		
		return var;
	}
	
	public static Variable intervalVariable(double minValue, double maxValue) {
		Variable var = new Variable();
		
		var.constrain(new Interval(minValue, maxValue));
		var.ready();
		var.eval();
		
		return var;
	}
	
	public static Variable relativeVariable(Variable reference, RealSet offset) {
		Variable var = new Variable();
		
		var.constrain(new Relation(reference, offset));
		var.ready();
		var.eval();
		
		return var;
	}
	
	public static Variable relativeVariable(Variable reference, double offset) {
		return relativeVariable(reference, new Singleton(offset));
	}
	
	public static Variable relativeVariable(Variable reference, double minOffset, double maxOffset) {
		return relativeVariable(reference, new Interval(minOffset, maxOffset));
	}
	
	public static Variable customVariable(RealSet... constraints) {
		Variable var = new Variable();
		
		var.constrain(constraints);
		var.ready();
		var.eval();
		
		return var;
	}

}
