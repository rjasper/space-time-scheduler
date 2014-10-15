package world.pathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import world.DynamicObstacle;
import world.Trajectory;

import com.vividsolutions.jts.geom.Polygon;

public abstract class Pathfinder {
	
	private List<Polygon> staticObstacles = Collections.emptyList();
	
	private List<DynamicObstacle> dynamicObstacles = Collections.emptyList();
	
	private double maxSpeed = 0.0;
	
	private Trajectory resultTrajectory = null;
	
	private List<DynamicObstacle> resultEvadedObstacles = null;

	public boolean isReady() {
		return maxSpeed > 0.0;
	};

	protected List<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> obstacles) {
		staticObstacles = new ArrayList<>(obstacles);
	};

	protected List<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> obstacles) {
		dynamicObstacles = new ArrayList<>(obstacles);
	};
	
	protected double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		if (maxSpeed <= 0.0 || Double.isInfinite(maxSpeed))
			throw new IllegalArgumentException("invalid max speed value");
		
		this.maxSpeed = maxSpeed;
	};

	public Trajectory getResultTrajectory() {
		return resultTrajectory;
	};

	protected void setResultTrajectory(Trajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	public List<DynamicObstacle> getResultEvadedObstacles() {
		return resultEvadedObstacles;
	};

	protected void setResultEvadedObstacles(
		List<DynamicObstacle> resultEvadedObstacles) {
		this.resultEvadedObstacles = resultEvadedObstacles;
	}
	
	public boolean calculatePath() {
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