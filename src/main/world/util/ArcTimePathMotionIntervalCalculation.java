package world.util;

import java.util.Objects;

import scheduler.util.SimpleIntervalSet;
import world.ArcTimePath;

public class ArcTimePathMotionIntervalCalculation {
	
	private final ArcTimePath arcTimePath;

	public ArcTimePathMotionIntervalCalculation(ArcTimePath arcTimePath) {
		this.arcTimePath = Objects.requireNonNull(arcTimePath, "arcTimePath");
	}
	
	public SimpleIntervalSet<Double> calculate(double from, double to) {
		// XXX last edition
		// TODO implement
		
		return null;
	}

}
