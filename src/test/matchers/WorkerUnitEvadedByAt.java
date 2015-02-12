package matchers;

import java.time.LocalDateTime;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import tasks.WorkerUnit;
import world.WorkerUnitObstacle;

public class WorkerUnitEvadedByAt extends TypeSafeDiagnosingMatcher<WorkerUnit> {
	
	private final WorkerUnit operand;
	
	private final LocalDateTime timeOfSegment;

	public WorkerUnitEvadedByAt(WorkerUnit operand, LocalDateTime timeOfSegment) {
		this.operand = operand;
		this.timeOfSegment = timeOfSegment;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker evaded by ")
			.appendValue(operand)
			.appendText(" moving along a segment present at ")
			.appendValue(timeOfSegment);
	}

	@Override
	protected boolean matchesSafely(WorkerUnit item, Description mismatchDescription) {
		WorkerUnitObstacle segment = item.getObstacleSection(timeOfSegment);
		
		mismatchDescription
		.appendValue(item)
		.appendText(" was not evaded by ")
		.appendValue(operand);
		
		if (segment == null) {
			mismatchDescription
				.appendText(" moving along a segment present at ")
				.appendValue(timeOfSegment);
			
			return false;
		} else {
			mismatchDescription
				.appendText(" moving along ")
				.appendValue(segment);
			
			return segment.getEvaders().stream()
				.map(WorkerUnitObstacle::getWorkerUnit)
				.anyMatch(operand::equals);
		}
	}

}
