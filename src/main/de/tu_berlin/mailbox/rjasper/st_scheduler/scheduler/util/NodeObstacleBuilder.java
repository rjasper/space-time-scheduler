package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.AsDynamicObstacles.asDynamicObstacles;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.collect.JoinedCollection;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Schedule;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

public class NodeObstacleBuilder {

	private Node node = null;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	public void setNode(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = Objects.requireNonNull(startTime, "startTime");
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = Objects.requireNonNull(finishTime, "finishTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public Collection<DynamicObstacle> build() {
		checkParameters();

		List<Collection<DynamicObstacle>> obstacleCollectionList = new LinkedList<>();

		for (Node n : schedule.getNodes()) {
			if (n == node)
				continue;

			ImmutablePolygon shape = shapeOf(n);

			// schedule trajectories
			obstacleCollectionList.add(
				asDynamicObstacles(shape, n.getTrajectories(startTime, finishTime)));

			// alternative trajectories
			schedule.getAlternatives().stream()
				.filter(sa -> sa.updatesNode(n))
				.map(sa -> sa.getTrajectoryUpdates(n))
				.map(ts -> asDynamicObstacles(shape, ts))
				.forEach(obstacleCollectionList::add);

			// alternative trajectories of current alternative
			if (alternative.updatesNode(n))
				obstacleCollectionList.add(
					asDynamicObstacles(shape, alternative.getTrajectoryUpdates(n)));
		}

		return JoinedCollection.of(obstacleCollectionList);
	}

	private void checkParameters() {
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");

		if (!startTime.isBefore(finishTime))
			throw new IllegalStateException("startTime is not before finishTime");
	}

	private ImmutablePolygon shapeOf(Node node) {
		double radius = this.node.getRadius();

		return immutable( node.getShape().buffer(radius) );
	}

}
