package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.fixtures;

import static de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory.*;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Node;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.factories.NodeFactory;

public final class NodeFixtures {

	private static NodeFactory wFact = NodeFactory.getInstance();

	public static Node withTwoJobs1() {
		Node node = wFact.createNode("unnamed", 0., 0.);

		wFact.addJobWithDuration(node, uuid("job1"), -3600.,    0., 2L * 3600L, 3L * 3600L);
		wFact.addJobWithDuration(node, uuid("job2"),     0., 3600., 9L * 3600L, 3L * 3600L);

		return node;
	}

	public static Node withTwoJobs2() {
		Node node = wFact.createNode("unnamed", 0., 0.);

		wFact.addJobWithDuration(node, uuid("job1"),    0., -3600., 2L * 3600L, 2L * 3600L);
		wFact.addJobWithDuration(node, uuid("job2"), 3600.,     0., 9L * 3600L, 2L * 3600L);

		return node;
	}

//	public static Node withThreeJobs() {
//		Node node = wFact.createNode("unnamed", 0., 0.);
//
//		wFact.addJobWithDuration(node, uuid("job1"), 10., 10.,  6L * 3600L, 1L * 3600L);
//		wFact.addJobWithDuration(node, uuid("job2"), 20., 10., 12L * 3600L, 3L * 3600L);
//		wFact.addJobWithDuration(node, uuid("job3"), 20., 20., 18L * 3600L, 2L * 3600L);
//
//		return node;
//	}

}
