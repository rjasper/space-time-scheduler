package de.tu_berlin.kbs.swarmos.st_scheduler.matchers;

import java.util.Collection;

import org.hamcrest.Matcher;

import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Job;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.PeriodicJobSpecification;

public final class JobMatchers {
	
	public static Matcher<Job> satisfies(JobSpecification spec) {
		return JobSatisfiesSpecification.satisfies(spec);
	}
	
	public static Matcher<Collection<Job>> satisfy(PeriodicJobSpecification spec) {
		return PeriodicJobsSatisfySpecification.satisfy(spec);
	}

}
