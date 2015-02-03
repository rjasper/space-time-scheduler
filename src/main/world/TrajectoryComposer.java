package world;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.DurationConv;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

/**
 * Composes a {@link SimpleTrajectory} from a provided spatial path and arc time
 * path component.
 * 
 * @author Rico
 */
public class TrajectoryComposer {

	/**
	 * The base time.
	 */
	private LocalDateTime baseTime = null;

	/**
	 * The spatial x-ordinates.
	 */
	private double[] xSpatial = null;

	/**
	 * The spatial y-ordinates.
	 */
	private double[] ySpatial = null;

	/**
	 * The spatial s-ordinates (arc).
	 */
	private double[] sSpatial = null;

	/**
	 * The arc-time s-ordindates (arc).
	 */
	private double[] sArcTime = null;

	/**
	 * The arc-time t-ordinates.
	 */
	private double[] tArcTime = null;

	/**
	 * The composed trajectory.
	 */
	private SimpleTrajectory resultTrajectory = null;

	/**
	 * Sets the base time of the arc-time component.
	 * 
	 * @param baseTime
	 * @throws NullPointerException
	 *             if {@code baseTime} is {@code null}.
	 */
	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = Objects.requireNonNull(baseTime, "baseTime");
	}

	/**
	 * Sets the spatial path component (x-y).
	 * 
	 * @param spatialPathComponent
	 */
	public void setSpatialPathComponent(SpatialPath spatialPathComponent) {
		int nSpatial = spatialPathComponent.size();

		xSpatial = new double[nSpatial];
		ySpatial = new double[nSpatial];
		sSpatial = new double[nSpatial];

		Iterator<SpatialPath.Vertex> it = spatialPathComponent.vertexIterator();

		for (int i = 0; i < nSpatial; ++i) {
			SpatialPath.Vertex v = it.next();

			xSpatial[i] = v.getX();
			ySpatial[i] = v.getY();
			sSpatial[i] = v.getArc();
		}
	}

	/**
	 * Sets the arc time component (s-t).
	 * 
	 * @param arcTimePathComponent
	 */
	public void setArcTimePathComponent(ArcTimePath arcTimePathComponent) {
		int nArcTime = arcTimePathComponent.size();

		Iterator<ImmutablePoint> it = arcTimePathComponent.iterator();

		sArcTime = new double[nArcTime];
		tArcTime = new double[nArcTime];

		for (int i = 0; i < nArcTime; ++i) {
			Point p = it.next();

			sArcTime[i] = p.getX();
			tArcTime[i] = p.getY();
		}
	}

	/**
	 * @return the length of the spatial path component.
	 */
	private int getSpatialPathSize() {
		// equivalent to xSpatial.length or ySpatial.legnth
		return sSpatial.length;
	}

	/**
	 * @return the length of the arc-time path component
	 */
	private int getArcTimePathSize() {
		// equivalent to tSpatial.length
		return sArcTime.length;
	}

	/**
	 * @return the composed trajectory.
	 */
	public SimpleTrajectory getResultTrajectory() {
		return resultTrajectory;
	}

	/**
	 * Sets the composed trajectory.
	 * 
	 * @param resultTrajectory
	 */
	private void setResultTrajectory(SimpleTrajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	/**
	 * Checks if parameters are validly set.
	 * 
	 * @throws NullPointerException
	 *             if any parameter is {@code null}.
	 * @throws IllegalStateException
	 *             if one component is empty while the other is not.
	 */
	private void checkParameters() {
		Objects.requireNonNull(baseTime, "baseTime");
		Objects.requireNonNull(xSpatial, "xSpatial");
		Objects.requireNonNull(sArcTime, "sArcTime");
		
		if ((getSpatialPathSize() == 0) ^ (getArcTimePathSize() == 0))
			throw new IllegalStateException("inconsistent array lengths");
	}

	/**
	 * Composes the trajectory from the provided spatial and arc-time path
	 * components.
	 */
	public void compose() {
		checkParameters();
		
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();

		// TODO tXY interpolation only works with bijective arc-time mapping
		// interpolate tXY
		double[] tSpatial = new double[nSpatial];

		interpolateSpatialTime(tSpatial);

		// interpolate xyST
		double[] xArcTime = new double[nArcTime];
		double[] yArcTime = new double[nArcTime];

		interpolateArcTimePoints(xArcTime, yArcTime);

		// merge
		int nMax = nSpatial + nArcTime - 2; // maximum number of vertices
		double[] x = new double[nMax];
		double[] y = new double[nMax];
		double[] t = new double[nMax];

		// n = actual number of vertices
		int n = mergePaths(tSpatial, xArcTime, yArcTime, x, y, t);

		// build trajectory

		// build path
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();
		for (int i = 0; i < n; ++i)
			builder.add(immutablePoint(x[i], y[i]));
		SpatialPath path = new SpatialPath(builder.build());
		
		Duration minDuration = Duration.between(baseTime, LocalDateTime.MIN);
		Duration maxDuration = Duration.between(baseTime, LocalDateTime.MAX);
		
		// build times
		ImmutableList<LocalDateTime> times = Arrays.stream(t, 0, n)
			.mapToObj(DurationConv::ofSeconds)
			.map(d -> {
				// double inaccuracy might lead to overflow
				if (d.compareTo(minDuration) < 0)
					return LocalDateTime.MIN;
				if (d.compareTo(maxDuration) > 0)
					return LocalDateTime.MAX;
				
				return baseTime.plus(d);
			})
			.collect(toImmutableList());
		
		setResultTrajectory(new SimpleTrajectory(path, times));
	}

	/**
	 * Interpolates the time values of the spatial path vertices. The result is
	 * stored in the provided array.
	 * 
	 * @param tSpatial
	 *            the interpolated time values.
	 */
	private void interpolateSpatialTime(double[] tSpatial) {
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();

		for (int i = 0, j = 0; i < nSpatial; ++i) {
			while (j < nArcTime - 1 && sSpatial[i] > sArcTime[j])
				++j;

			if (sSpatial[i] == sArcTime[j]) {
				tSpatial[i] = tArcTime[j];
			} else { // sArcTime[j-1] < sSpatial[i] < sArcTime[j]
				// linear interpolation of time

				double s = sSpatial[i];
				double s1 = sArcTime[j - 1];
				double s2 = sArcTime[j];
				double t1 = tArcTime[j - 1];
				double t2 = tArcTime[j];

				double alpha = (s - s1) / (s2 - s1);
				tSpatial[i] = t1 + alpha * (t2 - t1);
			}
		}
	}

	/**
	 * Interpolates the spatial values of the arc-time vertices. The result is
	 * stored in the provided arrays.
	 * 
	 * @param xArcTime
	 *            the interpolated x-values.
	 * @param yArcTime
	 *            the interpolated y-values.
	 */
	private void interpolateArcTimePoints(double[] xArcTime, double[] yArcTime) {
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();

		for (int i = 0, j = 0; i < nArcTime; ++i) {
			while (j < nSpatial - 1 && sArcTime[i] > sSpatial[j])
				++j;

			if (sArcTime[i] == sSpatial[j]) {
				xArcTime[i] = xSpatial[j];
				yArcTime[i] = ySpatial[j];
			} else { // sSpatial[j-1] < sArcTime[i] < sSpatial[j]
				// linear interpolation of spatial coordinates

				double s = sArcTime[i];
				double s1 = sSpatial[j - 1];
				double s2 = sSpatial[j];
				double x1 = xSpatial[j - 1];
				double x2 = xSpatial[j];
				double y1 = ySpatial[j - 1];
				double y2 = ySpatial[j];

				double alpha = (s - s1) / (s2 - s1);
				xArcTime[i] = x1 + alpha * (x2 - x1);
				yArcTime[i] = y1 + alpha * (y2 - y1);
			}
		}
	}

	/**
	 * Merges the spatial and arc-time path components to a 3-dimensional vertex
	 * vector.
	 * 
	 * @param tSpatial
	 *            spatial time-values.
	 * @param xArcTime
	 *            arc-time x-values.
	 * @param yArcTime
	 *            arc-time y-values.
	 * @param x
	 *            the merged x-values.
	 * @param y
	 *            the merged y-values.
	 * @param t
	 *            the merged t-values.
	 * @return number of 3-dimensional vertices.
	 */
	private int mergePaths(
		double[] tSpatial,
		double[] xArcTime,
		double[] yArcTime,
		double[] x,
		double[] y,
		double[] t)
	{
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();

		int k = 0;

		// merge and sort by arc
		for (int i = 0, j = 0; i < nSpatial && j < nArcTime;) {
			if (sSpatial[i] == sArcTime[j]) {
				x[k] = xSpatial[i];
				y[k] = ySpatial[i];
				t[k] = tArcTime[j];

				++i;
				++j;
			} else if (sSpatial[i] < sArcTime[j]) {
				x[k] = xSpatial[i];
				y[k] = ySpatial[i];
				t[k] = tSpatial[i];

				++i;
			} else { // sSpatial[i] > sArcTime[j]
				x[k] = xArcTime[j];
				y[k] = yArcTime[j];
				t[k] = tArcTime[j];

				++j;
			}

			++k;
		}

		return k;
	}

}
