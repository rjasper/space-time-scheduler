package world;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class DynamicObstacle {
	
	private final Polygon polygon;
	
	private final LineString path;

	public DynamicObstacle(Polygon polygon, LineString path) {
		this.polygon = (Polygon) polygon;
		this.path = (LineString) path;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public LineString getPath() {
		return path;
	}

}
