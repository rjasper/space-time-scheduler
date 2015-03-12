package matchers;

import java.util.Collection;

import org.hamcrest.Matcher;

import scheduler.Job;
import scheduler.JobSpecification;
import scheduler.PeriodicJobSpecification;

public final class JobMatchers {
	
	public static Matcher<Job> satisfies(JobSpecification spec) {
		return JobSatisfiesSpecification.satisfies(spec);
	}
	
	public static Matcher<Collection<Job>> satisfy(PeriodicJobSpecification spec) {
		return PeriodicJobsSatisfySpecification.satisfy(spec);
	}

}
