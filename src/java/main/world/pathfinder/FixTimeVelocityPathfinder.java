package world.pathfinder;

import com.vividsolutions.jts.geom.Point;

public abstract class FixTimeVelocityPathfinder extends VelocityPathfinder {

	private Point startPoint = null;
	
	private Point finishPoint = null;

	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& finishPoint != null;
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
	
}
