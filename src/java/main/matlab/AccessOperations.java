package matlab;

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
	
	public void assignStaticObstacle(String variableName, Object[] data) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		m.feval("assign_feval", variableName, "j2m_static_obstacles", data);
	}
	
	public Object[] retrieveStaticObstacles(String variableName) throws MatlabInvocationException {
		MatlabProxy m = getProxy();
		
		Object[] result = m.returningFeval("retrieve_feval", 1, "m2j_static_obstacles", variableName);
		Object[] data = (Object[]) result[0];
		
		return data;
	}

}
