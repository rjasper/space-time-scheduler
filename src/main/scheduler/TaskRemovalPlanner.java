package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;

public class TaskRemovalPlanner {
	
	private World world = null;
	
	private WorldPerspective worldPerspective = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private Task task = null;
	
	private boolean fixedEnd = true;

	private void checkParameters() {
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(worldPerspective, "worldPerspective");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(task, "task");
	}
	
	public boolean plan() {
		checkParameters();
		
		if (!task.getFinishTime().isBefore(frozenHorizonTime))
			return false;
		
		// XXX last edition
		// TODO implement
		
		return false;
	}

}
