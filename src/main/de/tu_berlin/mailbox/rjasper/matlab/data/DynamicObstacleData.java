package de.tu_berlin.mailbox.rjasper.matlab.data;

public class DynamicObstacleData {
	
	private final double[] polygonData;
	
	private final double[] pathData;
	
	private final double[] timesData;

	public DynamicObstacleData(double[] polygonData, double[] pathData, double[] timesData) {
		this.polygonData = polygonData;
		this.pathData = pathData;
		this.timesData = timesData;
	}

	public double[] getPolygonData() {
		return polygonData;
	}

	public double[] getPathData() {
		return pathData;
	}

	public double[] getTimesData() {
		return timesData;
	};

}
