package net.pterodactylus.fcp;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public class FCPPluginReplyTest {

	@Test
	public void identifierOfBaseMessageIsReturned() {
		baseMessage.setField("Identifier", "test identifier");
		FCPPluginReply reply = new FCPPluginReply(baseMessage, null);
		assertThat(reply.getIdentifier(), equalTo("test identifier"));
	}

	@Test
	public void pluginNameOfBaseMessageIsReturned() {
		baseMessage.setField("PluginName", "test plugin name");
		FCPPluginReply reply = new FCPPluginReply(baseMessage, null);
		assertThat(reply.getPluginName(), equalTo("test plugin name"));
	}

	@Test
	public void dataLengthOfBaseMessageIsReturned() {
		baseMessage.setField("DataLength", "1234");
		FCPPluginReply reply = new FCPPluginReply(baseMessage, null);
		assertThat(reply.getDataLength(), equalTo(1234L));
	}

	@Test
	public void replyFromBaseMessageIsReturned() {
		baseMessage.setField("Replies.Test", "value");
		FCPPluginReply reply = new FCPPluginReply(baseMessage, null);
		assertThat(reply.getReply("Test"), equalTo("value"));
	}

	@Test
	public void allRepliesFromBaseMessageAreReturned() {
		baseMessage.setField("Replies.Field1", "value1");
		baseMessage.setField("Replies.Field2", "value2");
		baseMessage.setField("Field3", "value3");
		FCPPluginReply reply = new FCPPluginReply(baseMessage, null);
		assertThat(reply.getReplies(), allOf(aMapWithSize(2), hasEntry("Field1", "value1"), hasEntry("Field2", "value2")));
	}

	@Test
	public void inputStreamFromBaseMessageIsReturned() throws IOException {
		FCPPluginReply reply = new FCPPluginReply(baseMessage, new ByteArrayInputStream("Hello World!".getBytes(UTF_8)));
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		     InputStream inputStream = reply.getPayloadInputStream()) {
			FcpUtils.copy(inputStream, outputStream);
			assertThat(outputStream.toByteArray(), equalTo("Hello World!".getBytes(UTF_8)));
		}
	}

	private final FcpMessage baseMessage = new FcpMessage("FCPPluginReply");

}
