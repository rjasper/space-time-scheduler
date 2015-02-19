package matchers;

import java.util.Collection;
import java.util.stream.StreamSupport;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import scheduler.WorkerUnit;
import world.WorkerUnitObstacle;

public class WorkerUnitEvadedByNumTimes extends TypeSafeDiagnosingMatcher<Iterable<WorkerUnit>> {

	private final WorkerUnit operand;
	
	private final int expectedTimes;

	public WorkerUnitEvadedByNumTimes(WorkerUnit operand, int expectedTimes) {
		if (operand == null)
			throw new NullPointerException("operand is null");
		
		this.operand = operand;
		this.expectedTimes = expectedTimes;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker evaded by ")
			.appendValue(operand)
			.appendText(" ")
			.appendValue(expectedTimes)
			.appendText(" times");
	}

	@Override
	protected boolean matchesSafely(Iterable<WorkerUnit> item, Description mismatchDescription) {
		long actualTimes = StreamSupport.stream(item.spliterator(), false)
			.map(WorkerUnit::getObstacleSections)
			.flatMap(Collection::stream)
			.map(WorkerUnitObstacle::getEvaders)
			.flatMap(Collection::stream)
			.map(WorkerUnitObstacle::getWorkerUnit)
			.filter(operand::equals)
			.count();
		
		boolean status = actualTimes == expectedTimes;
		
		mismatchDescription
			.appendText("number of times being evaded by ")
			.appendValue(operand)
			.appendText(" was ")
			.appendValue(actualTimes)
			.appendText(" instead of ")
			.appendValue(expectedTimes);
		
		return status;
	}

}
