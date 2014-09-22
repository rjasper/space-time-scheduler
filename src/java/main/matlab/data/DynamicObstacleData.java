package matlab.data;

public class DynamicObstacleData {
	
	private final double[] polygonData;
	
	private final double[] pathData;

	public DynamicObstacleData(double[] polygonData, double[] pathData) {
		this.polygonData = polygonData;
		this.pathData = pathData;
	}

	public double[] getPolygonData() {
		return polygonData;
	}

	public double[] getPathData() {
		return pathData;
	};

}
