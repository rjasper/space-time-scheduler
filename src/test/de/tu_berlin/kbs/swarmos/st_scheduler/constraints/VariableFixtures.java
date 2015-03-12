package de.tu_berlin.kbs.swarmos.st_scheduler.constraints;

import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.Interval;
import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.Relation;
import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.Singleton;
import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.Variable;
import de.tu_berlin.kbs.swarmos.st_scheduler.util.NameProvider;


public final class VariableFixtures {
	
	public static Variable[] mutualDependentVariables() {
		Variable a = new Variable();
		Variable b = new Variable();
		NameProvider.setNameFor(a, "A");
		NameProvider.setNameFor(b, "B");
		
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
