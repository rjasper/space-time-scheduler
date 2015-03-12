package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;

/**
 * A {@code WorldPerspective} describes the view of a node unit on the world.
 * Node units perceive the world differently than it is for itself. The reason
 * is the node units' individual shape which is reduced to a point. To ensure
 * collision avoidance the world needs to be buffered by the extend of the
 * node's shape. This enables an easier implementation of navigating through
 * the world.
 * 
 * @author Rico Jasper
 */
public class WorldPerspective {

	/**
	 * The individual view on the world for this perspective.
	 */
	private final World view;

	/**
	 * The spatial pathfinder of the view.
	 */
	private final AbstractSpatialPathfinder spatialPathfinder;

	/**
	 * Creates a perspective with the given view and spatial pathfinder of the
	 * world. The given World is not to be confused with the original world.
	 * 
	 * @param view
	 *            the view of this perspective.
	 * @param spatialPathfinder
	 *            of the view.
	 */
	public WorldPerspective(World view, AbstractSpatialPathfinder spatialPathfinder) {
		this.view = Objects.requireNonNull(view, "view");;
		this.spatialPathfinder = Objects.requireNonNull(spatialPathfinder, "spatialPathfinder");;

		spatialPathfinder.setStaticObstacles(view.getStaticObstacles());

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
	public AbstractSpatialPathfinder getSpatialPathfinder() {
		return spatialPathfinder;
	}

}
