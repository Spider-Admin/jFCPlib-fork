package net.pterodactylus.fcp;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThrows;

public class FcpMessageTest {

	@Test
	public void nameOfFcpMessageIsRetained() {
		FcpMessage message = new FcpMessage("Test");
		assertThat(message.getName(), equalTo("Test"));
	}

	@Test
	public void newMessageDoesNotHaveFields() {
		assertThat(fcpMessage.getFields(), anEmptyMap());
	}

	@Test
	public void setFieldIsRetained() {
		fcpMessage.put("Test field", "test value");
		assertThat(fcpMessage.getFields(), allOf(aMapWithSize(1), hasEntry("Test field", "test value")));
	}

	@Test
	public void setFieldIsReturnedAsSet() {
		fcpMessage.put("Test field", "test value");
		assertThat(fcpMessage.hasField("Test field"), equalTo(true));
	}

	@Test
	public void settingFieldWithValueNullThrowsNullPointerException() {
		assertThrows(NullPointerException.class, () -> fcpMessage.put("Test field", null));
	}

	@Test
	public void settingFieldWithNameNullThrowsNullPointerException() {
		assertThrows(NullPointerException.class, () -> fcpMessage.put(null, "test value"));
	}

	@Test
	public void setFieldsCanBeIteratorOver() {
		fcpMessage.put("Test1", "value1");
		fcpMessage.put("Test2", "value2");
		fcpMessage.put("Test3", "value3");
		assertThat(stream(fcpMessage.spliterator(), false).collect(toList()), containsInAnyOrder("Test1", "Test2", "Test3"));
	}

	@Test
	public void messageAndFieldsAreWrittenToOutputStreamUsingCrLfAndUtf8() throws IOException {
		fcpMessage.put("Test field", "test välue");
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			fcpMessage.write(outputStream);
			assertThat(outputStream.toByteArray(), equalTo("TestMessage\r\nTest field=test välue\r\nEndMessage\r\n".getBytes(UTF_8)));
		}
	}

	@Test
	public void messageWithPayloadIsTerminatedWithDataAndWithoutTerminatingLineBreak() throws Exception {
		fcpMessage.setPayloadInputStream(new ByteArrayInputStream("Test".getBytes()));
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			fcpMessage.write(outputStream);
			assertThat(outputStream.toByteArray(), equalTo("TestMessage\r\nData\r\nTest".getBytes(UTF_8)));
		}
	}

	private final FcpMessage fcpMessage = new FcpMessage("TestMessage");

}
