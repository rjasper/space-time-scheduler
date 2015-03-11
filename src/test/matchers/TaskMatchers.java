package matchers;

import java.util.Collection;

import org.hamcrest.Matcher;

import scheduler.PeriodicTaskSpecification;
import scheduler.Task;
import scheduler.TaskSpecification;

public final class TaskMatchers {
	
	public static Matcher<Task> satisfies(TaskSpecification spec) {
		return TaskSatisfiesSpecification.satisfies(spec);
	}
	
	public static Matcher<Collection<Task>> satisfy(PeriodicTaskSpecification spec) {
		return PeriodicTasksSatisfySpecification.satisfy(spec);
	}

}
