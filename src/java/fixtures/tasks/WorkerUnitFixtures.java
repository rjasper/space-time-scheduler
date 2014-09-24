package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import jts.geom.factories.StaticJtsFactories;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import static java.time.Month.*;
import static jts.geom.factories.StaticJtsFactories.*;

public final class WorkerUnitFixtures {
	
	public static WKTReader wkt() {
		return StaticJtsFactories.wktReader();
	}
	
	public static Polygon defaultShape() {
		try {
			return (Polygon) wkt().read("POLYGON ((-5 5, 5 5, 5 -5, -5 -5, -5 5))");
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static WorkerUnit basicWorker(Point location) {
		return new WorkerUnit(defaultShape(), 1., location,
			LocalDateTime.of(2000, JANUARY, 1, 0, 0)
		);
	}
	
	public static WorkerUnit withTwoTasks1() {
		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(-3600., 0.)));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(-3600., 0.)),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0),
			Duration.ofHours(3L)
		));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(0., 3600.)),
			LocalDateTime.of(2000, JANUARY, 1, 7, 0),
			Duration.ofHours(3L)
		));
		
		return worker;
	}
	
	public static WorkerUnit withTwoTasks2() {
		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(0., -3600.)));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(0., -3600.)),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0),
			Duration.ofHours(2L)
		));
		
		worker.addTask(new Task(
			geomFactory().createPoint(new Coordinate(3600., 0.)),
			LocalDateTime.of(2000, JANUARY, 1, 8, 0),
			Duration.ofHours(2L)
		));
		
		return worker;
	}
	
	public static WorkerUnit withThreeTasks() {
		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(0., 0.)));
		
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
