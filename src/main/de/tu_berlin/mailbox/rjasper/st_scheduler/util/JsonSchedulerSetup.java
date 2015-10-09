package de.tu_berlin.mailbox.rjasper.st_scheduler.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;

public class JsonSchedulerSetup {

	private static final ImmutablePolygon NODE_SHAPE = immutableBox(-0.5, -0.5, 0.5, 0.5);

	private static final double NODE_MAX_SPEED = 1.0;

	private LocalDateTime baseTime;
	
	private World world;
	
	private List<NodeSpecification> nodeSpecs;
	
	public JsonSchedulerSetup(JSONObject json, LocalDateTime baseTime) {
		this.baseTime = baseTime;
		this.world = jsonToWorld(json);
		this.nodeSpecs = jsonToNodeSpecs(json);
	}
	
	public World jsonToWorld(JSONObject json) {
		JSONArray jsonDynamicObstacles = json.getJSONArray("dynamicObstacles");
		int nDynamicObstacles = jsonDynamicObstacles.length();
		DynamicObstacle[] dynamicObstacles = new DynamicObstacle[nDynamicObstacles];
		
		for (int i = 0; i < nDynamicObstacles; ++i) {
			JSONObject dynamicObstacleJson = jsonDynamicObstacles.getJSONObject(i);
			DynamicObstacle dynamicObstacle = jsonToDynamicObstacle(dynamicObstacleJson);
			dynamicObstacles[i] = dynamicObstacle;
		}
		
		return new World(ImmutableList.of(), ImmutableList.copyOf(dynamicObstacles));
	}
	
	public DynamicObstacle jsonToDynamicObstacle(JSONObject json) {
		ImmutablePolygon shape = jsonToImmutablePolygon( json.getJSONArray("shape") );
		SpatialPath spatialPath = jsonToSpatialPath( json.getJSONArray("path") );
		ImmutableList<LocalDateTime> times = jsonToTimes( json.getJSONArray("times") );
		SimpleTrajectory trajectory = new SimpleTrajectory(spatialPath, times);
		
		return new DynamicObstacle(shape, trajectory);
	}
	
	public SpatialPath jsonToSpatialPath(JSONArray json) {
		int nPoints = json.length();
		ImmutablePoint[] points = new ImmutablePoint[nPoints];
		
		for (int i = 0; i < nPoints; ++i) {
			JSONObject jsonPoint = json.getJSONObject(i);
			ImmutablePoint point = jsonToImmutablePoint(jsonPoint);
			points[i] = point;
		}
		
		return new SpatialPath(ImmutableList.copyOf(points));
	}
	
	public ImmutablePolygon jsonToImmutablePolygon(JSONArray json) {
		int nOrdinates = json.length();
		double[] ordinates = new double[nOrdinates];
		
		for (int i = 0; i < nOrdinates; ++i)
			ordinates[i] = json.getDouble(i);
		
		return immutablePolygon(ordinates);
	}
	
	public ImmutablePoint jsonToImmutablePoint(JSONObject json) {
		double x = json.getDouble("x");
		double y = json.getDouble("y");
		
		return immutablePoint(x, y);
	}
	
	public ImmutableList<LocalDateTime> jsonToTimes(JSONArray json) {
		int nTimes = json.length();
		LocalDateTime[] times = new LocalDateTime[nTimes];
		
		for (int i = 0; i < nTimes; ++i) {
			double t = json.getDouble(i);
			times[i] = atSecond(t);
		}
		
		return ImmutableList.copyOf(times);
	}
	
	private LocalDateTime atSecond(double second) {
		Duration offset = TimeConv.secondsToDurationSafe(second);
		
		return baseTime.plus(offset);
	}
	
	public List<NodeSpecification> jsonToNodeSpecs(JSONObject json) {
		JSONArray jsonNodeSpecs = json.getJSONArray("nodesLocations");
		int nNodeSpecs = jsonNodeSpecs.length();
		List<NodeSpecification> nodeSpecs = new ArrayList<>(nNodeSpecs);
		
		for (int i = 0; i < nNodeSpecs; ++i) {
			JSONObject jsonNodeSpec = jsonNodeSpecs.getJSONObject(i);
			NodeSpecification nodeSpec = jsonToNodeSpec(jsonNodeSpec);
			nodeSpecs.add(nodeSpec);
		}
		
		return nodeSpecs;
	}
	
	private int nodeCounter = 0;

	public NodeSpecification jsonToNodeSpec(JSONObject json) {
		ImmutablePoint location = jsonToImmutablePoint(json);
		String nodeId = String.format("node#%d", nodeCounter++);
		
		return new NodeSpecification(nodeId, NODE_SHAPE, NODE_MAX_SPEED, location, baseTime);
	}

	public LocalDateTime getBaseTime() {
		return baseTime;
	}

	public World getWorld() {
		return world;
	}

	public List<NodeSpecification> getNodeSpecs() {
		return nodeSpecs;
	}

}
