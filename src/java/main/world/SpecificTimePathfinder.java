package world;

import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Point;

public abstract class SpecificTimePathfinder extends Pathfinder {
	
	private Point startPoint;
	
	private Point finishPoint;
	
	private LocalDateTime startTime;
	
	private LocalDateTime finishTime;
	
	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& finishPoint != null
			&& startTime != null
			&& finishTime != null
			&& startTime.compareTo(finishTime) < 0;
	}

	protected Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	protected Point getFinishPoint() {
		return finishPoint;
	}

	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = finishPoint;
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