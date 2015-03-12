package matchers;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import scheduler.Job;
import scheduler.JobSpecification;

public class JobSatisfiesSpecification extends TypeSafeMatcher<Job> {
	
	@Factory
	public static Matcher<Job> satisfies(JobSpecification spec) {
		return new JobSatisfiesSpecification(spec);
	}
	
	private final JobSpecification spec;

	public JobSatisfiesSpecification(JobSpecification spec) {
		this.spec = Objects.requireNonNull(spec, "spec");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a job satisfying ")
			.appendValue(spec);
	}

	@Override
	protected boolean matchesSafely(Job item) {
		if (!item.getId      ().equals   ( spec.getJobId()            ))
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
