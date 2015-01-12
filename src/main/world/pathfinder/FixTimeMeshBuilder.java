package world.pathfinder;

import java.util.Collection;
import java.util.Collections;

import jts.geom.util.GeometriesRequire;

import com.vividsolutions.jts.geom.Point;

/**
 * The {@code FixTimeMeshBuilder} implements an {@link ArcTimeMeshBuilder}.
 * It builds a graph with exactly one start vertex and one finish vertex. Both
 * points have to be set before building the graph. Since both points are
 * configured in advance the time is also fixated.
 * 
 * @author Rico
 */
public class FixTimeMeshBuilder extends ArcTimeMeshBuilder {
	
	/**
	 * The start vertex.
	 */
	private Point startPoint = null;
	
	/**
	 * The finish vertex.
	 */
	private Point finishPoint = null;
	
	/**
	 * @return the start vertex.
	 */
	private Point getStartPoint() {
		return startPoint;
	}

	/**
	 * Sets the start vertex.
	 * 
	 * @param startPoint
	 * @throws NullPointerException
	 *             if startPoint is {@code null}
	 */
	public void setStartPoint(Point startPoint) {
		GeometriesRequire.requireValid2DPoint(startPoint, "startPoint");
		
		this.startPoint = startPoint;
	}

	/**
	 * @return the finish vertex.
	 */
	private Point getFinishPoint() {
		return finishPoint;
	}

	/**
	 * Sets the finish vertex.
	 * 
	 * @param finishPoint
	 * @throws NullPointerException
	 *             if finishPoint is {@code null}
	 */
	public void setFinishPoint(Point finishPoint) {
		GeometriesRequire.requireValid2DPoint(finishPoint, "finishPoint");
		
		this.finishPoint = finishPoint;
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#isReady()
	 */
	@Override
	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& finishPoint != null;
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#buildStartVertices()
	 */
	@Override
	protected Collection<Point> buildStartVertices() {
		return Collections.singleton( getStartPoint() );
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#buildFinishVertices()
	 */
	@Override
	protected Collection<Point> buildFinishVertices() {
		return Collections.singleton( getFinishPoint() );
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#calculateWeight(com.vividsolutions.jts.geom.Point, com.vividsolutions.jts.geom.Point)
	 */
	@Override
	protected double calculateWeight(Point from, Point to) {
		// calculates the square error of the points' velocity in respect to the
		// average speed
		
		double s1 = from.getX(), s2 = to.getX();
		double t1 = from.getY(), t2 = to.getY();
		double v = (s2-s1) / (t2-t1);
		
		double totalLength   = finishPoint.getX() - startPoint.getX();
		double totalDuration = finishPoint.getY() - startPoint.getY();
		double vAvr = totalLength/totalDuration;
		
		double error = vAvr - v;
		
		return error*error;
	}

}
