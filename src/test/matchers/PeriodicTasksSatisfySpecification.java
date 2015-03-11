package matchers;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.vividsolutions.jts.geom.Point;

import scheduler.PeriodicTaskSpecification;
import scheduler.Task;
import scheduler.TaskSpecification;

public class PeriodicTasksSatisfySpecification extends TypeSafeMatcher<Collection<Task>> {
	
	@Factory
	public static Matcher<Collection<Task>> satisfy(PeriodicTaskSpecification spec) {
		return new PeriodicTasksSatisfySpecification(spec);
	}
	
	private final PeriodicTaskSpecification spec;

	public PeriodicTasksSatisfySpecification(PeriodicTaskSpecification spec) {
		this.spec = Objects.requireNonNull(spec, "spec");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("tasks satisfying ")
			.appendValue(spec);
	}

	@Override
	protected boolean matchesSafely(Collection<Task> item) {
		// spec.getTaskIds is not expected to be ever empty
		// but rather be sure since that behavior might be changed
		if (item.isEmpty() != spec.getTaskIds().isEmpty())
			return false;
		
		Map<UUID, Task> lookUp = item.stream()
			.collect(toMap(Task::getId, identity()));
		
		LocalDateTime periodStart = spec.getStartTime();
		for (UUID taskId : spec.getTaskIds()) {
			Task actual = lookUp.get(taskId);
			
			if (actual == null)
				return false;
			
			LocalDateTime periodFinish = periodStart.plus(spec.getPeriod());
			
			TaskSpecification taskSpec = new TaskSpecification(
				taskId,
				spec.getLocationSpace(),
				periodStart,
				periodFinish,
				spec.getDuration());
			
			Matcher<Task> taskMatcher = new TaskSatisfiesSpecification(taskSpec);
			
			if (!taskMatcher.matches(actual))
				return false;
			
			periodStart = periodFinish;
		}
		
		if (spec.isSameLocation()) {
			// first location
			Point location = item.iterator().next().getLocation();
			boolean sameLocation = item.stream()
				.allMatch(t -> t.getLocation().equals(location));
			
			if (!sameLocation)
				return false;
		}
		
		return true;
	}

}
