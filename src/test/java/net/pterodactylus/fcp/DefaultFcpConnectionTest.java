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
		runWithConnection(fcpConnection -> {
			BlockingQueue<CloseConnectionDuplicateClientName> receivedClosedConnectionDuplicateClientName = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
					receivedClosedConnectionDuplicateClientName.add(closeConnectionDuplicateClientName);
				}
			});
			writeMessageToSocket("CloseConnectionDuplicateClientName", "Test=yes");
			assertThat(receivedClosedConnectionDuplicateClientName.take().getField("Test"), equalTo("yes"));
		});
	}

	@Test
	public void nodeHelloIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<NodeHello> receivedNodeHello = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
					receivedNodeHello.add(nodeHello);
				}
			});
			writeMessageToSocket("NodeHello", "Version=1.2.3");
			assertThat(receivedNodeHello.take().getVersion(), equalTo("1.2.3"));
		});
	}

	@Test
	public void protocolErrorIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<ProtocolError> receivedProtocolError = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
					receivedProtocolError.add(protocolError);
				}
			});
			writeMessageToSocket("ProtocolError", "Identifier=TestRequest", "Code=404", "CodeDescription=Error not found", "ExtraDescription=TestError", "Fatal=true", "Global=true");
			ProtocolError protocolError = receivedProtocolError.take();
			assertThat(protocolError.getIdentifier(), equalTo("TestRequest"));
			assertThat(protocolError.getCode(), equalTo(404));
			assertThat(protocolError.getCodeDescription(), equalTo("Error not found"));
			assertThat(protocolError.getExtraDescription(), equalTo("TestError"));
			assertThat(protocolError.isFatal(), equalTo(true));
			assertThat(protocolError.isGlobal(), equalTo(true));
		});
	}

	@Test
	public void persistentGetIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<PersistentGet> receivedPersistentGet = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
					receivedPersistentGet.add(persistentGet);
				}
			});
			writeMessageToSocket("PersistentGet", "Identifier=TestRequest", "URI=KSK@test-uri", "ClientToken=client-token", "Filename=filename", "TempFilename=/tmp/filename", "Persistence=connection", "PriorityClass=1", "ReturnType=direct", "MaxRetries=82", "Verbosity=999", "Global=true");
			PersistentGet persistentGet = receivedPersistentGet.take();
			assertThat(persistentGet.getIdentifier(), equalTo("TestRequest"));
			assertThat(persistentGet.getURI(), equalTo("KSK@test-uri"));
			assertThat(persistentGet.getClientToken(), equalTo("client-token"));
			assertThat(persistentGet.getFilename(), equalTo("filename"));
			assertThat(persistentGet.getTempFilename(), equalTo("/tmp/filename"));
			assertThat(persistentGet.getPersistence(), equalTo(Persistence.connection));
			assertThat(persistentGet.getPriority(), equalTo(Priority.interactive));
			assertThat(persistentGet.getReturnType(), equalTo(ReturnType.direct));
			assertThat(persistentGet.getMaxRetries(), equalTo(82));
			assertThat(persistentGet.getVerbosity().toString(), equalTo("999"));
			assertThat(persistentGet.isGlobal(), equalTo(true));
		});
	}

	@Test
	public void persistentPutIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<PersistentPut> receivedPersistentPut = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
					receivedPersistentPut.add(persistentPut);
				}
			});
			writeMessageToSocket("PersistentPut", "Identifier=TestRequest", "URI=KSK@test-uri", "PrivateURI=KSK@private-test-uri", "Verbosity=999", "PriorityClass=2", "UploadFrom=direct", "Persistence=reboot", "Filename=filename.dat", "TargetURI=KSK@target-uri", "Metadata.ContentType=application/x-test-data", "Global=true", "DataLength=2345", "ClientToken=client-token", "Started=true", "MaxRetries=83", "TargetFilename=target.dat", "BinaryBlob=true", "CompatibilityMode=COMPAT_CURRENT", "DontCompress=false", "Codecs=codec1,codec2", "RealTime=true", "SplitfileCryptoKey=splitfile-crypto-key");
			PersistentPut persistentPut = receivedPersistentPut.take();
			assertThat(persistentPut.getIdentifier(), equalTo("TestRequest"));
			assertThat(persistentPut.getURI(), equalTo("KSK@test-uri"));
			assertThat(persistentPut.getPrivateURI(), equalTo("KSK@private-test-uri"));
			assertThat(persistentPut.getVerbosity().toString(), equalTo("999"));
			assertThat(persistentPut.getPriority(), equalTo(Priority.immediateSplitfile));
			assertThat(persistentPut.getUploadFrom(), equalTo(UploadFrom.direct));
			assertThat(persistentPut.getPersistence(), equalTo(Persistence.reboot));
			assertThat(persistentPut.getFilename(), equalTo("filename.dat"));
			assertThat(persistentPut.getTargetURI(), equalTo("KSK@target-uri"));
			assertThat(persistentPut.getMetadataContentType(), equalTo("application/x-test-data"));
			assertThat(persistentPut.isGlobal(), equalTo(true));
			assertThat(persistentPut.getDataLength(), equalTo(2345L));
			assertThat(persistentPut.getClientToken(), equalTo("client-token"));
			assertThat(persistentPut.isStarted(), equalTo(true));
			assertThat(persistentPut.getMaxRetries(), equalTo(83));
			assertThat(persistentPut.getTargetFilename(), equalTo("target.dat"));
			assertThat(persistentPut.isBinaryBlob(), equalTo(true));
			assertThat(persistentPut.getCompatibilityMode(), equalTo("COMPAT_CURRENT"));
			assertThat(persistentPut.isDontCompress(), equalTo(false));
			assertThat(persistentPut.getCodecs(), equalTo("codec1,codec2"));
			assertThat(persistentPut.isRealTime(), equalTo(true));
			assertThat(persistentPut.getSplitfileCryptoKey(), equalTo("splitfile-crypto-key"));
		});
	}

	@Test
	public void simpleProgressIsDispatchedCorrectly() throws Exception {
		runWithConnection(fcpConnection -> {
			BlockingQueue<SimpleProgress> receivedSimpleProgress = new ArrayBlockingQueue<>(1);
			fcpConnection.addFcpListener(new FcpAdapter() {
				@Override
				public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
					receivedSimpleProgress.add(simpleProgress);
				}
			});
			writeMessageToSocket("SimpleProgress", "Identifier=TestRequest", "Global=true", "LastProgress=2000", "FinalizedTotal=true", "MinSuccessFetchBlocks=750", "Total=1000", "Required=500", "Failed=38", "FatallyFailed=0", "Succeeded=347");
			SimpleProgress simpleProgress = receivedSimpleProgress.take();
			assertThat(simpleProgress.getIdentifier(), equalTo("TestRequest"));
			assertThat(simpleProgress.getLastProgress(), equalTo(2000L));
			assertThat(simpleProgress.isFinalizedTotal(), equalTo(true));
			assertThat(simpleProgress.getMinSuccessFetchBlocks(), equalTo(750));
			assertThat(simpleProgress.getTotal(), equalTo(1000));
			assertThat(simpleProgress.getRequired(), equalTo(500));
			assertThat(simpleProgress.getFailed(), equalTo(38));
			assertThat(simpleProgress.getFatallyFailed(), equalTo(0));
			assertThat(simpleProgress.getSucceeded(), equalTo(347));
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
