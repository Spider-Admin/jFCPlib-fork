package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.DSAGroup;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class DsaGroupMatchers {

	public static Matcher<DSAGroup> isDsaGroup(Matcher<? super String> base, Matcher<? super String> prime, Matcher<? super String> subprime) {
		return new TypeSafeDiagnosingMatcher<DSAGroup>() {
			@Override
			protected boolean matchesSafely(DSAGroup dsaGroup, Description mismatchDescription) {
				if (!base.matches(dsaGroup.getBase())) {
					mismatchDescription.appendText("base is ").appendValue(dsaGroup.getBase());
					return false;
				}
				if (!prime.matches(dsaGroup.getPrime())) {
					mismatchDescription.appendText("prime is ").appendValue(dsaGroup.getPrime());
					return false;
				}
				if (!subprime.matches(dsaGroup.getSubprime())) {
					mismatchDescription.appendText("subprime is ").appendValue(dsaGroup.getSubprime());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is dsa group with base ").appendDescriptionOf(base);
				description.appendText(", prime ").appendDescriptionOf(prime);
				description.appendText(", and subprime ").appendDescriptionOf(subprime);
			}
		};
	}

}
