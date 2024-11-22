package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.highlevel.GetRequest;
import net.pterodactylus.fcp.highlevel.Request;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Requests {

	public static Matcher<Request> isGetRequest() {
		return isGetRequest(Matchers.anything());
	}

	public static Matcher<Request> isGetRequest(Matcher<? super String> identifier) {
		return new TypeSafeDiagnosingMatcher<Request>() {
			@Override
			protected boolean matchesSafely(Request item, Description mismatchDescription) {
				if (!(item instanceof GetRequest)) {
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
				description.appendText("get request");
			}
		};
	}

}
