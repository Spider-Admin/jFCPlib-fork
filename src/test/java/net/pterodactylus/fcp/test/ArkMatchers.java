package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.ARK;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class ArkMatchers {

	public static Matcher<ARK> isArk(Matcher<? super String> publicUri, Matcher<? super String> privateUri, Matcher<? super Integer> number) {
		return new TypeSafeDiagnosingMatcher<ARK>() {
			@Override
			protected boolean matchesSafely(ARK ark, Description mismatchDescription) {
				if (!publicUri.matches(ark.getPublicURI())) {
					mismatchDescription.appendText("public URI is ").appendValue(ark.getPublicURI());
					return false;
				}
				if (!privateUri.matches(ark.getPrivateURI())) {
					mismatchDescription.appendText("private URI is ").appendValue(ark.getPrivateURI());
					return false;
				}
				if (!number.matches(ark.getNumber())) {
					mismatchDescription.appendText("number is ").appendValue(ark.getNumber());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is ARK with public URI ").appendDescriptionOf(publicUri);
				description.appendText(", private URI ").appendDescriptionOf(privateUri);
				description.appendText(", and number ").appendDescriptionOf(number);
			}
		};
	}

}
