package net.pterodactylus.fcp.test;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamMatchers {

	public static Matcher<InputStream> streamContaining(int... content) {
		return new TypeSafeDiagnosingMatcher<InputStream>() {
			@Override
			protected boolean matchesSafely(InputStream inputStream, Description mismatchDescription) {
				try {
					for (int index = 0; index < content.length; index++) {
						int readByte = inputStream.read();
						if (readByte != content[index]) {
							mismatchDescription.appendText("was ").appendValue(readByte).appendText(" at offset ").appendValue(index);
							return false;
						}
					}
					int eof = inputStream.read();
					if (eof != -1) {
						mismatchDescription.appendText("contained more than ").appendValue(content.length).appendText(" bytes");
						return false;
					}
				} catch (IOException e) {
					mismatchDescription.appendText("could not be read (").appendValue(e).appendText(")");
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is input stream containing ").appendValue(content);
			}
		};
	}

}
