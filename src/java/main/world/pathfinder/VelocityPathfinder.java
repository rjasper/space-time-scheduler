package world.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

import world.DynamicObstacle;
import world.Trajectory;

public abstract class VelocityPathfinder {
	
	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private LineString spatialPath = null;
	
	private double maxSpeed = 0.0;
	
	private Trajectory resultTrajectory = null;
	
	private List<DynamicObstacle> resultEvadedObstacles = null;
	
	public boolean isReady() {
		return spatialPath != null
			&& maxSpeed > 0.0;
	}

	protected List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
	}

	protected LineString getSpatialPath() {
		return spatialPath;
	}

	public void setSpatialPath(LineString spatialPath) {
		if (spatialPath == null)
			throw new NullPointerException("path cannot be null");
		if (spatialPath.getCoordinateSequence().getDimension() != 2)
			throw new IllegalArgumentException("invalid path dimension");
		if (spatialPath.getNumPoints() < 2)
			throw new IllegalArgumentException("path too short");
		
		this.spatialPath = spatialPath;
	}

	protected double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		if (maxSpeed <= 0.0 || !Double.isFinite(maxSpeed))
			throw new IllegalArgumentException("invalid maximum speed value");
		
		this.maxSpeed = maxSpeed;
	}

	public Trajectory getResultTrajectory() {
		return resultTrajectory;
	}

	protected void setResultTrajectory(Trajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	public List<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	}

	protected void setResultEvadedObstacles(List<DynamicObstacle> resultEvadedObstacles) {
		this.resultEvadedObstacles = resultEvadedObstacles;
	}
	
	public final boolean calculatePath() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		boolean status = calculatePathImpl();
		
		if (!status) {
			setResultTrajectory(null);
			setResultEvadedObstacles(null);
		}
		
		return status;
	}
	
	protected abstract boolean calculatePathImpl();

}
