package world.util;

// TODO document
public interface Seeker<P, T> {
	
	public abstract SeekResult<P, T> seekFloor(P position);
	
	public abstract SeekResult<P, T> seekCeiling(P position);
	
	public static class SeekResult<P, T> {
		private final int index;
		private final P position;
		private final T object;
		
		public SeekResult(int index, P position, T object) {
			this.index = index;
			this.position = position;
			this.object = object;
		}

		public int getIndex() {
			return index;
		}

		public P getPosition() {
			return position;
		}

		public T get() {
			return object;
		}
		
	}

}
