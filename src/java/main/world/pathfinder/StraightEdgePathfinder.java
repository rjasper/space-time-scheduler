package world.pathfinder;

import static straightedge.geom.path.PathBlockingObstacleImpl.createObstacleFromOuterPolygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Polygon;

import straightedge.geom.PolygonConverter;
import straightedge.geom.path.NodeConnector;
import straightedge.geom.path.PathBlockingObstacle;
import straightedge.geom.path.PathBlockingObstacleImpl;

public class StraightEdgePathfinder extends SpatialPathfinder {
	
	private NodeConnector<PathBlockingObstacle> nodeConnector = null;
	
	private double maxConnectionDistance;
	
	public StraightEdgePathfinder(double maxConnectionDistance) {
		this.maxConnectionDistance = maxConnectionDistance;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		NodeConnector<PathBlockingObstacle> nc = new NodeConnector<>();
		PolygonConverter conv = new PolygonConverter();
		
		double maxConnectionDistance = getMaxConnectionDistance();
		
		ArrayList<PathBlockingObstacle> obstacles = staticObstacles.stream()
			.map((p) -> conv.makeKPolygonFromExterior(p))
			.map((kp) -> createObstacleFromOuterPolygon(kp))
			.collect(Collectors.toCollection(ArrayList::new));
		
		for (PathBlockingObstacle o : obstacles)
			nc.addObstacle(o, obstacles, maxConnectionDistance);
	}

	private NodeConnector<PathBlockingObstacle> getNodeConnector() {
		return nodeConnector;
	}

	private void setNodeConnector(NodeConnector<PathBlockingObstacle> nodeConnector) {
		this.nodeConnector = nodeConnector;
	}

	private double getMaxConnectionDistance() {
		return maxConnectionDistance;
	}

	public void setMaxConnectionDistance(double maxConnectionDistance) {
		this.maxConnectionDistance = maxConnectionDistance;
	}

	@Override
	protected boolean calculatePathImpl() {
		// TODO Auto-generated method stub
		return false;
	}

}
