package constraints;


public final class VariableFixtures {
	
	public static Variable[] mutualDependentVariables() {
		Variable a = new Variable("A");
		Variable b = new Variable("B");
		
		a.constrain(
			new Interval(0., 10.),
			new Relation(b, new Singleton(-5.))
		);
		b.constrain(
			new Interval(10., 20.),
			new Relation(a, new Singleton(5.))
		);
		
		a.ready();
		b.ready();
		
		return new Variable[] {a, b};
	}

}
