package world.pathfinder;

import java.util.Collection;
import java.util.Collections;

import com.vividsolutions.jts.geom.Point;

public class FixTimeMeshBuilder extends ArcTimeMeshBuilder {
	
	private Point startPoint = null;
	
	private Point finishPoint = null;
	
	private Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	private Point getFinishPoint() {
		return finishPoint;
	}

	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = finishPoint;
	}

	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& finishPoint != null;
	}

	@Override
	protected Collection<Point> buildStartVertices() {
		return Collections.singleton( getStartPoint() );
	}

	@Override
	protected Collection<Point> buildFinishVertices() {
		return Collections.singleton( getFinishPoint() );
	}

	@Override
	protected double calculateWeight(Point from, Point to) {
		double t1 = from.getY(), t2 = to.getY();
		
		// TODO reconsider weight cost function
		// for fix time pretty dumb
		// maybe: square error to average speed
		// (maxArc/duration - (s2-s1)/(t2-t1))^2
		
		return t2 - t1;
	}

}
