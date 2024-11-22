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

	public static Matcher<Request> isGetRequest() {
		return isGetRequest(anything());
	}

	public static Matcher<Request> isGetRequest(Matcher<? super String> identifier) {
		return isRequest(instanceOf(GetRequest.class), identifier);
	}

	public static Matcher<Request> isPutRequest() {
		return isPutRequest(anything());
	}

	public static Matcher<Request> isPutRequest(Matcher<? super String> identifier) {
		return isRequest(instanceOf(PutRequest.class), identifier);
	}

	private static TypeSafeDiagnosingMatcher<Request> isRequest(Matcher<? super Class<?>> requestClass, Matcher<? super String> identifier) {
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
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendDescriptionOf(requestClass);
			}
		};
	}

}
