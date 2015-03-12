package scheduler.util;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static scheduler.util.DependencyNormalizer.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import scheduler.TaskSpecification;
import scheduler.util.DependencyNormalizer.DependencyNormalizationException;
import util.TimeFactory;
import util.UUIDFactory;

public class DependencyNormalizerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static TaskSpecification spec(
		String taskIdSeed,
		double earliestStartTime,
		double latestFinishTime,
		double duration)
	{
		return new TaskSpecification(
			uuid(taskIdSeed),
			immutablePoint(0, 0),
			secondsToTime(earliestStartTime, TimeFactory.BASE_TIME),
			secondsToTime(latestFinishTime, TimeFactory.BASE_TIME),
			secondsToDuration(duration));
	}
	
	private static Map<UUID, TaskSpecification> specMap(TaskSpecification... specs) {
		return Arrays.stream(specs)
			.collect(toMap(TaskSpecification::getTaskId, identity()));
	}
	
	private static void add(
		SimpleDirectedGraph<UUID, DefaultEdge> graph,
		String taskIdSeed,
		String... dependencies)
	{
		UUID taskId = uuid(taskIdSeed);
		graph.addVertex(taskId);
		
		Arrays.stream(dependencies)
			.map(UUIDFactory::uuid)
			.forEach(d -> graph.addEdge(taskId, d));
	}
	
	private static SimpleDirectedGraph<UUID, DefaultEdge> graph() {
		return new SimpleDirectedGraph<>(DefaultEdge.class);
	}

	@Test
	public void testEmpty() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap();
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		
		Map<UUID, TaskSpecification> normalized =
			normalizeDependentTaskSpecifications(graph, specs, LocalDateTime.MIN);
		
		assertThat("normalized specs were not empty",
			normalized.isEmpty(), is(true));
	}

	@Test
	public void testSingle() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 0, 0, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");

		Map<UUID, TaskSpecification> normalized =
			normalizeDependentTaskSpecifications(graph, specs, LocalDateTime.MIN);
		
		assertThat("normalized specs don't include original spec",
			normalized.get(uuid("t1")), is(specs.get(uuid("t1"))));
	}

	@Test
	public void testTwo() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 3, 8, 2),
			spec("t2", 4, 9, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");

		Map<UUID, TaskSpecification> normalized =
			normalizeDependentTaskSpecifications(graph, specs, LocalDateTime.MIN);
		
		TaskSpecification ts1Norm = normalized.get(uuid("t1"));
		TaskSpecification ts2Norm = normalized.get(uuid("t2"));
		
		assertThat(ts1Norm.getEarliestStartTime(), equalTo(atSecond(3)));
		assertThat(ts1Norm.getLatestStartTime(), equalTo(atSecond(7)));
		assertThat(ts2Norm.getEarliestStartTime(), equalTo(atSecond(5)));
		assertThat(ts2Norm.getLatestStartTime(), equalTo(atSecond(9)));
	}

	@Test
	public void testMultipleDependencies() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 3, 7, 1),
			spec("t2", 2, 8, 4),
			spec("t3", 6, 10, 3),
			spec("t4", 3, 11, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		add(graph, "t3", "t1");
		add(graph, "t4", "t2", "t3");

		Map<UUID, TaskSpecification> normalized =
			normalizeDependentTaskSpecifications(graph, specs, LocalDateTime.MIN);
		
		TaskSpecification ts1Norm = normalized.get(uuid("t1"));
		TaskSpecification ts2Norm = normalized.get(uuid("t2"));
		TaskSpecification ts3Norm = normalized.get(uuid("t3"));
		TaskSpecification ts4Norm = normalized.get(uuid("t4"));

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
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 3, 5, 1),
			spec("t2", 2, 3, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		
		thrown.expect(DependencyNormalizationException.class);

		normalizeDependentTaskSpecifications(graph, specs, LocalDateTime.MIN);
	}
	
	@Test
	public void testSingleFrozenHorizon() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 0, 10, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");

		Map<UUID, TaskSpecification> normalized =
			normalizeDependentTaskSpecifications(graph, specs, atSecond(5));
		
		assertThat("normalized specs violated frozen horizon",
			normalized.get(uuid("t1")).getEarliestStartTime(), is(atSecond(5)));
	}

	@Test
	public void testImpossibleFrozenHorizon() throws DependencyNormalizationException {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 0, 5, 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		
		thrown.expect(DependencyNormalizationException.class);
	
		normalizeDependentTaskSpecifications(graph, specs, atSecond(10));
	}

}
