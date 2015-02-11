package world.util;

import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;

import world.ArcTimePath;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.util.Interpolator.InterpolationResult;

public class TrajectoryComposer2 {
	
	private DecomposedTrajectory trajectory = null;
	
	private SimpleTrajectory resultTrajectory = null;

	public void setTrajectory(DecomposedTrajectory trajectory) {
		this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
	}

	private void checkParameter() {
		if (trajectory == null)
			throw new IllegalStateException("trajectory has not been set");
	}
	
	public SimpleTrajectory compose() {
		// XXX last edition
		// TODO implement
		checkParameter();
		
		if (trajectory.isEmpty())
			return SimpleTrajectory.empty();
		
		SpatialPath xyComponent = trajectory.getSpatialPathComponent();
		ArcTimePath stComponent = trajectory.getArcTimePathComponent();
		
		// seeks arcs
		Seeker<Double, SpatialPath.Vertex> xySeeker = new AffineLinearSeeker<>(
			xyComponent::getVertex,
			SpatialPath.Vertex::getArc,
			xyComponent.size());
		Interpolator<Double, ImmutablePoint> xyInterpolator =
			new PointPathInterpolator<SpatialPath.Vertex>(xySeeker);
		
		// seeks sub-index
		Seeker<Double, ArcTimePath.Vertex> stSeeker = new AffineLinearSeeker<>(
			stComponent::getVertex,
			v -> (double) v.getIndex(),
			stComponent.size());
		Interpolator<Double, ImmutablePoint> stInterpolator =
			new PointPathInterpolator<ArcTimePath.Vertex>(stSeeker);
		
		Iterator<ArcTimePath.Vertex> st = stComponent.vertexIterator();
		
		ImmutableList.Builder<ImmutablePoint> locationsBuilder = ImmutableList.builder();
		ImmutableList.Builder<LocalDateTime> timesBuilder = ImmutableList.builder();
		
		int startIndex = 0; // arbitrary number, will be overwritten before read
		
		while (st.hasNext()) {
			ArcTimePath.Vertex stVertex = st.next();
			
			// TODO add locations between arc-time vertices
			
			double arc = stVertex.getX();
			double t = stVertex.getY();
			LocalDateTime time = makeTime(t);
			InterpolationResult<ImmutablePoint> res = xyInterpolator.interpolate(t);

			if (stVertex.getIndex() > 0) {
				int finishIndex = res.getFinishIndex();
				for (int i = startIndex+1; i < finishIndex; ++i) {
					locationsBuilder.add( xyComponent.getPoint(i) );
					// TODO interpolate time
					// determine sub-index
					// interpolate time
					
					double subIndex = 0.0; // TODO
					double xyT = stInterpolator.interpolate(subIndex)
						.getInterpolation().getY();
					
					timesBuilder.add(makeTime(xyT));
				}
			}
			
			locationsBuilder.add(res.getInterpolation());
			timesBuilder.add(time);
			startIndex = res.getStartIndex();
		}
		
		ImmutableList<ImmutablePoint> locations = locationsBuilder.build();
		ImmutableList<LocalDateTime> times = timesBuilder.build();
		
		return new SimpleTrajectory(new SpatialPath(locations), times);
	}
	
	private LocalDateTime makeTime(double t) {
		return trajectory.getBaseTime().plus(ofSeconds(t));
	}
	
}
