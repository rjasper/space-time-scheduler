package pickers;

import static jts.geom.factories.StaticJtsFactories.geomFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.GeometryCollectionMapper;
import com.vividsolutions.jts.util.GeometricShapeFactory;

// TODO elaborate

/**
 * An iterator that extracts points from a Geometry.
 *
 * @author Rico Jasper
 */
public class LocationIterator implements Iterator<Point> {

	/**
	 * The space to pick points from.
	 */
	private final Geometry space;

	/**
	 * The maximum number of picks.
	 */
	private final int maxPicks;

	/**
	 * The current number of picks.
	 */
	private int picks = 0;

	/**
	 * A queue of envelopes to mask a sub area of the {@link #space}.
	 */
	private Queue<Envelope> queue = new PriorityQueue<>(envelopePriorityComparator);

	/**
	 * A queue of point and envelop pairs which were already used.
	 */
	private Queue<PointEnvelopePair> recycling = new PriorityQueue<>();

	/**
	 * The next point to be returned by {@link #next()}.
	 */
	private Point nextPoint;

	private GeometricShapeFactory shapeFactory = new GeometricShapeFactory(geomFactory());

	/**
	 * Compares to Envelops by their area size.
	 */
	private static final Comparator<Envelope> envelopePriorityComparator = new Comparator<Envelope>() {
		@Override
		public int compare(Envelope o1, Envelope o2) {
			double a1 = o1.getArea();
			double a2 = o2.getArea();

			return Double.compare(a2, a1);
		}
	};

	private static final int MAX_TRASH = 30;
	private static final int TRASH_AFTER_DUMP = 20;

	/**
	 * Constructs a new LocationIterator. Picks points from the given space
	 * but not more than {@code maxPicks} times.
	 *
	 * @param space
	 * @param maxPicks
	 * @throws NullPointerException if space is null
	 * @throws IllegalArgumentException if any of the following is true:
	 * <ul>
	 * <li>{@code space} is non-simple or invalid</li>
	 * <li>{@code maxPicks} is negative</li>
	 * </ul>
	 */
	public LocationIterator(Geometry space, int maxPicks) {
		Objects.requireNonNull(space, "space");

		if (!space.isSimple() || !space.isValid())
			throw new IllegalArgumentException("illegal space");
		if (maxPicks < 0)
			throw new IllegalArgumentException("maxPicks is negative");

		this.space = space;
		this.maxPicks = maxPicks;

		init();
	}

	/**
	 * Initializes this object.
	 */
	private void init() {
		initShapeFactory();
		initQueue();
		initNextPoint();
	}

	/**
	 * Initializes the envelope queue.
	 */
	private void initQueue() {
		Queue<Envelope> queue = getQueue();
		Geometry space = getSpace();
		Envelope envelope = space.getEnvelopeInternal();

		queue.add(envelope);
	}

	/**
	 * Initializes the {@link #shapeFactory}.
	 */
	private void initShapeFactory() {
		GeometricShapeFactory shapeFactory = getShapeFactory();

		shapeFactory.setNumPoints(4);
	}

	/**
	 * Calculates the first point to pick.
	 */
	private void initNextPoint() {
		Point point = calcNextPoint();

		setNextPoint(point);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		Point point = getNextPoint();

		return point != null;
	}

	/**
	 * @return the space to pick points from.
	 */
	private Geometry getSpace() {
		return space;
	}

	/**
	 * @return the maximum number of picks.
	 */
	public int getMaxPicks() {
		return maxPicks;
	}

	/**
	 * @return the current number of picks.
	 */
	public int getPicks() {
		return picks;
	}

	/**
	 * Sets the current number of picks.
	 *
	 * @param picks
	 */
	private void setPicks(int picks) {
		this.picks = picks;
	}

	/**
	 * @return the next point to be returned by {@link #next()}.
	 */
	private Point getNextPoint() {
		return nextPoint;
	}

	/**
	 * Sets the next point to be returned by {@link #next()}.
	 *
	 * @param point
	 */
	private void setNextPoint(Point point) {
		this.nextPoint = point;
	}

	/**
	 * @return the queue of envelopes.
	 */
	private Queue<Envelope> getQueue() {
		return queue;
	}

	/**
	 * @return the queue of point and envelop pairs which were already used.
	 */
	private Queue<PointEnvelopePair> getRecycling() {
		return this.recycling;
	}

