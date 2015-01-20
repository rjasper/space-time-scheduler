package world;

/**
 * Provides static methods that operate on or return paths.
 * 
 * @author Rico
 */
public final class Paths {
	
	private Paths() {}
	
	/**
	 * An empty {@code Path} instance.
	 */
	private static final Path EMPTY_PATH = new Path();
	
	/**
	 * An empty {@code SpatialPath} instance.
	 */
	private static final SpatialPath EMPTY_SPATIAL_PATH = new SpatialPath();
	
	/**
	 * An empty {@code ArcTimePath} instance.
	 */
	private static final ArcTimePath EMPTY_ARC_TIME_PATH = new ArcTimePath();
	
	/**
	 * @return an empty {@code Path} instance.
	 */
	public static Path emptyPath() {
		return EMPTY_PATH;
	}
	
	/**
	 * @return an empty {@code SpatialPath} instance.
	 */
	public static SpatialPath emptySpatialPath() {
		return EMPTY_SPATIAL_PATH;
	}

	/**
	 * @return an empty {@code ArcTimePath} instance.
	 */
	public static ArcTimePath emptyArcTimePath() {
		return EMPTY_ARC_TIME_PATH;
	}

}
