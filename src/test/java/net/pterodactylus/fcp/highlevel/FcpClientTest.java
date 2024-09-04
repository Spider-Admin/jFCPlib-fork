package net.pterodactylus.fcp.highlevel;

import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.SSKKeypair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.pterodactylus.fcp.test.InputStreamMatchers.streamContaining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FcpClientTest {

	@Test
	public void canCreateFcpClient() {
		FcpConnection fcpConnection = createFcpConnection();
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
		}
	}

	@Test
	public void connectingFcpClientSendsClientHelloWithCorrectClientName() throws IOException, FcpException {
		sendAndVerifyClientHelloMessage("Name", "Test Client");
	}

	@Test
	public void connectingFcpClientSendsClientHelloWithCorrectExpectedVersion() throws IOException, FcpException {
		sendAndVerifyClientHelloMessage("ExpectedVersion", "2.0");
	}

	private static void sendAndVerifyClientHelloMessage(String messageField, String expectedValue) throws IOException, FcpException {
		AtomicReference<String> sentMessageField = new AtomicReference<>();
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ClientHello")) {
				sentMessageField.set(message.getField(messageField));
				return (listener, connection) -> listener.receivedNodeHello(connection, new NodeHello(new FcpMessage("NodeHello").put("Node", "TestNode")));
			}
			return (l, c) -> {
			};
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection, false)) {
			fcpClient.connect("Test Client");
			assertThat(sentMessageField.get(), equalTo(expectedValue));
		}
	}

	@Test
	public void closingFcpClientWillCloseFcpConnection() {
		FcpConnection fcpConnection = createFcpConnection();
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
		}
		assertThat(fcpConnection.isClosed(), equalTo(true));
	}

	@Test
	public void getUriReturnsSuccessfulGetResult() throws Exception {
		sendClientGetAndReturnAllData(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", message.getField("Identifier")).put("ContentType", "text/plain").put("DataLength", "4"), new ByteArrayInputStream("Data".getBytes())));
			}
		});
	}

	@Test
	public void getUriIgnoresAllDataForDifferentIdentifier() throws Exception {
		sendClientGetAndReturnAllData(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", "OtherIdentifier").put("ContentType", "text/plain").put("DataLength", "2"), new ByteArrayInputStream("OK".getBytes())));
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", message.getField("Identifier")).put("ContentType", "text/plain").put("DataLength", "4"), new ByteArrayInputStream("Data".getBytes())));
			}
		});
	}

	@Test
	public void getUriFollowsRedirectForCode27() throws Exception {
		sendClientGetAndReturnAllData(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedGetFailed(connection, new GetFailed(new FcpMessage("GetFailed").put("Identifier", message.getField("Identifier")).put("Code", "27").put("RedirectURI", "KSK@test-2")));
			}
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test-2")) {
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", message.getField("Identifier")).put("ContentType", "text/plain").put("DataLength", "4"), new ByteArrayInputStream("Data".getBytes())));
			}
		});
	}

	@Test
	public void getUriFollowsRedirectForCode24() throws Exception {
		sendClientGetAndReturnAllData(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedGetFailed(connection, new GetFailed(new FcpMessage("GetFailed").put("Identifier", message.getField("Identifier")).put("Code", "24").put("RedirectURI", "KSK@test-2")));
			}
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test-2")) {
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", message.getField("Identifier")).put("ContentType", "text/plain").put("DataLength", "4"), new ByteArrayInputStream("Data".getBytes())));
			}
		});
	}

	@Test
	public void getUriIgnoresGetFailedForDifferentIdentifier() throws Exception {
		sendClientGetAndReturnAllData(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedGetFailed(connection, new GetFailed(new FcpMessage("GetFailed").put("Identifier", "OtherIdentifier").put("Code", "1")));
				listener.receivedGetFailed(connection, new GetFailed(new FcpMessage("GetFailed").put("Identifier", message.getField("Identifier")).put("Code", "27").put("RedirectURI", "KSK@test-2")));
			}
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test-2")) {
				listener.receivedAllData(connection, new AllData(new FcpMessage("AllData").put("Identifier", message.getField("Identifier")).put("ContentType", "text/plain").put("DataLength", "4"), new ByteArrayInputStream("Data".getBytes())));
			}
		});
	}

	@Test
	public void getUriFailesOnGetFailed() throws Exception {
		FcpConnection fcpConnection = createFcpConnection(message -> (listener, connection) -> {
			if (message.getName().equals("ClientGet") && message.getField("URI").equals("KSK@test")) {
				listener.receivedGetFailed(connection, new GetFailed(new FcpMessage("GetFailed").put("Identifier", message.getField("Identifier")).put("Code", "123").put("RedirectURI", "KSK@test-2")));
			}
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			GetResult getResult = fcpClient.getURI("KSK@test");
			assertThat(getResult.isSuccess(), equalTo(false));
			assertThat(getResult.getErrorCode(), equalTo(123));
		}
	}

	private void sendClientGetAndReturnAllData(Function<FcpMessage, BiConsumer<FcpListener, FcpConnection>> messageReplier) throws Exception {
		FcpConnection fcpConnection = createFcpConnection(messageReplier);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			GetResult getResult = fcpClient.getURI("KSK@test");
			assertThat(getResult.isSuccess(), equalTo(true));
			assertThat(getResult.getContentLength(), equalTo(4L));
			assertThat(getResult.getInputStream(), streamContaining('D', 'a', 't', 'a'));
		}
	}
	@Test
	public void generatingKeyPairSendsCorrectMessage() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("GenerateSSK")) {
				return ((listener, connection) -> listener.receivedSSKKeypair(connection, new SSKKeypair(new FcpMessage("SSKKeypair").put("InsertURI", "insert-uri").put("RequestURI", "request-uri"))));
			}
			return (l, c) -> {
			};
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			SSKKeypair keypair = fcpClient.generateKeyPair();
			assertThat(keypair.getInsertURI(), equalTo("insert-uri"));
			assertThat(keypair.getRequestURI(), equalTo("request-uri"));
		}
	}

	private static FcpConnection createFcpConnection() {
		return createFcpConnection(m -> (l, c) -> {
		});
	}

	private static FcpConnection createFcpConnection(Function<FcpMessage, BiConsumer<FcpListener, FcpConnection>> messageConsumer) {
		return new FcpConnection() {
			private final List<FcpListener> listeners = new ArrayList<>();
			private volatile boolean closed = false;

			@Override
			public void addFcpListener(FcpListener fcpListener) {
				listeners.add(fcpListener);
			}

			@Override
			public void removeFcpListener(FcpListener fcpListener) {
				listeners.remove(fcpListener);
			}

			@Override
			public boolean isClosed() {
				return closed;
			}

			@Override
			public void connect() throws IOException, IllegalStateException {
			}

			@Override
			public void disconnect() {
				close();
			}

			@Override
			public void close() {
				closed = true;
			}

			@Override
			public void sendMessage(FcpMessage fcpMessage) {
				listeners.forEach(listener -> messageConsumer.apply(fcpMessage).accept(listener, this));
			}
		};
	}

	@Rule
	public final Timeout timeout = Timeout.seconds(5);

}
