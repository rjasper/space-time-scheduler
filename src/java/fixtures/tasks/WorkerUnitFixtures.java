package tasks;

import geom.factories.StaticJstFactories;

import java.time.Duration;
import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.WKTReader;

import static java.time.Month.*;
import static geom.factories.StaticJstFactories.*;

public final class WorkerUnitFixtures {
	
	public static WKTReader wkt() {
		return StaticJstFactories.wktReader();
	}
	
	public static WorkerUnit withThreeTasks() {
		WorkerUnit worker = new WorkerUnit(
			geomFactory().createPoint(new Coordinate(0., 0.)),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0)
		);
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(10., 10.)),
			LocalDateTime.of(2000, JANUARY, 1, 6, 0),
			Duration.ofHours(1L)
		));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(20., 10.)),
			LocalDateTime.of(2000, JANUARY, 1, 12, 0),
			Duration.ofHours(3L)
		));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(20., 20.)),
			LocalDateTime.of(2000, JANUARY, 1, 18, 0),
			Duration.ofHours(2L)
		));
		
		return worker;
	}

}
