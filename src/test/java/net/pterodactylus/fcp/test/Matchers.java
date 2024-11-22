package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.highlevel.Request;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class Matchers {

	public static Matcher<FcpMessage> isMessage(String name, String... lines) {
		return isMessage(name, containsInAnyOrder(lines), equalTo("EndMessage"), anything());
	}

	public static Matcher<FcpMessage> isDataMessage(String name, String... lines) {
		return isMessage(name, containsInAnyOrder(lines), equalTo("Data"), anything());
	}

	public static Matcher<FcpMessage> isMessage(String name, Matcher<? super Iterable<? super String>> linesMatcher) {
		return isMessage(name, linesMatcher, equalTo("EndMessage"), anything());
	}

	public static Matcher<FcpMessage> isDataMessage(String name, Matcher<? super Iterable<? super String>> linesMatcher) {
		return isMessage(name, linesMatcher, equalTo("Data"), anything());
	}

	public static Matcher<FcpMessage> isDataMessage(String name, Matcher<? super Iterable<? super String>> linesMatcher, Matcher<? super Iterable<? super String>> dataMatcher) {
		return isMessage(name, linesMatcher, equalTo("Data"), dataMatcher);
	}

	public static Matcher<FcpMessage> isMessage(String name, Matcher<? super Iterable<? super String>> linesMatcher, Matcher<? super String> terminatorMatcher, Matcher<? super Iterable<? super String>> dataMatcher) {
		return new TypeSafeDiagnosingMatcher<FcpMessage>() {
			@Override
			protected boolean matchesSafely(FcpMessage fcpMessage, Description mismatchDescription) {
				Iterator<String> messageLines = MessageUtils.encodeMessage(fcpMessage).iterator();
				String messageName = messageLines.next();
				if (!messageName.equals(name)) {
					mismatchDescription.appendText("name is ").appendValue(messageName);
					return false;
				}
				List<String> parameterLines = new ArrayList<>();
				while (true) {
					String actualLine = messageLines.next();
					if (actualLine.contains("=")) {
						parameterLines.add(actualLine);
					} else {
						if (!terminatorMatcher.matches(actualLine)) {
							terminatorMatcher.describeMismatch(actualLine, mismatchDescription);
							return false;
						}
						if (!linesMatcher.matches(parameterLines)) {
							linesMatcher.describeMismatch(parameterLines, mismatchDescription);
							return false;
						}
						List<String> dataLines = new ArrayList<>();
						messageLines.forEachRemaining(dataLines::add);
						if (!dataMatcher.matches(dataLines)) {
							dataMatcher.describeMismatch(dataLines, mismatchDescription);
							return false;
						}
						return true;
					}
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is message named ").appendValue(name)
						.appendText(" with parameters ").appendDescriptionOf(linesMatcher)
						.appendText(" terminated with ").appendDescriptionOf(terminatorMatcher)
						.appendText(" and data of ").appendDescriptionOf(dataMatcher);
			}
		};
	}

	public static Matcher<FcpMessage> hasField(String name, Matcher<? super String> valueMatcher) {
		return new TypeSafeDiagnosingMatcher<FcpMessage>() {
			@Override
			protected boolean matchesSafely(FcpMessage fcpMessage, Description mismatchDescription) {
				Object value = fcpMessage.getField(name);
				if (!valueMatcher.matches(value)) {
					valueMatcher.describeMismatch(value, mismatchDescription);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has field ").appendValue(name).appendText(" with value ").appendDescriptionOf(valueMatcher);
			}
		};
	}

	public static <R extends Request> Matcher<R> matches(String name, Predicate<R> predicate) {
		return new TypeSafeDiagnosingMatcher<R>() {
			@Override
			protected boolean matchesSafely(R item, Description mismatchDescription) {
				if (!predicate.test(item)) {
					mismatchDescription.appendText("not ").appendValue(name);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(name);
			}
		};
	}

}
