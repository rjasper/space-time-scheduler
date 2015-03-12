package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

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

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.PeriodicJobSpecification;

public class PeriodicJobsSatisfySpecification extends TypeSafeMatcher<Collection<Job>> {
	
	@Factory
	public static Matcher<Collection<Job>> satisfy(PeriodicJobSpecification spec) {
		return new PeriodicJobsSatisfySpecification(spec);
	}
	
	private final PeriodicJobSpecification spec;

	public PeriodicJobsSatisfySpecification(PeriodicJobSpecification spec) {
		this.spec = Objects.requireNonNull(spec, "spec");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("jobs satisfying ")
			.appendValue(spec);
	}

	@Override
	protected boolean matchesSafely(Collection<Job> item) {
		// spec.getJobIds is not expected to be ever empty
		// but rather be sure since that behavior might be changed
		if (item.isEmpty() != spec.getJobIds().isEmpty())
			return false;
		
		Map<UUID, Job> lookUp = item.stream()
			.collect(toMap(Job::getId, identity()));
		
		LocalDateTime periodStart = spec.getStartTime();
		for (UUID jobId : spec.getJobIds()) {
			Job actual = lookUp.get(jobId);
			
			if (actual == null)
				return false;
			
			LocalDateTime periodFinish = periodStart.plus(spec.getPeriod());
			
			JobSpecification jobSpec = JobSpecification.createSF(
				jobId,
				spec.getLocationSpace(),
				periodStart,
				periodFinish,
				spec.getDuration());
			
			Matcher<Job> jobMatcher = new JobSatisfiesSpecification(jobSpec);
			
			if (!jobMatcher.matches(actual))
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
