package matchers;

import org.hamcrest.Matcher;

import tasks.Task;
import tasks.TaskSatisfiesSpecification;
import tasks.TaskSpecification;

public final class TaskMatchers {
	
	public static Matcher<Task> satisfies(TaskSpecification spec) {
		return TaskSatisfiesSpecification.satisfies(spec);
	}

}
