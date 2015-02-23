package scheduler;

import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class ScheduleAlternative {
	
	private final ImmutableList<WorkerUnitScheduleUpdate> updates;

	public ScheduleAlternative(
		ImmutableList<WorkerUnitScheduleUpdate> updates)
	{
		this.updates = Objects.requireNonNull(updates, "updates");
	}

	public ImmutableList<WorkerUnitScheduleUpdate> getUpdates() {
		return updates;
	}

}
