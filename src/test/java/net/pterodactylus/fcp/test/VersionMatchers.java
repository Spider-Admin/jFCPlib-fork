package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.Version;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class VersionMatchers {

	public static Matcher<Version> isVersion(Matcher<? super String> nodeName, Matcher<? super String> treeVersion, Matcher<? super String> protocolVersion, Matcher<? super Integer> buildNumber) {
		return new TypeSafeDiagnosingMatcher<Version>() {
			@Override
			protected boolean matchesSafely(Version version, Description mismatchDescription) {
				if (!nodeName.matches(version.getNodeName())) {
					mismatchDescription.appendText("node name is ").appendValue(version.getNodeName());
					return false;
				}
				if (!treeVersion.matches(version.getTreeVersion())) {
					mismatchDescription.appendText("tree version is ").appendValue(version.getTreeVersion());
					return false;
				}
				if (!protocolVersion.matches(version.getProtocolVersion())) {
					mismatchDescription.appendText("protocol version is ").appendValue(version.getProtocolVersion());
					return false;
				}
				if (!buildNumber.matches(version.getBuildNumber())) {
					mismatchDescription.appendText("build number is ").appendValue(version.getBuildNumber());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is version with node name ").appendDescriptionOf(nodeName);
				description.appendText(", tree version ").appendDescriptionOf(treeVersion);
				description.appendText(", protocol version ").appendDescriptionOf(protocolVersion);
				description.appendText(", and build number ").appendDescriptionOf(buildNumber);
			}
		};
	}

}
