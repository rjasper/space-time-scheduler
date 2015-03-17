package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static java.util.stream.Collectors.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;
import static de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Stream;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySplitter;

public class LazyMeshBuilder {

	private double startArc = Double.NaN;

	private double finishArc = Double.NaN;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	private LocalDateTime baseTime = null;

	private Collection<ForbiddenRegion> forbiddenRegions = null;

	private double maxVelocity = Double.NaN;

	private double lazyVelocity = Double.NaN;

	private Duration minStopDuration = Duration.ZERO;

	private transient ImmutablePoint startVertex;

	private transient ImmutablePoint finishVertex;

	private transient Collection<ImmutablePoint> forbiddenRegionVertices;

	private transient Geometry forbiddenMap;

	private transient Collection<Ray> rays;

	private static class Ray {
		public final ImmutablePoint origin;
		public final LineString line;
		public Ray(ImmutablePoint origin, LineString line) {
			this.origin = origin;
			this.line = line;
		}
	}

	public void setStartArc(double startArc) {
		if (!Double.isFinite(startArc))
			throw new IllegalArgumentException("illegal arc");

		this.startArc = startArc;
	}

	public void setFinishArc(double finishArc) {
		if (!Double.isFinite(finishArc))
			throw new IllegalArgumentException("illegal arc");

		this.finishArc = finishArc;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = requireNonNull(startTime, "startTime");
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = requireNonNull(finishTime, "finishTime");
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = requireNonNull(baseTime, "baseTime");
	}

	public void setForbiddenRegions(Collection<ForbiddenRegion> forbiddenRegions) {
		this.forbiddenRegions = requireNonNull(forbiddenRegions, "forbiddenRegions");;
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.maxVelocity = maxVelocity;
	}

	public void setLazyVelocity(double lazyVelocity) {
		if (!Double.isFinite(lazyVelocity) || lazyVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.lazyVelocity = lazyVelocity;
	}

	public void setMinStopDuration(Duration minStopDuration) {
		requireNonNull(minStopDuration, "minStopDuration");

		if (minStopDuration.isNegative())
			throw new IllegalArgumentException("illegal duration");

		this.minStopDuration = minStopDuration;
	}

	private void checkParameters() {
		if (Double.isNaN(startArc)     ||
			Double.isNaN(finishArc)    ||
			startTime        == null   ||
			finishTime       == null   ||
			forbiddenRegions == null   ||
			Double.isNaN(maxVelocity)  ||
			Double.isNaN(lazyVelocity))
		{
			throw new IllegalStateException("unset parameters");
		}

		if (startArc >= finishArc)
			throw new IllegalStateException("startArc >= finishArc");
		if (startTime.compareTo(finishTime) >= 0)
			throw new IllegalStateException("startTime >= finishTime");
		if (maxVelocity < lazyVelocity)
			throw new IllegalStateException("maxVelocity < lazyVelocity");
	}

	public DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge>
	build() {
		checkParameters();

		init();
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph =
			buildImpl();
		cleanUp();

		return graph;
	}

	public DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge>
	buildImpl() {
		// TODO implement

		// cast lazy velocity
		calcRays();

		// debug
		LineString[] lines = rays.stream()
			.map(r -> r.line)
			.toArray(n -> new LineString[n]);

		System.out.println( multiLineString(lines) );
		System.out.println( forbiddenMap );

		return null; // TODO
	}

	private void init() {
		startVertex = makeVertex(startArc, startTime);
		finishVertex = makeVertex(finishArc, finishTime);
		forbiddenRegionVertices = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.map(Geometry::getCoordinates)
			.flatMap(Arrays::stream)
			.map(this::makeVertex)
			.collect(toSet());
		forbiddenMap = makeForbiddenMap();
//		rays = new LinkedList<>();
	}

	private void cleanUp() {
		startVertex = null;
		finishVertex = null;
		forbiddenRegionVertices = null;
		forbiddenMap = null;
		rays = null;
	}

	private ImmutablePoint makeVertex(double arc, LocalDateTime time) {
		return immutablePoint(arc, timeToSeconds(time, baseTime));
	}

	private ImmutablePoint makeVertex(Coordinate coordinate) {
		return immutablePoint(coordinate.x, coordinate.y);
	}

	private Geometry makeForbiddenMap() {
		Geometry[] regions = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(n -> new Geometry[n]);

		return geometryCollection(regions);
	}

	private void calcRays() {
		// TODO only cast from tangent points
		Stream<ImmutablePoint> origins = Stream.concat(
			Stream.of(finishVertex), forbiddenRegionVertices.stream());

		rays = origins
			.map(this::calcLazyRay)
			.filter(r -> r != null)
			.collect(toList());
	}

	private Ray calcLazyRay(ImmutablePoint origin) {
		double s1 = origin.getX();
		double s2 = startArc;
		double ds = s1 - s2;
		double t1 = origin.getY();
		double t2 = t1 - ds/lazyVelocity;

		LineString fullLine = immutableLineString(s1, t1, s2, t2);

		Geometry intersection = forbiddenMap.intersection(fullLine);

		double s3 = s2;
		double t3 = t2;

		// seek first line string intersection
		int n = intersection.getNumGeometries();
		for (int i = 0; i < n; ++i) {
			Geometry g = intersection.getGeometryN(i);

			if (g instanceof LineString) {
				LineString line = (LineString) g;
				Point startPoint = line.getStartPoint();

				if (startPoint.equalsTopo(origin))
					return null;

				// if earlier collision
				if (startPoint.getY() > t3) {
					s3 = startPoint.getX();
					t3 = startPoint.getY();
				}
			}
		}

		// if no line string intersection
		if (t3 == t2)
			return new Ray(origin, fullLine);
		else
			return new Ray(origin, immutableLineString(s1, t1, s3, t3));
	}

}