	/**
	 * @return the shape factory.
	 */
	private GeometricShapeFactory getShapeFactory() {
		return shapeFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Point next() {
		if (!hasNext())
			throw new NoSuchElementException("maximum picks reached");

		int pick = getPicks() + 1;
		int maxPicks = getMaxPicks();

		Point point = getNextPoint();
		Point next = pick < maxPicks ? calcNextPoint() : null;

		setPicks(pick);
		setNextPoint(next);

		return point;
	}

	/**
	 * Calculates the next point to pick.
	 *
	 * @return the next point.
	 */
	private Point calcNextPoint() {
		Queue<PointEnvelopePair> recycling = getRecycling();

		Envelope envelope;
		Geometry subSpace;

		// get a non-empty subspace
		do {
			envelope = nextEnvelope();

			if (envelope == null)
				return null;

			subSpace = calcSubSpace(envelope);
		} while (subSpace.isEmpty());

		Point point = subSpace.getInteriorPoint();

		// save envelope for later
		recycling.add(new PointEnvelopePair(point, subSpace.getEnvelopeInternal()));

		// don't let recyling grow to much
		if (recycling.size() > MAX_TRASH)
			dumpTrash();

		return point;
	}

	/**
	 * Reduce the trash in recycling by dumping the tail. After calling there
	 * will be at most {@link #TRASH_AFTER_DUMP} elements.
	 */
	private void dumpTrash() {
		Iterator<PointEnvelopePair> it = getRecycling().iterator();

		// get into position
		for (int i = 0; i < TRASH_AFTER_DUMP; ++i)
			it.next();

		// remove trash
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	/**
	 * @return the next envelope to mask the space.
	 */
	private Envelope nextEnvelope() {
		Queue<Envelope> queue = getQueue();

		// if there is no envelope in the queue then recycle one
		if (queue.isEmpty()) {
			recycle();

			if (queue.isEmpty())
				return null;
		}

		Envelope envelope = queue.poll();

		return envelope;
	}

	/**
	 * Recycles one envelope by dividing into sub areas. The point extracted
	 * using this envelope mask will determine the split location.
	 */
	private void recycle() {
		Queue<Envelope> queue = getQueue();
		Queue<PointEnvelopePair> recycling = getRecycling();

		if (recycling.isEmpty())
			return;

		PointEnvelopePair pair = recycling.poll();
		Envelope envelope = pair.getEnvelope();
		Point point = pair.getPoint();

		Envelope[] sections = devideEnvelope(envelope, point);

		for (Envelope s : sections)
			queue.add(s);
	}

	/**
	 * Creates four new envelops by splitting the old one vertically and
	 * horizontally where the given point lies at the crossing of both cuts.
	 *
	 * @param envelope the envelope to be devided
	 * @param point
	 *
	 * @return an array of four new envelopes
	 */
	private Envelope[] devideEnvelope(Envelope envelope, Point point) {
		double x1 = envelope.getMinX();
		double x2 = point.getX();
		double x3 = envelope.getMaxX();
		double y1 = envelope.getMinY();
		double y2 = point.getY();
		double y3 = envelope.getMaxY();

		return new Envelope[] {
			new Envelope(x1, x2, y1, y2), new Envelope(x2, x3, y1, y2),
			new Envelope(x1, x2, y2, y3), new Envelope(x2, x3, y2, y3)
		};
	}

	/**
	 * Calculates a sub space from {@link #space} by using the given envelope
	 * as mask.
	 *
	 * @param envelope
	 * @return the sub space.
	 */
	private Geometry calcSubSpace(Envelope envelope) {
		GeometricShapeFactory shapeFactory = getShapeFactory();
		Geometry space = getSpace();

		shapeFactory.setEnvelope(envelope);

		Geometry mask = shapeFactory.createRectangle();
		Geometry subSpace = mask.intersection(space);

		// only use geometries of the highest dimension
		if (subSpace instanceof GeometryCollection) {
			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

			int dim = subSpace.getDimension();
			Geometry empty = geomBuilder.geometryCollection();

			subSpace = GeometryCollectionMapper.map(
				(GeometryCollection) subSpace,
				g -> g.getDimension() == dim ? g : empty);
		}

		return subSpace;
	}

	/**
	 * Helper class to store a pair of a {@link Point} and a {@link Envelope}.
	 * The point is the one extracted from the sub space masked by the envelope.
	 */
	private static class PointEnvelopePair implements Comparable<PointEnvelopePair> {
		/**
		 * The point extracted from the sub space.
		 */
		private Point point;
		/**
		 * The envelope used to mask the sub space.
		 */
		private Envelope envelope;

		/**
		 * Constructs a pair using the given point and envelope.
		 *
		 * @param point
		 * @param envelope
		 */
		public PointEnvelopePair(Point point, Envelope envelope) {
			this.point = point;
			this.envelope = envelope;
		}

		/**
		 * @return the point extracted from the sub space.
		 */
		public Point getPoint() {
			return point;
		}

		/**
		 * @return the envelope used to mask the sub space.
		 */
		public Envelope getEnvelope() {
			return envelope;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[point=" + point + ", envelope=" + envelope + "]";
		}

		@Override
		public int compareTo(PointEnvelopePair o) {
			return envelopePriorityComparator.compare(envelope, o.envelope);
		}

	}

}
