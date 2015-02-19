package scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Schedule {
	
	private Map<String, WorkerUnit> workerUnits = new HashMap<>();
	
	private List<ScheduleAlternative> alternatives = new LinkedList<>();
	
	public void addWorkerUnit(WorkerUnit worker) {
		WorkerUnit previous = workerUnits.putIfAbsent(worker.getId(), worker);
		
		if (previous != null)
			throw new IllegalArgumentException("worker id already assigned");
	}
	
	public WorkerUnit getWorkerUnit(String workerId) {
		WorkerUnit worker = workerUnits.get(
			Objects.requireNonNull(workerId, "workerId"));
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		return worker;
	}
	
	public void removeWorkerUnit(String workerId) {
		WorkerUnit worker = workerUnits.remove(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
	}
	
	public void addAlternative(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		// TODO check alternative
		
		alternatives.add(alternative);
		// TODO lock workers
	}
	
	public void integrate(ScheduleAlternative alternative) {
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		// TODO implement actual integration process
	}
	
	public void eliminate(ScheduleAlternative alternative) {
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		// TODO implement actual elimination process
	}

}
