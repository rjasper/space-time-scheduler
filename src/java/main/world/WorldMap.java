package world;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class WorldMap {
	
	private static final GeometryFactory factory = new GeometryFactory();
	
	private static final Geometry EMPTY_GEOMETRY = factory().createGeometryCollection(null);
	
	private static GeometryFactory factory() {
		return factory;
	}
	
	private static Geometry emptyGeometry() {
		return EMPTY_GEOMETRY;
	}
	
	private Set<Geometry> obstacles = new HashSet<>();
	
	private Geometry map = factory().createGeometryCollection(null); // empty
	
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
		
		return factory().createGeometry(map);
	}
	
	Geometry _getMap() {
		return this.map;
	}
	
	private void _setMap(Geometry map) {
		this.map = map;
	}
	
	private Set<Geometry> _getObstacles() {
		return this.obstacles;
	}
	
	public void add(Geometry obstacle) {
		if (_isReady())
			throw new IllegalStateException("already ready");
		if (obstacle == null)
			throw new NullPointerException("obstacle cannot be null");
		
		_add(obstacle);
	}
	
	public void add(Geometry... obstacles) {
		if (_isReady())
			throw new IllegalStateException("already ready");
		if (obstacles == null)
			throw new NullPointerException("obstacle cannot be null");
		
		for (Geometry o : obstacles)
			_add(o);
	}
	
	private void _add(Geometry obstacle) {
		Set<Geometry> set = _getObstacles();
		
		set.add(obstacle);
	}
	
	public void ready() {
		if (_isReady())
			throw new IllegalStateException("already ready");
		
		Set<Geometry> obstacles = _getObstacles();
		
		Geometry map = obstacles.stream()
			.reduce((acc, o) -> acc.union(o))
			.orElse(emptyGeometry());
		
		_setMap(map);
		_setReady(true);
	}

}
