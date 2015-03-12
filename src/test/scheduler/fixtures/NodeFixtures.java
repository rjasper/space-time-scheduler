package scheduler.fixtures;

import static util.UUIDFactory.*;
import scheduler.Node;
import scheduler.factories.NodeFactory;

public final class NodeFixtures {

	private static NodeFactory wFact = NodeFactory.getInstance();

	public static Node withTwoTasks1() {
		Node node = wFact.createNode("unnamed", 0., 0.);

		wFact.addTaskWithDuration(node, uuid("task1"), -3600.,    0., 2L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(node, uuid("task2"),     0., 3600., 9L * 3600L, 3L * 3600L);

		return node;
	}

	public static Node withTwoTasks2() {
		Node node = wFact.createNode("unnamed", 0., 0.);

		wFact.addTaskWithDuration(node, uuid("task1"),    0., -3600., 2L * 3600L, 2L * 3600L);
		wFact.addTaskWithDuration(node, uuid("task2"), 3600.,     0., 9L * 3600L, 2L * 3600L);

		return node;
	}

//	public static Node withThreeTasks() {
//		Node node = wFact.createNode("unnamed", 0., 0.);
//
//		wFact.addTaskWithDuration(node, uuid("task1"), 10., 10.,  6L * 3600L, 1L * 3600L);
//		wFact.addTaskWithDuration(node, uuid("task2"), 20., 10., 12L * 3600L, 3L * 3600L);
//		wFact.addTaskWithDuration(node, uuid("task3"), 20., 20., 18L * 3600L, 2L * 3600L);
//
//		return node;
//	}

}
