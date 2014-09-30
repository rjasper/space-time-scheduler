package world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jts.geom.factories.StaticJtsFactories;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class World {
	
	private static final Geometry EMPTY_GEOMETRY = geomFactory().createMultiPolygon(null);
	
	private static GeometryFactory geomFactory() {
		return StaticJtsFactories.geomFactory();
	}
	
	private static Geometry emptyGeometry() {
		return EMPTY_GEOMETRY;
	}
	
	private final Set<Polygon> obstacles = new HashSet<>();
	
	private Geometry map;
	
	private boolean ready = false;
	
	public World() {}
	
	public boolean isReady() {
		return this.ready;
	}
	
	private void setReady(boolean ready) {
		this.ready = ready;
	}
	
	public Geometry getMap() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		Geometry map = _getMap();
		
		return geomFactory().createGeometry(map);
	}
	
	public Collection<Polygon> getPolygonMap() {
		return Collections.unmodifiableCollection( getObstacles() );
	}
	
	Geometry _getMap() {
		return this.map;
	}
	
	private void setMap(Geometry map) {
		this.map = map;
	}
	
	private Set<Polygon> getObstacles() {
		return this.obstacles;
	}
	
	public void add(Polygon obstacle) {
		if (isReady())
			throw new IllegalStateException("already ready");
		if (obstacle == null)
			throw new NullPointerException("obstacle cannot be null");
		
		_add(obstacle);
	}
	
	public void add(Polygon... obstacles) {
		if (isReady())
			throw new IllegalStateException("already ready");
		if (obstacles == null)
			throw new NullPointerException("obstacle cannot be null");
		
		for (Polygon o : obstacles)
			_add(o);
	}
	
	private void _add(Polygon obstacle) {
		Set<Polygon> set = getObstacles();
		
		set.add(obstacle);
	}
	
	public void ready() {
		if (isReady())
			throw new IllegalStateException("already ready");
		
		Set<Polygon> obstacles = getObstacles();
		
		Geometry map = obstacles.stream()
			.map((o) -> (Geometry) o)
			.reduce((acc, o) -> acc.union(o))
			.orElse(emptyGeometry());
		
		setMap(map);
		setReady(true);
	}
	
	public Geometry space(Geometry mask) {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		Geometry map = _getMap();
		
		Geometry space = mask.difference(map);
		
		return space;
	}

}
