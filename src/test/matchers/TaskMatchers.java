package matchers;

import org.hamcrest.Matcher;

import scheduler.Task;
import scheduler.TaskSatisfiesSpecification;
import scheduler.TaskSpecification;

public final class TaskMatchers {
	
	public static Matcher<Task> satisfies(TaskSpecification spec) {
		return TaskSatisfiesSpecification.satisfies(spec);
	}

}
