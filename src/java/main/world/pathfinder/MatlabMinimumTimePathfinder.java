package world.pathfinder;

import static matlab.ConvertOperations.j2mDuration;
import static matlab.ConvertOperations.j2mDynamicObstacles;
import static matlab.ConvertOperations.j2mLocalDateTime;
import static matlab.ConvertOperations.j2mPoint;
import static matlab.ConvertOperations.j2mStaticObstacles;
import static matlab.ConvertOperations.m2jTrajectory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import world.DynamicObstacle;
import world.Trajectory;
import matlab.AccessOperations;
import matlab.MatlabAccess;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class MatlabMinimumTimePathfinder extends MinimumTimePathfinder {
	
	private final MatlabProxy proxy;
	
	private final AccessOperations access;
	
	public MatlabMinimumTimePathfinder() {
		this(MatlabAccess.getProxy());
	}
	
	public MatlabMinimumTimePathfinder(MatlabProxy proxy) {
		this.proxy = proxy;
		this.access = new AccessOperations(proxy);
	}

	private MatlabProxy getProxy() {
		return proxy;
	}

	private AccessOperations getAccess() {
		return access;
	}

	@Override
	protected boolean calculatePathImpl() {
		if (!isReady())
			throw new IllegalStateException("invalid parameters");
		
		Point startPoint = getStartPoint();
		Point finishPoint = getFinishPoint();
		LocalDateTime startTime = getStartTime();
		LocalDateTime earliestFinishTime = getEarliestFinishTime();
		LocalDateTime latestFinishTime = getLatestFinishTime();
		Duration spareTime = getSpareTime();
		double maxSpeed = getMaxSpeed();
		Collection<Polygon> staticObstacles = getStaticObstacles();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		
		MatlabProxy m = getProxy();
		AccessOperations acc = getAccess();
		
		try {
			acc.assingPoint("I", j2mPoint(startPoint, 2));
			acc.assingPoint("F", j2mPoint(finishPoint, 2));
			m.setVariable("t_start", j2mLocalDateTime(startTime));
			m.setVariable("t_end_min", j2mLocalDateTime(earliestFinishTime));
			m.setVariable("v_max", maxSpeed);
			m.setVariable("t_end_max", j2mLocalDateTime(latestFinishTime));
			m.setVariable("t_spare", j2mDuration(spareTime));
			acc.assignStaticObstacles("Os", j2mStaticObstacles(staticObstacles));
			acc.assignDynamicObstacles("Om", j2mDynamicObstacles(dynamicObstacles));
			
			Object[] result = m.returningEval("pathfinder_mt(I, F, t_start, t_end_min, t_end_max, v_max, t_spare, Os, Om)", 2);
			
			double[] trajectoryData = (double[]) result[0];
			double[] evasionsDouble = (double[]) result[1];
			
			int[] evasions = Arrays.stream(evasionsDouble)
				.mapToInt((d) -> (int) d - 1).toArray();

			Trajectory trajectory = m2jTrajectory(trajectoryData);
			setResultTrajectory(trajectory.isEmpty() ? null : trajectory);
			storeEvasions(evasions);
			
			return !trajectory.isEmpty();
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		return false; // unreachable
	}

	private void storeEvasions(int[] evasions) {
		List<DynamicObstacle> obstacles = getDynamicObstacles();
		
		List<DynamicObstacle> evadedObstacles = Arrays.stream(evasions)
			.mapToObj((e) -> obstacles.get(e)).collect(Collectors.toList());
		
		setResultEvadedObstacles(evadedObstacles);
	}

}
