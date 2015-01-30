package tasks.fixtures;

import static util.UUIDFactory.*;
import tasks.WorkerUnit;
import tasks.factories.WorkerUnitFactory;

public final class WorkerUnitFixtures {

	private static WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();

	public static WorkerUnit withTwoTasks1() {
		WorkerUnit worker = wFact.createWorkerUnit(0., 0.);

		wFact.addTaskWithDuration(worker, uuid("task1"), -3600.,    0., 2L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker, uuid("task2"),     0., 3600., 9L * 3600L, 3L * 3600L);

		return worker;
	}

	public static WorkerUnit withTwoTasks2() {
		WorkerUnit worker = wFact.createWorkerUnit(0., 0.);

		wFact.addTaskWithDuration(worker, uuid("task1"),    0., -3600., 2L * 3600L, 2L * 3600L);
		wFact.addTaskWithDuration(worker, uuid("task2"), 3600.,     0., 9L * 3600L, 2L * 3600L);

		return worker;
	}

	public static WorkerUnit withThreeTasks() {
		WorkerUnit worker = wFact.createWorkerUnit(0., 0.);

		wFact.addTaskWithDuration(worker, uuid("task1"), 10., 10.,  6L * 3600L, 1L * 3600L);
		wFact.addTaskWithDuration(worker, uuid("task2"), 20., 10., 12L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker, uuid("task3"), 20., 20., 18L * 3600L, 2L * 3600L);

		return worker;
	}

}
