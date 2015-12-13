package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToTime;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertThat;

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

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.time.TimeFactory;
import de.tu_berlin.mailbox.rjasper.util.UUIDFactory;

public class DependentJobIteratorTest {
	
	private static JobSpecification spec(String jobIdSeed, double timeInSeconds) {
		LocalDateTime time = secondsToTime(timeInSeconds, TimeFactory.BASE_TIME);
		
		return new JobSpecification(
			uuid(jobIdSeed),
			immutablePoint(0, 0),
			time,
			time,
			Duration.ofSeconds(1));
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
	
	private static List<UUID> asList(String... uuidSeeds) {
		return Arrays.stream(uuidSeeds)
			.map(UUIDFactory::uuid)
			.collect(toList());
	}
	
	private static List<UUID> collect(Iterator<JobSpecification> iterator) {
		List<UUID> list = new LinkedList<>();
		
		while (iterator.hasNext())
			list.add(iterator.next().getJobId());
		
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
		Map<UUID, JobSpecification> specs = specMap();
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		
		DependentJobIterator it = new DependentJobIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList()));
	}

	@Test
	public void testSingle() {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		
		DependentJobIterator it = new DependentJobIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1")));
	}

	@Test
	public void testTwo() {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 1),
			spec("t2", 2));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		
		DependentJobIterator it = new DependentJobIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1", "t2")));
	}

	@Test
	public void testEarliestDeadlineFirst() {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 2),
			spec("t2", 1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2");
		
		DependentJobIterator it = new DependentJobIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t2", "t1")));
	}

	@Test
	public void testMultipleDependenciesEDF() {
		Map<UUID, JobSpecification> specs = specMap(
			spec("t1", 1),
			spec("t2", 4),
			spec("t3", 2),
			spec("t4", 3));
		
		SimpleDirectedGraph<UUID, DefaultEdge> graph = graph();
		add(graph, "t1");
		add(graph, "t2", "t1");
		add(graph, "t3", "t1");
		add(graph, "t4", "t2", "t3");
		
		DependentJobIterator it = new DependentJobIterator(graph, specs);
		
		assertThat(collect(it), equalTo(asList("t1", "t3", "t2", "t4")));
	}

}
