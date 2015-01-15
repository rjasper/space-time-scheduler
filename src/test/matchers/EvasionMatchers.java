package matchers;

import static org.hamcrest.CoreMatchers.*;

import java.time.LocalDateTime;

import org.hamcrest.Matcher;

import tasks.WorkerUnit;

public final class EvasionMatchers {
	
	private EvasionMatchers() {}
	
	public static Matcher<WorkerUnit> isEvadedBy(WorkerUnit operand) {
		return new WorkerUnitEvadedBy(operand);
	}
	
	public static Matcher<Iterable<? super WorkerUnit>> areEvadedBy(WorkerUnit operand) {
		return hasItem(isEvadedBy(operand));
	}
	
	public static Matcher<WorkerUnit> isEvadedBy(WorkerUnit operand, LocalDateTime timeOfSegment) {
		return new WorkerUnitEvadedByAt(operand, timeOfSegment);
	}
	
	public static Matcher<Iterable<WorkerUnit>> evadedByNumTimes(WorkerUnit operand, int times) {
		return new WorkerUnitEvadedByNumTimes(operand, times);
	}

}

