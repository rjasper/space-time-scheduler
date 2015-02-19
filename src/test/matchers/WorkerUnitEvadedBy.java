package matchers;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import scheduler.WorkerUnit;
import world.WorkerUnitObstacle;

public class WorkerUnitEvadedBy extends TypeSafeDiagnosingMatcher<WorkerUnit> {
	
	private WorkerUnit operand;
	
	public WorkerUnitEvadedBy(WorkerUnit operand) {
		this.operand = operand;
	}

	@Override
	protected boolean matchesSafely(WorkerUnit item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" wasn't evaded by ")
			.appendValue(operand);
		
		// true if item is evaded by the operand
		return item.getObstacleSections().stream()
			.map(WorkerUnitObstacle::getEvaders)
			.flatMap(Collection::stream)
			.map(WorkerUnitObstacle::getWorkerUnit)
			.anyMatch(operand::equals);
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker evaded by ")
			.appendValue(operand);
	}

}
