package world;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import world.util.SpatialPathSegmentIterable.SpatialPathSegmentIterator;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryComposer {
	
	private TrajectoryFactory trajFact = new TrajectoryFactory();
	
	private double[] xSpatial = null;
	
	private double[] ySpatial = null;
	
	private double[] sSpatial = null;
	
	private double[] sArcTime = null;
	
	private double[] tArcTime = null;
	
	private SimpleTrajectory resultTrajectory = null;
	
	public boolean isReady() {
		return xSpatial != null
			&& ySpatial != null
			&& sSpatial != null
			&& sArcTime != null
			&& tArcTime != null;
	}
	
	private TrajectoryFactory getTrajectoryFactory() {
		return trajFact;
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.trajFact.setBaseTime(baseTime);
	}
	
	public void setSpatialPath(List<Point> spatialPath) {
		int nSpatial = spatialPath.size();

		double arcAcc = 0.0;
		SpatialPathSegmentIterator segments = new SpatialPathSegmentIterator(spatialPath);
		
		xSpatial = new double[nSpatial];
		ySpatial = new double[nSpatial];
		sSpatial = new double[nSpatial];
		
		Iterator<Point> it = spatialPath.iterator();
		
		for (int i = 0; i < nSpatial; ++i) {
			Point p = it.next();
			
			xSpatial[i] = p.getX();
			ySpatial[i] = p.getY();
			sSpatial[i] = arcAcc;
			
			// if not the last coordinate
			if (i < nSpatial-1)
				arcAcc += segments.next().getLength();
		}
	}
	
	public void setArcTimePath(List<Point> arcTimePath) {
		int nArcTime = arcTimePath.size();
		
		Iterator<Point> it = arcTimePath.iterator();
		
		sArcTime = new double[nArcTime];
		tArcTime = new double[nArcTime];
		
		for (int i = 0; i < nArcTime; ++i) {
			Point p = it.next();
			
			sArcTime[i] = p.getX();
			tArcTime[i] = p.getY();
		}
	}
	
	private double[] getXSpatial() {
		return xSpatial;
	}

	private double[] getYSpatial() {
		return ySpatial;
	}

	private double[] getSSpatial() {
		return sSpatial;
	}

	private double[] getSArcTime() {
		return sArcTime;
	}

	private double[] getTArcTime() {
		return tArcTime;
	}

	private int getSpatialPathSize() {
		return sSpatial.length;
	}

	private int getArcTimePathSize() {
		return sArcTime.length;
	}

	public SimpleTrajectory getResultTrajectory() {
		return resultTrajectory;
	}

	private void setResultTrajectory(SimpleTrajectory resultTrajectory) {
		this.resultTrajectory = resultTrajectory;
	}

	public void compose() {
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();
		
		// interpolate tXY
		double[] tSpatial = new double[nSpatial];
		
		interpolateSpatialTime(tSpatial);
		
		// interpolate xyST
		double[] xArcTime = new double[nArcTime];
		double[] yArcTime = new double[nArcTime];
		
		interpolateArcTimePoints(xArcTime, yArcTime);
		
		// merge
		int n = nSpatial + nArcTime - 2;
		double[] x = new double[n];
		double[] y = new double[n];
		double[] t = new double[n];
		
		n = mergePaths(tSpatial, xArcTime, yArcTime, x, y, t);
		
		// build trajectory
		TrajectoryFactory trajFact = getTrajectoryFactory();
		
		// TODO don't use factory
		SimpleTrajectory trajectory = trajFact.trajectory(x, y, t, n);
		
		setResultTrajectory(trajectory);
	}

	// @param tSpatial result vector
	private void interpolateSpatialTime(double[] tSpatial) {
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();
		double[] sSpatial = getSSpatial();
		double[] sArcTime = getSArcTime();
		double[] tArcTime = getTArcTime();

		for (int i = 0, j = 0; i < nSpatial; ++i) {
			while (j < nArcTime-1 && sSpatial[i] > sArcTime[j])
				++j;
			
			if (sSpatial[i] == sArcTime[j]) {
				tSpatial[i] = tArcTime[j];
			} else { // sArcTime[j-1] < sSpatial[i] < sArcTime[j]
				// linear interpolation of time
				
				double s = sSpatial[i];
				double s1 = sArcTime[j-1];
				double s2 = sArcTime[j];
				double t1 = tArcTime[j-1];
				double t2 = tArcTime[j];
				
				double alpha = (s - s1)/(s2 - s1);
				tSpatial[i] = t1 + alpha*(t2 - t1);
			}
		}
	}

	// @param xArcTime result vector
	// @param yArcTime result vector
	private void interpolateArcTimePoints(double[] xArcTime, double[] yArcTime) {
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();
		double[] sArcTime = getSArcTime();
		double[] sSpatial = getSSpatial();
		double[] xSpatial = getXSpatial();
		double[] ySpatial = getYSpatial();

		for (int i = 0, j = 0; i < nArcTime; ++i) {
			while (j < nSpatial-1 && sArcTime[i] > sSpatial[j])
				++j;
			
			if (sArcTime[i] == sSpatial[j]) {
				xArcTime[i] = xSpatial[j];
				yArcTime[i] = ySpatial[j];
			} else { // sSpatial[j-1] < sArcTime[i] < sSpatial[j]
				// linear interpolation of spatial coordinates
				
				double s = sArcTime[i];
				double s1 = sSpatial[j-1];
				double s2 = sSpatial[j];
				double x1 = xSpatial[j-1];
				double x2 = xSpatial[j];
				double y1 = ySpatial[j-1];
				double y2 = ySpatial[j];
				
				double alpha = (s - s1)/(s2 - s1);
				xArcTime[i] = x1 + alpha*(x2 - x1);
				yArcTime[i] = y1 + alpha*(y2 - y1);
			}
		}
	}

	// @param x result vector
	// @param y result vector
	// @param t result vector
	private int mergePaths(
		double[] tSpatial, double[] xArcTime, double[] yArcTime,
		double[] x, double[] y, double[] t)
	{
		int nSpatial = getSpatialPathSize();
		int nArcTime = getArcTimePathSize();
		double[] sSpatial = getSSpatial();
		double[] xSpatial = getXSpatial();
		double[] ySpatial = getYSpatial();
		double[] sArcTime = getSArcTime();
		double[] tArcTime = getTArcTime();
		
		int k = 0;
		
		// merge and sort by arc
		for (int i = 0, j = 0; i < nSpatial && j < nArcTime;) {
			if (sSpatial[i] == sArcTime[j]) {
				x[k] = xSpatial[i];
				y[k] = ySpatial[i];
				t[k] = tArcTime[j];
				
				++i; ++j;
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
