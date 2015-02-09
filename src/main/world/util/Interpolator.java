package world.util;

import java.util.Objects;

// TODO document
public interface Interpolator<P, T> {

	public abstract InterpolationResult<T> interpolate(P position);
	
	public static class InterpolationResult<T> {
		private final int index;
		private final boolean onSpot;
		private final T interpolation;
		
		public static <T> InterpolationResult<T> onSpot(int index, T interpolation) {
			return new InterpolationResult<T>(index, interpolation, true);
		}
		
		public static <T> InterpolationResult<T> inbetween(int startIndex, T interpolation) {
			return new InterpolationResult<T>(startIndex, interpolation, false);
		}
		
		private InterpolationResult(int index, T interpolation, boolean onSpot) {
			Objects.requireNonNull(interpolation, "interpolation");
			
			if (index < 0)
				throw new IllegalArgumentException("invalid index");
			
			this.index = index;
			this.interpolation = interpolation;
			this.onSpot = onSpot;
		}
		
		public boolean isOnSpot() {
			return onSpot;
		}
		
		public boolean isInbetween() {
			return !onSpot;
		}
		
		/**
		 * @return the startIndex
		 */
		public int getStartIndex() {
			return index;
		}
		
		/**
		 * @return the finishIndex
		 */
		public int getFinishIndex() {
			return onSpot ? index : index + 1;
		}
		
		/**
		 * @return the interpolation
		 */
		public T getInterpolation() {
			return interpolation;
		}
		
	}

}