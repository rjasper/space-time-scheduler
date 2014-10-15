package world;

import java.time.Duration;
import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Point;

public abstract class MinimumTimePathfinder extends Pathfinder {
	
	private Point startPoint;
	
	private Point finishPoint;
	
	private LocalDateTime startTime;
	
	private LocalDateTime earliestFinishTime;
	
	private LocalDateTime latestFinishTime;
	
	private Duration spareTime;


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

	protected LocalDateTime getEarliestFinishTime() {
		return earliestFinishTime;
	}

	public void setEarliestFinishTime(LocalDateTime earliestFinishTime) {
		this.earliestFinishTime = earliestFinishTime;
	}

	protected LocalDateTime getLatestFinishTime() {
		return latestFinishTime;
	}

	public void setLatestFinishTime(LocalDateTime latestFinishTime) {
		this.latestFinishTime = latestFinishTime;
	}

	protected Duration getSpareTime() {
		return spareTime;
	}

	public void setSpareTime(Duration spareTime) {
		this.spareTime = spareTime;
	}

}