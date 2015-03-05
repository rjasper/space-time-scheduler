package world.util;

import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.TimeConv;
import world.ArcTimePath;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.util.Interpolator.InterpolationResult;

import com.google.common.collect.ImmutableList;

public class TrajectoryComposer {
	
	public static SimpleTrajectory compose(DecomposedTrajectory trajectory) {
		TrajectoryComposer composer = new TrajectoryComposer(trajectory);
		
		return composer.compose();
	}
	
	private final DecomposedTrajectory trajectory;
	
	private SpatialPath xyComponent;
	private ArcTimePath stComponent;
	
	private Interpolator<Double, ImmutablePoint> xyInterpolator;
	private Interpolator<Double, ImmutablePoint> stInterpolator;
	
	private ImmutableList.Builder<ImmutablePoint> locationsBuilder;
	private ImmutableList.Builder<LocalDateTime> timesBuilder;

	public TrajectoryComposer(DecomposedTrajectory trajectory) {
		this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
		
		init();
	}

	private void init() {
		xyComponent = trajectory.getSpatialPathComponent();
		stComponent = trajectory.getArcTimePathComponent();
		
		// seeks arcs
		Seeker<Double, SpatialPath.Vertex> xySeeker = new AffineLinearSeeker<>(
			xyComponent::getVertex,
			SpatialPath.Vertex::getArc,
			xyComponent.size());
		this.xyInterpolator =
			new PointPathInterpolator<SpatialPath.Vertex>(xySeeker);
		
		// seeks sub-index
		Seeker<Double, ArcTimePath.Vertex> stSeeker = new IndexSeeker<>(
			stComponent::getVertex,
			stComponent.size());
		this.stInterpolator =
			new PointPathInterpolator<ArcTimePath.Vertex>(stSeeker);
	}

	private void checkParameter() {
		if (trajectory == null)
			throw new IllegalStateException("trajectory has not been set");
	}
	
	public SimpleTrajectory compose() {
		checkParameter();
		
		if (trajectory.isEmpty())
			return SimpleTrajectory.empty();
		
		// preparations
		
		locationsBuilder = ImmutableList.builder();
		timesBuilder     = ImmutableList.builder();
		
		Iterable<ArcTimePath.Vertex> stVertices =
			() -> stComponent.vertexIterator();
		
		InterpolationResult<ImmutablePoint> lastXyRes = null;
		double lastArc = Double.NaN;
		
		// computes all vertices from spatial and arc-time components
		for (ArcTimePath.Vertex stVertex : stVertices) {
			int stIndex = stVertex.getIndex();
			double arc = stVertex.getX();
			double t = stVertex.getY();
			LocalDateTime time = makeTime(t);
			
			// interpolate location of current arc
			InterpolationResult<ImmutablePoint> xyRes = xyInterpolator.interpolate(arc);

			// collect all spatial vertices between the last and current arc
			if (stIndex > 0) {
				if (arc >= lastArc) {
					// go forward
					int start  = lastXyRes.getStartIndex()  + 1;
					int finish = xyRes    .getFinishIndex() - 1;
					for (int i = start; i <= finish; ++i)
						addSpatialVertices(i, stIndex-1, lastArc, arc);
				} else { // arc < lastArc
					// go backward
					int start  = lastXyRes.getFinishIndex() - 1;
					int finish = xyRes    .getStartIndex()  + 1;
					for (int i = start; i >= finish; --i)
						addSpatialVertices(i, stIndex-1, arc, lastArc);
				}
			}
			
			// add vertex of the current arc
			locationsBuilder.add(xyRes.get());
			timesBuilder.add(time);
			
			lastArc = arc;
			lastXyRes = xyRes;
		}
		
		ImmutableList<ImmutablePoint> locations = locationsBuilder.build();
		ImmutableList<LocalDateTime> times = timesBuilder.build();
		
		return new SimpleTrajectory(new SpatialPath(locations), times);
	}
	
	private LocalDateTime makeTime(double t) {
		return TimeConv.secondsToTime(t, trajectory.getBaseTime());
	}
	
	private void addSpatialVertices(int xyIndex, int stIndex, double lowArc, double highArc) {
		SpatialPath.Vertex xyVertex = xyComponent.getVertex(xyIndex);
		double xyArc = xyVertex.getArc();
		double stSubIndex = stIndex + (xyArc - lowArc) / (highArc - lowArc);
		
		InterpolationResult<ImmutablePoint> stRes = stInterpolator.interpolate(stSubIndex);
		ImmutablePoint stPoint = stRes.get();
		
		ImmutablePoint location = xyVertex.getPoint();
		double t = stPoint.getY();
		
		locationsBuilder.add(location);
		timesBuilder.add(makeTime(t));
	}
	
}
