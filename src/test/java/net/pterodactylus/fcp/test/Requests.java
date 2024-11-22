package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.highlevel.GetRequest;
import net.pterodactylus.fcp.highlevel.PutRequest;
import net.pterodactylus.fcp.highlevel.Request;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;

public class Requests {

	public static Matcher<Request> isRequest() {
		return isRequest(anything());
	}

	@SafeVarargs
	public static Matcher<Request> isRequest(Matcher<? super String> identifier, Matcher<? super Request>... requestMatchers) {
		return isRequest(anything(), identifier, requestMatchers);
	}

	public static Matcher<Request> isGetRequest() {
		return isGetRequest(anything());
	}

	@SafeVarargs
	public static Matcher<Request> isGetRequest(Matcher<? super String> identifier, Matcher<? super Request>... requestMatchers) {
		return isRequest(instanceOf(GetRequest.class), identifier, requestMatchers);
	}

	public static Matcher<Request> isPutRequest() {
		return isPutRequest(anything());
	}

	public static Matcher<Request> isPutRequest(Matcher<? super String> identifier) {
		return isRequest(instanceOf(PutRequest.class), identifier);
	}

	@SafeVarargs
	private static TypeSafeDiagnosingMatcher<Request> isRequest(Matcher<? super Class<?>> requestClass, Matcher<? super String> identifier, Matcher<? super Request>... requestMatchers) {
		return new TypeSafeDiagnosingMatcher<Request>() {
			@Override
			protected boolean matchesSafely(Request item, Description mismatchDescription) {
				if (!requestClass.matches(item)) {
					mismatchDescription.appendText("is a " + item.getClass().getSimpleName());
					return false;
				}
				if (!identifier.matches(item.getIdentifier())) {
					mismatchDescription.appendText("identifier is ").appendValue(item.getIdentifier());
					return false;
				}
				for (Matcher<? super Request> requestMatcher : requestMatchers) {
					if (!requestMatcher.matches(item)) {
						requestMatcher.describeMismatch(item, mismatchDescription);
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendDescriptionOf(requestClass);
			}
		};
	}

}
