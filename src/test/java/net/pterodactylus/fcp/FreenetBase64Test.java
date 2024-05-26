package net.pterodactylus.fcp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link FreenetBase64Test}.
 */
public class FreenetBase64Test {

	@Test
	public void testEncodingVectorsFromRfc4648() {
		getRfc4648TestVectors().forEach(vector -> assertThat(encoder.encode(vector[0].getBytes(UTF_8)), equalTo(vector[1])));
	}

	@Test
	public void testDecodingVectorsFromRfc4648() {
		getRfc4648TestVectors().forEach(vector -> assertThat(encoder.decode(vector[1]), equalTo(vector[0].getBytes(UTF_8))));
	}

	private static List<String[]> getRfc4648TestVectors() {
		return asList(
			vector("", ""),
			vector("f", "Zg=="),
			vector("fo", "Zm8="),
			vector("foo", "Zm9v"),
			vector("foob", "Zm9vYg=="),
			vector("fooba", "Zm9vYmE="),
			vector("foobar", "Zm9vYmFy")
		);
	}

	private static String[] vector(String plain, String encoded) {
		return new String[] { plain, encoded };
	}

	private final FreenetBase64 encoder = new FreenetBase64();

}
