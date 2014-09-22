package matlab;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Vector;

import matlab.data.DynamicObstacleData;
import matlab.data.LineStringData;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

public class AccessOperations {
	
	private final MatlabProxy proxy;
	
	public AccessOperations(MatlabProxy proxy) {
		this.proxy = proxy;
	}
	
	protected MatlabProxy getProxy() {
		return proxy;
	}
	
	public void assingPoint(String variableName, double[] data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("assign_feval", variableName, "j2m_point", data);
	}
	
	public double[] retrievePoint(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 1, "m2j_point", variableName);
		double[] data = (double[]) result[0];
		
		return data;
	}
	
	public void assignLineString(String variableName, LineStringData lsData) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		double[] data = lsData.getData();
		int dim = lsData.getDimension();
		
		m.feval("assign_feval", variableName, "j2m_line_string", data, dim);
	}
	
	public LineStringData retrieveLineString(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 2, "m2j_line_string", variableName);
		double[] data = (double[]) result[0];
		int dim = (int) ((double []) result[1])[0];
		
		return new LineStringData(data, dim);
	}

	public void assignPolygon(String variableName, double[] data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("assign_feval", variableName, "j2m_polygon", data);
	}

	public double[] retrievePolygon(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 1, "m2j_polygon", variableName);
		double[] data = (double []) result[0];
		
		return data;
	}
	
	public void assignStaticObstacles(String variableName, Object[] data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("assign_feval", variableName, "j2m_static_obstacles", data);
	}
	
	public Object[] retrieveStaticObstacles(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 1, "m2j_static_obstacles", variableName);
		Object[] data = (Object[]) result[0];
		
		return data;
	}
	
	public void assignDynamicObstacle(String variableName, DynamicObstacleData data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		double[] polygonData = data.getPolygonData();
		double[] pathData = data.getPathData();
		
		m.feval("assign_feval", variableName, "j2m_dynamic_obstacle", polygonData, pathData);
	}
	
	public DynamicObstacleData retrieveDynamicObstacle(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 2, "m2j_dynamic_obstacle", variableName);
		double[] polygonData = (double[]) result[0];
		double[] pathData = (double[]) result[1];
		
		return new DynamicObstacleData(polygonData, pathData);
	}
	
	public void assignDynamicObstacles(String variableName, Collection<DynamicObstacleData> data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] polygonsData = data.stream()
			.map(DynamicObstacleData::getPolygonData)
			.collect(toList())
			.toArray();
		Object[] pathsData = data.stream()
			.map(DynamicObstacleData::getPathData)
			.collect(toList())
			.toArray();

		m.feval("assign_feval", variableName, "j2m_dynamic_obstacles", polygonsData, pathsData);
	}
	
	public Collection<DynamicObstacleData> retrieveDynamicObstacles(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 2, "m2j_dynamic_obstacles", variableName);
		Object[] polygonsData = (Object[]) result[0];
		Object[] pathsData = (Object[]) result[1];
		
		int n = polygonsData.length;
		Collection<DynamicObstacleData> obstaclesData = new Vector<>(n);
		
		for (int i = 0; i < n; ++i) {
			double[] polygonData = (double[]) polygonsData[i];
			double[] pathData = (double[]) pathsData[i];
			
			obstaclesData.add(new DynamicObstacleData(polygonData, pathData));
		}
		
		return obstaclesData;
	}

}
