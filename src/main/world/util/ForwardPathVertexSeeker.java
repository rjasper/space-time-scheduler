// TODO remove
//package world.util;
//
//import java.util.Iterator;
//import java.util.function.Function;
//
//import world.PointPath;
//
//public class ForwardPathVertexSeeker<
//	V extends PointPath.Vertex,
//	P extends PointPath<V, ?>>
//	
//	extends AbstractVertexSeeker<V, P>
//{
//	
//	private final Iterator<V> iterator;
//	
//	private double last;
//	
//	private V curr;
//	
//	private V next;
//
//	public ForwardPathVertexSeeker(P path, Function<V, Double> positionMapper) {
//		super(path, positionMapper);
//		
//		this.iterator = path.vertexIterator();
//		
//		if (!path.isEmpty()) {
//			this.curr = iterator.next();
//			this.next = iterator.next();
//			this.last = position(curr);
//		}
//	}
//
//	@Override
//	public V seekFloor(double position) {
//		return curr;
//	}
//	
//	@Override
//	public V seekCeiling(double position) {
//		seek(position);
//		
//		if (position == position(curr))
//			return curr;
//		else
//			return next;
//	}
//	
//	private void seek(double position) {
//		if (getPath().isEmpty())
//			throw new IllegalArgumentException("path is empty");
//		if (!Double.isFinite(position))
//			throw new IllegalArgumentException("position is not finite");
//		if (position < last)
//			throw new IllegalArgumentException("cannot go backwards");
//		
//		// position >= last
//		
//		if (position == last)
//			return;
//		
//		// position > last
//		
//		while (next != null && position > position(next)) {
//			curr = next;
//			next = iterator.hasNext() ? iterator.next() : null;
//		}
//		
//		// equivalent to curr.isLast() && position > position(curr) 
//		if (curr.isLast() && position != position(curr))
//			throw new IllegalArgumentException("position too big");
//		
//		// position >= position(curr), position <= position(next)
//		
//		last = position;
//	}
//
//}
