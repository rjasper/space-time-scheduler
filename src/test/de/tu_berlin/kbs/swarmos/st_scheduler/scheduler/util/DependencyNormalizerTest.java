package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util.DependencyNormalizer.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util.DependencyNormalizer.DependencyNormalizationException;
import de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory;
import de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory;

public class DependencyNormalizerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static JobSpecification spec(
		String jobIdSeed,
		double earliestStartTime,
		double latestFinishTime,
		double duration)
	{
		return new JobSpecification(
			uuid(jobIdSeed),
			immutablePoint(0, 0),
			secondsToTime(earliestStartTime, TimeFactory.BASE_TIME),
			secondsToTime(latestFinishTime, TimeFactory.BASE_TIME),
			secondsToDuration(duration));
	}
	
	private static Map<UUID, JobSpecification> specMap(JobSpecification... specs) {
		return Arrays.stream(specs)
			.collect(toMap(JobSpecification::getJobId, identity()));
	}
	
	private static void add(
		SimpleDirectedGraph<UUID, DefaultEdge> graph,
		String jobIdSeed,
		String... dependencies)
	{
		UUID jobId = uuid(jobIdSeed);
		graph.addVertex(jobId);
		
		Arrays.stream(dependencies)
			.map(UUIDFactory::uuid)
			.forEach(d -> graph.addEdge(jobId, d));
	}
	
	private static SimpleDirectedGraph<UUID, DefaultEdge> graph() {
		return new SimpleDirectedGraph<>(DefaultEdge.class);
	}

	@Test
	public void testEmpty() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap();
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		
		Map<UUID, JobSpecification> normalized =
			normalizeDependentJobSpecifications(graph, specs, LocalDateTime.MIN);
		
		assertThat("normalized specs were not empty",
			normalized.isEmpty(), is(true));
	}

	@Test
	public void testSingle() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 0, 0, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");

		Map<UUID, JobSpecification> normalized =
			normalizeDependentJobSpecifications(graph, specs, LocalDateTime.MIN);
		
		assertThat("normalized specs don't include original spec",
			normalized.get(uuid("t1")), is(specs.get(uuid("t1"))));
	}

	@Test
	public void testTwo() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 3, 8, 2),
			spec("t2", 4, 9, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");

		Map<UUID, JobSpecification> normalized =
			normalizeDependentJobSpecifications(graph, specs, LocalDateTime.MIN);
		
		JobSpecification ts1Norm = normalized.get(uuid("t1"));
		JobSpecification ts2Norm = normalized.get(uuid("t2"));
		
		assertThat(ts1Norm.getEarliestStartTime(), equalTo(atSecond(3)));
		assertThat(ts1Norm.getLatestStartTime(), equalTo(atSecond(7)));
		assertThat(ts2Norm.getEarliestStartTime(), equalTo(atSecond(5)));
		assertThat(ts2Norm.getLatestStartTime(), equalTo(atSecond(9)));
	}

	@Test
	public void testMultipleDependencies() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 3, 7, 1),
			spec("t2", 2, 8, 4),
			spec("t3", 6, 10, 3),
			spec("t4", 3, 11, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		add(graph, "t3", "t1");
		add(graph, "t4", "t2", "t3");

		Map<UUID, JobSpecification> normalized =
			normalizeDependentJobSpecifications(graph, specs, LocalDateTime.MIN);
		
		JobSpecification ts1Norm = normalized.get(uuid("t1"));
		JobSpecification ts2Norm = normalized.get(uuid("t2"));
		JobSpecification ts3Norm = normalized.get(uuid("t3"));
		JobSpecification ts4Norm = normalized.get(uuid("t4"));

		assertThat(ts1Norm.getEarliestStartTime(), equalTo(atSecond( 3)));
		assertThat(ts1Norm.getLatestStartTime  (), equalTo(atSecond( 6)));
		assertThat(ts2Norm.getEarliestStartTime(), equalTo(atSecond( 4)));
		assertThat(ts2Norm.getLatestStartTime  (), equalTo(atSecond( 7)));
		assertThat(ts3Norm.getEarliestStartTime(), equalTo(atSecond( 6)));
		assertThat(ts3Norm.getLatestStartTime  (), equalTo(atSecond( 8)));
		assertThat(ts4Norm.getEarliestStartTime(), equalTo(atSecond( 9)));
		assertThat(ts4Norm.getLatestStartTime  (), equalTo(atSecond(11)));
	}
	
	@Test
	public void testImpossibleDependencies() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 3, 5, 1),
			spec("t2", 2, 3, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		
		thrown.expect(DependencyNormalizationException.class);

		normalizeDependentJobSpecifications(graph, specs, LocalDateTime.MIN);
	}
	
	@Test
	public void testSingleFrozenHorizon() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 0, 10, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");

		Map<UUID, JobSpecification> normalized =
			normalizeDependentJobSpecifications(graph, specs, atSecond(5));
		
		assertThat("normalized specs violated frozen horizon",
			normalized.get(uuid("t1")).getEarliestStartTime(), is(atSecond(5)));
	}

	@Test
	public void testImpossibleFrozenHorizon() throws DependencyNormalizationException {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 0, 5, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		
		thrown.expect(DependencyNormalizationException.class);
	
		normalizeDependentJobSpecifications(graph, specs, atSecond(10));
	}

}
