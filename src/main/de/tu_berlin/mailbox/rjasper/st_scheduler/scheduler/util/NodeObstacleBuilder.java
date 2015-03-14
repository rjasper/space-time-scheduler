package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Schedule;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public class NodeObstacleBuilder {

	private Node node = null;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	private Map<Node, ImmutablePolygon> shapeLookUp = null;

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
		init();

		Collection<DynamicObstacle> nodeObstacles = new LinkedList<>();

		LocalDateTime from = startTime;
		LocalDateTime to = finishTime;

		// TODO make code fancier (a little repetitive right now)

		// original trajectories
		for (Node n : schedule.getNodes()) {
			if (n == node)
				continue;

			for (Trajectory t : n.getTrajectories(from, to))
				nodeObstacles.add(makeNodeObstacle(n, t));
		}

		// alternative trajectories added to schedule
		for (ScheduleAlternative a : schedule.getAlternatives()) {
			for (Node n : a.getNodes()) {
				if (n == node)
					continue;

				for (Trajectory t : a.getTrajectoryUpdates(n))
					nodeObstacles.add(makeNodeObstacle(n, t));
			}
		}

		// alternative trajectories of current alternative
		for (Node n : alternative.getNodes()) {
			if (n == node)
				continue;

			for (Trajectory t : alternative.getTrajectoryUpdates(n))
				nodeObstacles.add(makeNodeObstacle(n, t));
		}

		cleanUp();

		return nodeObstacles;
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

	private void init() {
		shapeLookUp = new HashMap<>();
	}

	private void cleanUp() {
		shapeLookUp = null;
	}

	private DynamicObstacle makeNodeObstacle(Node node, Trajectory trajectory) {
		double radius = this.node.getRadius();

		ImmutablePolygon shape = shapeLookUp.computeIfAbsent(node, w ->
			(ImmutablePolygon) immutable(w.getShape().buffer(radius)));

		return new DynamicObstacle(shape, trajectory);
	}

}
