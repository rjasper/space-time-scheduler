package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.BOUNDARY;
import static com.vividsolutions.jts.geom.Location.INTERIOR;

import java.util.Collection;
import java.util.Objects;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public class StaticCollisionDetector {

	public static boolean collides(Trajectory trajectory, Collection<StaticObstacle> obstacles) {
		return new StaticCollisionDetector(trajectory, obstacles)
			.collides();
	}

	private final Geometry trajectoryTrace;

	private final Collection<StaticObstacle> obstacles;

	public StaticCollisionDetector(Trajectory trajectory, Collection<StaticObstacle> obstacles) {
		this.trajectoryTrace = Objects.requireNonNull(trajectory, "trajectory")
			.trace();
		this.obstacles = CollectionsRequire.requireNonNull(obstacles, "obstacles");

		if (trajectory.isEmpty())
			throw new IllegalArgumentException("empty trajectory");
	}

	public boolean collides() {
		return obstacles.stream()
			.map(StaticObstacle::getShape)
			.anyMatch(this::checkCollision);
	}

	private boolean checkCollision(Geometry obstacleShape) {
		IntersectionMatrix mat = obstacleShape.relate(trajectoryTrace);

		return isTrue( mat.get(INTERIOR, INTERIOR) )
			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
	}

}
