package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import static java.util.Objects.*;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

// TODO test
public class AsDynamicObstacles extends AbstractCollection<DynamicObstacle> {

	public static Collection<DynamicObstacle> asDynamicObstacles(
		ImmutablePolygon shape, Collection<Trajectory> trajectories)
	{
		return new AsDynamicObstacles(shape, trajectories);
	}

	private final ImmutablePolygon shape;

	private final Collection<Trajectory> trajectories;

	public AsDynamicObstacles(ImmutablePolygon shape, Collection<Trajectory> trajectories) {
		this.shape = requireNonNull(shape, "node");
		this.trajectories = requireNonNull(trajectories, "trajectories");
	}

	@Override
	public Iterator<DynamicObstacle> iterator() {
		return new Itr(shape, trajectories.iterator());
	}

	@Override
	public int size() {
		return trajectories.size();
	}

	private static class Itr implements Iterator<DynamicObstacle> {

		private final ImmutablePolygon shape;

		private final Iterator<Trajectory> it;

		public Itr(ImmutablePolygon shape, Iterator<Trajectory> it) {
			this.shape = shape;
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public DynamicObstacle next() {
			return new DynamicObstacle(shape, it.next());
		}

	}

}
