package de.tu_berlin.kbs.swarmos.st_scheduler.matchers;

import java.util.Objects;
import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class MapMatcher<T, R> extends TypeSafeMatcher<T> {
	
	private final Matcher<R> matcher;
	private final Function<T, R> mapper;

	public MapMatcher(Matcher<R> matcher, Function<T, R> mapper) {
		this.matcher = Objects.requireNonNull(matcher, "matcher");
		this.mapper = Objects.requireNonNull(mapper, "mapper");
	}

	@Override
	protected boolean matchesSafely(T item) {
		return matcher.matches(mapper.apply(item));
	}

	@Override
	public void describeTo(Description description) {
		matcher.describeTo(description);
	}

}
