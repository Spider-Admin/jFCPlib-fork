package net.pterodactylus.fcp;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class FCPPluginMessageTest {

	@Test
	public void messageCanBeCreatedWithIdentifierAndPluginClass() {
		FcpMessage message = new FCPPluginMessage("identifier", "test.Plugin");
		assertThat(message.getField("Identifier"), equalTo("identifier"));
		assertThat(message.getField("PluginName"), equalTo("test.Plugin"));
	}

	@Test
	public void parametersAreSetAsFieldsWithPrefix() {
		FCPPluginMessage message = new FCPPluginMessage("identifier", "test.Plugin");
		message.setParameter("One", "1");
		assertThat(message.getField("Param.One"), equalTo("1"));
	}

	@Test
	public void settingDataLengthAndPayloadIncludesLengthAndDataInWrittenMessage() throws IOException {
		FCPPluginMessage message = new FCPPluginMessage("identifier", "test.Plugin");
		message.setData(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), 12);
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			message.write(outputStream);
			String renderedMessage = new String(outputStream.toByteArray(), UTF_8);
			assertThat(renderedMessage, containsString("\r\nDataLength=12\r\n"));
			assertThat(renderedMessage, endsWith("\r\nData\r\nHello World!"));
		}
	}

}
