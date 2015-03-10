package matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import scheduler.Task;
import scheduler.TaskSpecification;

public class TaskSatisfiesSpecification extends TypeSafeMatcher<Task> {
	
	@Factory
	public static Matcher<Task> satisfies(TaskSpecification spec) {
		return new TaskSatisfiesSpecification(spec);
	}
	
	private final TaskSpecification spec;

	public TaskSatisfiesSpecification(TaskSpecification spec) {
		this.spec = spec;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a task satisfying ")
			.appendValue(spec);
	}

	@Override
	protected boolean matchesSafely(Task item) {
		if (!item.getId      ().equals   ( spec.getTaskId()            ))
			return false;
		if (!item.getLocation().within   ( spec.getLocationSpace()     ))
			return false;
		if (item.getStartTime().compareTo( spec.getEarliestStartTime() ) < 0)
			return false;
		if (item.getStartTime().compareTo( spec.getLatestStartTime()   ) > 0)
			return false;
		if (item.getDuration ().compareTo( spec.getDuration()          ) != 0)
			return false;
		
		return true;
	}

}
