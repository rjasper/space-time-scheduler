package world.pathfinder;

import java.time.LocalDateTime;


public abstract class FixTimeVelocityPathfinder extends VelocityPathfinder {
	
	private LocalDateTime startTime = null;
	
	private LocalDateTime finishTime = null;

	public boolean isReady() {
		return super.isReady()
			&& startTime!= null
			&& finishTime!= null;
	}

	protected LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	protected LocalDateTime getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = finishTime;
	}

}
