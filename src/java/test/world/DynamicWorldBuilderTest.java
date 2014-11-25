// TODO delete file

//package world;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.hasItem;
//import static org.junit.Assert.assertThat;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.junit.Test;
//
//import tasks.WorkerUnit;
//import tasks.WorkerUnitFactory;
//
//public class DynamicWorldBuilderTest {
//
//	private WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();
//	private LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
//	private TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
//
//	@Test
//	public void testBuild() {
//		WorkerUnit w1 = wFact.createWorkerUnit(10.0,  5.0);
//
//		LocalDateTime endTime = timeFact.second(80L);
//
//		wFact.addTask(w1, 10.0, 15.0,  30L,  70L);
//
//		DynamicWorldBuilder builder = new DynamicWorldBuilder();
//
//		builder.setEndTime(endTime);
//		builder.setWorkers(Collections.singleton(w1));
//
//		builder.build();
//
//		Collection<DynamicObstacle> obstacles = builder.getResultObstacles();
//
//		List<Trajectory> trajectories = obstacles.stream()
//			.map(DynamicObstacle::getTrajectory)
//			.collect(Collectors.toList());
//
//		assertThat(trajectories.size(), equalTo(3));
//
//		assertThat(trajectories, hasItem(trajFact.trajectory(
//			new double[] {10., 10.},
//			new double[] { 5., 15.},
//			new double[] { 0., 30.}
//		)));
//
//		assertThat(trajectories, hasItem(trajFact.trajectory(
//			new double[] {10., 10.},
//			new double[] {15., 15.},
//			new double[] {30., 70.}
//		)));
//
//		assertThat(trajectories, hasItem(trajFact.trajectory(
//			new double[] {10., 10.},
//			new double[] {15., 15.},
//			new double[] {70., 80.}
//		)));
//	}
//
//}
