package tasks;

import jts.geom.factories.StaticJtsFactories;

import com.vividsolutions.jts.io.WKTReader;

public final class WorkerUnitFixtures {
	
	private static WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();
	
	public static WKTReader wkt() {
		return StaticJtsFactories.wktReader();
	}
	
	public static WorkerUnit withTwoTasks1() {
		WorkerUnit worker = wFact.createWorkerUnit(-3600., 0.);
		
		wFact.addTaskWithDuration(worker, -3600.,    0., 0L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker,     0., 3600., 7L * 3600L, 3L * 3600L);
		
		return worker;
	}
	
	public static WorkerUnit withTwoTasks2() {
		WorkerUnit worker = wFact.createWorkerUnit(0., -3600.);
		
		wFact.addTaskWithDuration(worker,    0., -3600., 0L * 3600L, 2L * 3600L);
		wFact.addTaskWithDuration(worker, 3600.,     0., 7L * 3600L, 2L * 3600L);
		
		return worker;
	}
	
	public static WorkerUnit withThreeTasks() {
		WorkerUnit worker = wFact.createWorkerUnit(0., 0.);
		
		wFact.addTaskWithDuration(worker, 10., 10.,  6L * 3600L, 1L * 3600L);
		wFact.addTaskWithDuration(worker, 20., 10., 12L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker, 20., 20., 18L * 3600L, 2L * 3600L);
		
		return worker;
	}

}
