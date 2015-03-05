package scheduler.pickers;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.UUIDFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Test;

import scheduler.TaskSpecification;
import util.TimeFactory;
import util.UUIDFactory;

public class DependentTaskIteratorTest {
	
	private static TaskSpecification spec(String taskIdSeed, double timeInSeconds) {
		LocalDateTime time = secondsToTime(timeInSeconds, TimeFactory.BASE_TIME);
		
		return new TaskSpecification(
			uuid(taskIdSeed),
			immutablePoint(0, 0),
			time,
			time,
			Duration.ofSeconds(1));
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
	
	private static List<UUID> asList(String... uuidSeeds) {
		return Arrays.stream(uuidSeeds)
			.map(UUIDFactory::uuid)
			.collect(toList());
	}
	
	private static List<UUID> collect(Iterator<TaskSpecification> iterator) {
		List<UUID> list = new LinkedList<>();
		
		while (iterator.hasNext())
			list.add(iterator.next().getTaskId());
		
		return list;
	}
	
	private static Matcher<List<?>> equalTo(List<?> list) {
		return new TypeSafeMatcher<List<?>>() {
			@Override
			public void describeTo(Description description) {
				description
					.appendText("a list ")
					.appendValue(list);
			}
			@Override
			protected boolean matchesSafely(List<?> item) {
				
				Iterator<?> expected = list.iterator();
				Iterator<?> actual = item.iterator();
				
				while (expected.hasNext() && actual.hasNext()) {
					if (!expected.next().equals(actual.next()))
						return false;
				}
				
				return !expected.hasNext() && !actual.hasNext();
			}
		};
	}

	@Test
	public void testEmpty() {
		Map<UUID, TaskSpecification> specs = specMap();
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		
		DependentTaskIterator it = new DependentTaskIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList()));
	}

	@Test
	public void testSingle() {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		
		DependentTaskIterator it = new DependentTaskIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1")));
	}

	@Test
	public void testTwo() {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 1),
			spec("t2", 2));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		
		DependentTaskIterator it = new DependentTaskIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1", "t2")));
	}

	@Test
	public void testEarliestDeadlineFirst() {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 2),
			spec("t2", 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2");
		
		DependentTaskIterator it = new DependentTaskIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t2", "t1")));
	}

	@Test
	public void testMultipleDependenciesEDF() {
		Map<UUID, TaskSpecification> specs = specMap(
			spec("t1", 1),
			spec("t2", 4),
			spec("t3", 2),
			spec("t4", 3));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		add(graph, "t3", "t1");
		add(graph, "t4", "t2", "t3");
		
		DependentTaskIterator it = new DependentTaskIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1", "t3", "t2", "t4")));
	}

}
