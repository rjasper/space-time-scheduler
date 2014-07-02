package world;

import static geom.factories.StaticJstFactories.*;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class LocationPicker {
	
	private final Geometry space;
	
	private final int maxPicks;
	
	private int picks = 0;
	
	private Queue<Envelope> queue = new PriorityQueue<>(envelopePriorityComparator);
	
	private Queue<PointEnvelopePair> recycling = new PriorityQueue<>();
	
	private Point nextPoint;

	private GeometricShapeFactory shapeFactory = new GeometricShapeFactory(geomFactory());
	
	private static Comparator<Envelope> envelopePriorityComparator = new Comparator<Envelope>() {
		@Override
		public int compare(Envelope o1, Envelope o2) {
			double a1 = o1.getArea();
			double a2 = o2.getArea();
			
			return Double.compare(a2, a1);
		}
	};

	public LocationPicker(Geometry space, int maxPicks) {
		this.space = space;
		this.maxPicks = maxPicks;
		
		_init();
	}
	
	private void _init() {
		_initShapeFactory();
		_initQueue();
		_initNextPoint();
	}
	
	private void _initQueue() {
		Queue<Envelope> queue = _getQueue();
		Geometry space = _getSpace();
		Envelope envelope = space.getEnvelopeInternal();
		
		queue.add(envelope);
	}

	private void _initShapeFactory() {
		GeometricShapeFactory shapeFactory = _getShapeFactory();
		
		shapeFactory.setNumPoints(4);
	}
	
	private void _initNextPoint() {
		Point point = calcNextPoint();
		
		_setNextPoint(point);
	}

	public boolean isDone() {
		return _isDone();
	}
	
	private boolean _isDone() {
		Point point = _getNextPoint();
		
		return point == null;
	}
	
	private Geometry _getSpace() {
		return this.space;
	}
	
	public int getMaxPicks() {
		return _getMaxPicks();
	}
	
	private int _getMaxPicks() {
		return this.maxPicks;
	}
	
	public int getPicks() {
		return _getPicks();
	}
	
	private int _getPicks() {
		return this.picks;
	}
	
	private void _setPicks(int picks) {
		this.picks = picks;
	}
	
	private Point _getNextPoint() {
		return this.nextPoint;
	}
	
	private void _setNextPoint(Point point) {
		this.nextPoint = point;
	}
	
	private Queue<Envelope> _getQueue() {
		return this.queue;
	}
	
	private Queue<PointEnvelopePair> _getRecycling() {
		return this.recycling;
	}
	
	private GeometricShapeFactory _getShapeFactory() {
		return shapeFactory;
	}
	
	public Point next() {
		if (_isDone())
			throw new IllegalStateException("maximum picks reached");
		
		int pick = _getPicks() + 1;
		int maxPicks = _getMaxPicks();
		Point point = _getNextPoint();
		
		Point next = pick < maxPicks ? calcNextPoint() : null;
		
		_setPicks(pick);
		_setNextPoint(next);
		
		return point;
	}
	
	private Point calcNextPoint() {
		Queue<PointEnvelopePair> recycling = _getRecycling();
		
		Envelope envelope;
		Geometry subSpace;
		
		do {
			envelope = nextEnvelope();
			
			if (envelope == null)
				return null;
			
			subSpace = calcSubSpace(envelope);
		} while (subSpace.isEmpty());
		
		Point point = subSpace.getInteriorPoint();
		recycling.add(new PointEnvelopePair(point, envelope));
		
		// TODO: what about a dump function to limit the trash?
		
		return point;
	}
	
	private Envelope nextEnvelope() {
		Queue<Envelope> queue = _getQueue();
		
		if (queue.isEmpty()) {
			recycle();
			
			if (queue.isEmpty())
				return null;
		}
		
		Envelope envelope = queue.poll();
		
		return envelope;
	}
	
	private void recycle() {
		Queue<Envelope> queue = _getQueue();
		Queue<PointEnvelopePair> recycling = _getRecycling();
		
		if (recycling.isEmpty())
			return;
		
		PointEnvelopePair pair = recycling.poll();
		Envelope envelope = pair.getEnvelope();
		Point point = pair.getPoint();
		
		Envelope[] sections = devideEnvelope(envelope, point);
		
		for (Envelope s : sections)
			queue.add(s);
	}

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

	private Geometry calcSubSpace(Envelope envelope) {
		GeometricShapeFactory shapeFactory = _getShapeFactory();
		Geometry space = _getSpace();
		
		shapeFactory.setEnvelope(envelope);
		
		Geometry mask = shapeFactory.createRectangle();
		Geometry subSpace = mask.intersection(space);
		
		return subSpace;
	}
	
	private static class PointEnvelopePair implements Comparable<PointEnvelopePair> {
		private Point point;
		private Envelope envelope;
		
		public PointEnvelopePair(Point point, Envelope envelope) {
			this.point = point;
			this.envelope = envelope;
		}

		public Point getPoint() {
			return point;
		}

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
