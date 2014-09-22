package matlab.data;

public class LineStringData {
	
	private final int dimension;
	
	private final double[] data;

	public LineStringData(double[] data, int dimension) {
		this.dimension = dimension;
		this.data = data;
	}

	public int getDimension() {
		return dimension;
	}

	public double[] getData() {
		return data;
	}
	
	public int size() {
		return data.length;
	}

}
