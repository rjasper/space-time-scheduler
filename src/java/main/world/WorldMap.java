package world;

import geom.factories.StaticJstFactories;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class WorldMap {
	
	private static final Geometry EMPTY_GEOMETRY = geomFactory().createMultiPolygon(null);
	
	private static GeometryFactory geomFactory() {
		return StaticJstFactories.geomFactory();
	}
	
	private static Geometry emptyGeometry() {
		return EMPTY_GEOMETRY;
	}
	
	private final Set<Polygon> obstacles = new HashSet<>();
	
	private Geometry map;
	
	private boolean ready = false;
	
	public WorldMap() {}
	
	public boolean isReady() {
		return _isReady();
	}
	
	private boolean _isReady() {
		return this.ready;
	}
	
	private void _setReady(boolean ready) {
		this.ready = ready;
	}
	
	public Geometry getMap() {
		if (!_isReady())
			throw new IllegalStateException("not ready yet");
		
		Geometry map = _getMap();
		
		return geomFactory().createGeometry(map);
	}
	
	Geometry _getMap() {
		return this.map;
	}
	
	private void _setMap(Geometry map) {
		this.map = map;
	}
	
	private Set<Polygon> _getObstacles() {
		return this.obstacles;
	}
	
	public void add(Polygon obstacle) {
		if (_isReady())
			throw new IllegalStateException("already ready");
		if (obstacle == null)
			throw new NullPointerException("obstacle cannot be null");
		
		_add(obstacle);
	}
	
	public void add(Polygon... obstacles) {
		if (_isReady())
			throw new IllegalStateException("already ready");
		if (obstacles == null)
			throw new NullPointerException("obstacle cannot be null");
		
		for (Polygon o : obstacles)
			_add(o);
	}
	
	private void _add(Polygon obstacle) {
		Set<Polygon> set = _getObstacles();
		
		set.add(obstacle);
	}
	
	public void ready() {
		if (_isReady())
			throw new IllegalStateException("already ready");
		
		Set<Polygon> obstacles = _getObstacles();
		
		Geometry map = obstacles.stream()
			.map((o) -> (Geometry) o)
			.reduce((acc, o) -> acc.union(o))
			.orElse(emptyGeometry());
		
		_setMap(map);
		_setReady(true);
	}
	
	public Geometry space(Geometry mask) {
		if (!_isReady())
			throw new IllegalStateException("not ready yet");
		
		Geometry map = _getMap();
		
		Geometry space = mask.difference(map);
		
		return space;
	}

}
