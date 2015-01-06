package world;

import java.util.Objects;

import world.pathfinder.SpatialPathfinder;

/**
 * A {@code WorldPerspective} describes the view of a worker unit on the world.
 * Worker units perceive the world differently than it is for itself. The reason
 * is the worker units' individual shape which is reduced to a point. To ensure
 * collision avoidance the world needs to be buffered by the extend of the
 * worker's shape. This enables an easier implementation of navigating through
 * the world.
 * 
 * @author Rico
 */
public class WorldPerspective {

	/**
	 * The individual view on the world for this perspective.
	 */
	private final World view;

	/**
	 * The spatial pathfinder of the view.
	 */
	private final SpatialPathfinder spatialPathfinder;

	/**
	 * Creates a perspective with the given view and spatial pathfinder of the
	 * world. The given World is not to be confused with the original world.
	 * 
	 * @param view
	 *            the view of this perspective.
	 * @param spatialPathfinder
	 *            of the view.
	 */
	public WorldPerspective(World view, SpatialPathfinder spatialPathfinder) {
		Objects.requireNonNull(view, "view");
		Objects.requireNonNull(spatialPathfinder, "spatialPathfinder");

		spatialPathfinder.setStaticObstacles(view.getStaticObstacles());

		this.view = view;
		this.spatialPathfinder = spatialPathfinder;
	}

	/**
	 * @return the individual view on the world for this perspective.
	 */
	public World getView() {
		return view;
	}

	/**
	 * @return the spatial pathfinder of the view of this perspective.
	 */
	public SpatialPathfinder getSpatialPathfinder() {
		return spatialPathfinder;
	}

}
