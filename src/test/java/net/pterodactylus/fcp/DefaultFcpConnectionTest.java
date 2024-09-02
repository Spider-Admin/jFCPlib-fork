/*
 * jFCPlib - DefaultFcpConnectionTest.java - Copyright © 2023 David ‘Bombe’ Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.fcp;

import net.pterodactylus.fcp.test.LocalServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DefaultFcpConnectionTest {

	@Test
	public void canConnectToAddress() throws Exception {
		runWithConnection(fcpConnection -> {
			Socket socket = localServer.getSocket();
			assertThat(socket, notNullValue());
		});
	}

	@Test
	public void clientHelloIsSentCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			FcpMessage fcpMessage = new FcpMessage("TestClient");
			fcpMessage.setField("Test", "yes");
			fcpConnection.sendMessage(fcpMessage);
		});
		Socket socket = localServer.getSocket();
		try (InputStream socketInputStream = socket.getInputStream();
		     Reader socketReader = new InputStreamReader(socketInputStream, UTF_8);
		     BufferedReader bufferedReader = new BufferedReader(socketReader)) {
			List<String> sentLines = new ArrayList<>();
			String line = bufferedReader.readLine();
			while (line != null) {
				sentLines.add(line);
				line = bufferedReader.readLine();
			}
			assertThat(sentLines, contains("TestClient", "Test=yes", "EndMessage"));
		}
	}

	@Test
	public void allDataIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<AllData> receivedAllData = new ArrayBlockingQueue<>(1);
			StringBuffer receivedPayload = new StringBuffer();
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
					try {
						while (true) {
							int r = allData.getPayloadInputStream().read();
							if (r == -1) {
								break;
							}
							receivedPayload.append((char) r);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					receivedAllData.add(allData);
				}
			});
			writeMessageToSocket("AllData", "Hello World!".getBytes(UTF_8), "Identifier=TestRequest", "DataLength=12", "StartupTime=1000", "CompletionTime=2000", "Metadata.ContentType=application/x-test-data");
			AllData allData = receivedAllData.take();
			assertThat(allData.getIdentifier(), equalTo("TestRequest"));
			assertThat(allData.getDataLength(), equalTo(12L));
			assertThat(allData.getStartupTime(), equalTo(1000L));
			assertThat(allData.getCompletionTime(), equalTo(2000L));
			assertThat(allData.getContentType(), equalTo("application/x-test-data"));
			assertThat(receivedPayload.toString(), equalTo("Hello World!"));
		});
	}

	@Test
	public void closedConnectionIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<Boolean> connectionClosed = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
					connectionClosed.add(true);
				}
			});
			localServer.getSocket().close();
			assertThat(connectionClosed.take(), equalTo(true));
		});
	}

	@Test
	public void closeConnectionDuplicateNameIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("CloseConnectionDuplicateClientName");
	}

	@Test
	public void nodeHelloIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("NodeHello");
	}

	@Test
	public void protocolErrorIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("ProtocolError");
	}

	@Test
	public void persistentGetIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PersistentGet");
	}

	@Test
	public void persistentPutIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PersistentPut");
	}

	@Test
	public void simpleProgressIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("SimpleProgress");
	}

	@Test
	public void persistentPutDirIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PersistentPutDir");
	}

	@Test
	public void uriGeneratedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("URIGenerated");
	}

	@Test
	public void endListPersistentRequestsIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("EndListPersistentRequests");
	}

	@Test
	public void peerIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("Peer");
	}

	@Test
	public void peerNoteIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PeerNote");
	}

	@Test
	public void startedCompressionIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("StartedCompression");
	}

	@Test
	public void finishedCompressionIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("FinishedCompression");
	}

	@Test
	public void getFailedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("GetFailed");
	}

	@Test
	public void putFetchableIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PutFetchable");
	}

	@Test
	public void putSuccessfulIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PutSuccessful");
	}

	@Test
	public void putFailedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PutFailed");
	}

	@Test
	public void dataFoundIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("DataFound");
	}

	@Test
	public void subscribedUSKUpdateIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("SubscribedUSKUpdate");
	}

	@Test
	public void subscribedUSKIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("SubscribedUSK");
	}

	@Test
	public void identifierCollisionIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("IdentifierCollision");
	}

	@Test
	public void endListPeerNotesIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("EndListPeerNotes");
	}

	@Test
	public void endListPeersIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("EndListPeers");
	}

	@Test
	public void sskKeypairIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("SSKKeypair");
	}

	@Test
	public void peerRemovedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PeerRemoved");
	}

	@Test
	public void persistentRequestModifiedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PersistentRequestModified");
	}

	@Test
	public void persistentRequestRemovedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PersistentRequestRemoved");
	}

	@Test
	public void unknownPeerNoteTypeIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("UnknownPeerNoteType");
	}

	@Test
	public void unknownNodeIdentifierIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("UnknownNodeIdentifier");
	}

	@Test
	public void fcpPluginReplyIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("FCPPluginReply");
	}

	@Test
	public void pluginInfoIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PluginInfo");
	}

	@Test
	public void pluginRemovedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("PluginRemoved");
	}

	@Test
	public void nodeDataIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("NodeData", "ark.pubURI=", "ark.number=0", "auth.negTypes=0", "version=0,1,2,3", "lastGoodVersion=0,1,2,3");
	}

	@Test
	public void testDDAReplyIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("TestDDAReply");
	}

	@Test
	public void testDDACompleteIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("TestDDAComplete");
	}

	@Test
	public void configDataIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("ConfigData");
	}

	@Test
	public void sentFeedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("SentFeed");
	}

	@Test
	public void receivedBookmarkFeedIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("ReceivedBookmarkFeed");
	}

	@Test
	public void messageIsDispatchedCorrectly() throws Exception {
		dispatchMessageToAdapter("Message");
	}

	private <T> void dispatchMessageToAdapter(String name, String... parameters) throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<T> queue = new ArrayBlockingQueue<>(1);
			DispatchingFcpAdapter fcpListener = new DispatchingFcpAdapter();
			fcpListener.addListener(name, queue::add);
			fcpConnection.addFcpListener(fcpListener);
			writeMessageToSocket(name, parameters);
			assertThat(queue.take(), notNullValue());
		});
	}

	private void writeMessageToSocket(String messageName, String... parameters) throws IOException {
		writeMessageToSocket(messageName, null, parameters);
	}

	private void writeMessageToSocket(String messageName, byte[] data, String... parameters) throws IOException {
		try (OutputStream socketOutputStream = localServer.getSocket().getOutputStream();
		     Writer socketWriter = new OutputStreamWriter(socketOutputStream, UTF_8)) {
			socketWriter.write(messageName + "\n");
			for (String parameter : parameters) {
				socketWriter.write(parameter + "\n");
			}
			if (data != null) {
				socketWriter.write("Data\n");
				socketWriter.flush();
				socketOutputStream.write(data);
			} else {
				socketWriter.write("EndMessage\n");
			}
		}
	}

	public interface ThrowingConsumer<T, E extends Exception> {
		void accept(T t) throws E;
	}

	private void runWithConnection(ThrowingConsumer<FcpConnection, ?> action) throws Exception {
		try (FcpConnection fcpConnection = new DefaultFcpConnection("localhost", localServer.getPort())) {
			fcpConnection.connect();
			action.accept(fcpConnection);
		}
	}

	@Rule
	public final LocalServer localServer = new LocalServer();

	@Rule
	public final Timeout timeout = Timeout.seconds(5);

}
