package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.Peer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class PeerMatchers {

	public static Matcher<Peer> peerWithIdentity(Matcher<? super String> identityMatcher) {
		return new TypeSafeDiagnosingMatcher<Peer>() {
			@Override
			protected boolean matchesSafely(Peer peer, Description mismatchDescription) {
				if (!identityMatcher.matches(peer.getIdentity())) {
					identityMatcher.describeMismatch(peer.getIdentity(), mismatchDescription);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is peer with identity ").appendValue(identityMatcher);
			}
		};
	}

}
