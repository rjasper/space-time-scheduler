package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;
import java.util.Collections;

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
	 */
	public void setStartPoint(Point startPoint) {
		this.startPoint = immutable(startPoint);
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
	 */
	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = immutable(finishPoint);
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
		double t1 = from.getY(), t2 = to.getY();
		
		// TODO reconsider weight cost function
		// for fix time pretty dumb
		// maybe: square error to average speed
		// (maxArc/duration - (s2-s1)/(t2-t1))^2
		
		return t2 - t1;
	}

}
