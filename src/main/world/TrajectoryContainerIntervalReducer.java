// TODO remove
package world;

//import static util.Comparables.*;
//
//import java.time.LocalDateTime;
//import java.util.Objects;
//import java.util.function.BinaryOperator;
//
//import util.TriFunction;
//
//public class TrajectoryContainerIntervalReducer<T> {
//	
//	private final TrajectoryContainer container;
//	
//	private final TriFunction<Trajectory, LocalDateTime, LocalDateTime, T> mapper;
//	
//	private final BinaryOperator<T> combiner;
//	
//	private final T identity;
//	
//	public TrajectoryContainerIntervalReducer(
//		TrajectoryContainer container,
//		TriFunction<Trajectory, LocalDateTime, LocalDateTime, T> mapper,
//		BinaryOperator<T> combiner,
//		T identity)
//	{
//		this.container = container;
//		this.mapper = mapper;
//		this.combiner = combiner;
//		this.identity = identity;
//	}
//
//	public T reduce(LocalDateTime from, LocalDateTime to) {
//		Objects.requireNonNull(from, "from");
//		Objects.requireNonNull(to, "to");
//		
//		if (from.isAfter(to))
//			throw new IllegalArgumentException("from is after to");
//		
//		return container.getTrajectories(from, to).stream()
//			.map(t -> {
//				LocalDateTime tFrom = max(from, t.getStartTime());
//				LocalDateTime tTo = min(to, t.getFinishTime());
//				return mapper.apply(t, tFrom, tTo);
//			})
//			.reduce(combiner)
//			.orElse(identity);
//	}
//
//}
