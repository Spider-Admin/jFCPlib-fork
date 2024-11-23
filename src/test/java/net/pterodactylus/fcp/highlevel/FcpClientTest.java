package net.pterodactylus.fcp.highlevel;

import net.pterodactylus.fcp.AddPeer.Trust;
import net.pterodactylus.fcp.AddPeer.Visibility;
import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.EndListPersistentRequests;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerRemoved;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.UnknownNodeIdentifier;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static net.pterodactylus.fcp.AddPeer.Trust.HIGH;
import static net.pterodactylus.fcp.AddPeer.Trust.LOW;
import static net.pterodactylus.fcp.AddPeer.Trust.NORMAL;
import static net.pterodactylus.fcp.AddPeer.Visibility.NAME_ONLY;
import static net.pterodactylus.fcp.AddPeer.Visibility.NO;
import static net.pterodactylus.fcp.AddPeer.Visibility.YES;
import static net.pterodactylus.fcp.test.InputStreamMatchers.streamContaining;
import static net.pterodactylus.fcp.test.Matchers.hasField;
import static net.pterodactylus.fcp.test.Matchers.matches;
import static net.pterodactylus.fcp.test.NodeRefs.createNodeRef;
import static net.pterodactylus.fcp.test.PeerMatchers.peerWithIdentity;
import static net.pterodactylus.fcp.test.Peers.createPeer;
import static net.pterodactylus.fcp.test.Requests.isGetRequest;
import static net.pterodactylus.fcp.test.Requests.isPutRequest;
import static net.pterodactylus.fcp.test.Requests.isRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

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
	public void getPeersWithMetadataFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getPeers, true, false, contains(hasField("WithMetadata", equalTo("true"))), anything());
	}

	@Test
	public void getPeersWithMetadataFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getPeers, false, false, contains(hasField("WithMetadata", equalTo("false"))), anything());
	}

	@Test
	public void getPeersWithVolatileFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getPeers, false, true, contains(hasField("WithVolatile", equalTo("true"))), anything());
	}

	@Test
	public void getPeersWithVolatileFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getPeers, false, false, contains(hasField("WithVolatile", equalTo("false"))), anything());
	}

	@Test
	public void getPeersReturnsPeersWithCorrectIdentifier() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getPeers, false, false, anything(), containsInAnyOrder(peerWithIdentity(equalTo("1")), peerWithIdentity(equalTo("2")), peerWithIdentity(equalTo("3"))));
	}

	@Test
	public void getDarknetPeersWithMetadataFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getDarknetPeers, true, false, contains(hasField("WithMetadata", equalTo("true"))), anything());
	}

	@Test
	public void getDarknetPeersWithMetadataFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getDarknetPeers, false, false, contains(hasField("WithMetadata", equalTo("false"))), anything());
	}

	@Test
	public void getDarknetPeersWithVolatileFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getDarknetPeers, false, true, contains(hasField("WithVolatile", equalTo("true"))), anything());
	}

	@Test
	public void getDarknetPeersWithVolatileFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getDarknetPeers, false, false, contains(hasField("WithVolatile", equalTo("false"))), anything());
	}

	@Test
	public void getDarknetPeersReturnsPeersWithCorrectIdentifier() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getDarknetPeers, false, false, anything(), contains(peerWithIdentity(equalTo("3"))));
	}

	@Test
	public void getOpennetPeersWithMetadataFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getOpennetPeers, true, false, contains(hasField("WithMetadata", equalTo("true"))), anything());
	}

	@Test
	public void getOpennetPeersWithMetadataFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getOpennetPeers, false, false, contains(hasField("WithMetadata", equalTo("false"))), anything());
	}

	@Test
	public void getOpennetPeersWithVolatileFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getOpennetPeers, false, true, contains(hasField("WithVolatile", equalTo("true"))), anything());
	}

	@Test
	public void getOpennetPeersWithVolatileFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getOpennetPeers, false, false, contains(hasField("WithVolatile", equalTo("false"))), anything());
	}

	@Test
	public void getOpennetPeersReturnsPeersWithCorrectIdentifier() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getOpennetPeers, false, false, anything(), contains(peerWithIdentity(equalTo("1"))));
	}

	@Test
	public void getSeedPeersWithMetadataFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getSeedPeers, true, false, contains(hasField("WithMetadata", equalTo("true"))), anything());
	}

	@Test
	public void getSeedPeersWithMetadataFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getSeedPeers, false, false, contains(hasField("WithMetadata", equalTo("false"))), anything());
	}

	@Test
	public void getSeedPeersWithVolatileFlagSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getSeedPeers, false, true, contains(hasField("WithVolatile", equalTo("true"))), anything());
	}

	@Test
	public void getSeedPeersWithVolatileFlagNotSetSendsCorrectMessage() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getSeedPeers, false, false, contains(hasField("WithVolatile", equalTo("false"))), anything());
	}

	@Test
	public void getSeedPeersReturnsPeersWithCorrectIdentifier() {
		sendListPeersAndVerifySentMessagesAndReturnedPeers(FcpClientTest::getSeedPeers, false, false, anything(), contains(peerWithIdentity(equalTo("2"))));
	}

	private static BiFunction<Boolean, Boolean, Collection<Peer>> getPeers(FcpClient fcpClient) {
		return (withMetadata, withVolatile) -> {
			try {
				return fcpClient.getPeers(withMetadata, withVolatile);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static BiFunction<Boolean, Boolean, Collection<Peer>> getDarknetPeers(FcpClient fcpClient) {
		return (withMetadata, withVolatile) -> {
			try {
				return fcpClient.getDarknetPeers(withMetadata, withVolatile);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static BiFunction<Boolean, Boolean, Collection<Peer>> getOpennetPeers(FcpClient fcpClient) {
		return (withMetadata, withVolatile) -> {
			try {
				return fcpClient.getOpennetPeers(withMetadata, withVolatile);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static BiFunction<Boolean, Boolean, Collection<Peer>> getSeedPeers(FcpClient fcpClient) {
		return (withMetadata, withVolatile) -> {
			try {
				return fcpClient.getSeedPeers(withMetadata, withVolatile);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static void sendListPeersAndVerifySentMessagesAndReturnedPeers(Function<FcpClient, BiFunction<Boolean, Boolean, Collection<Peer>>> peerRetrieval, boolean withMetadataFlag, boolean withVolatileFlag, Matcher<? super Iterable<? extends FcpMessage>> sentMessagesMatcher, Matcher<? super Collection<Peer>> peersMatcher) {
		List<FcpMessage> sentMessages = new ArrayList<>();
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ListPeers")) {
				sentMessages.add(message);
				return (listener, connection) -> {
					String identifier = message.getField("Identifier");
					listener.receivedPeer(connection, new Peer(new FcpMessage("Peer").put("Identifier", identifier).put("identity", "1").put("opennet", "true")));
					listener.receivedPeer(connection, new Peer(new FcpMessage("Peer").put("Identifier", "Other Identifier").put("identity", "4")));
					listener.receivedPeer(connection, new Peer(new FcpMessage("Peer").put("Identifier", identifier).put("identity", "2").put("opennet", "true").put("seed", "true")));
					listener.receivedEndListPeers(connection, new EndListPeers(new FcpMessage("EndListPeers").put("Identifier", "Other Identifier")));
					listener.receivedPeer(connection, new Peer(new FcpMessage("Peer").put("Identifier", identifier).put("identity", "3")));
					listener.receivedEndListPeers(connection, new EndListPeers(new FcpMessage("EndListPeers").put("Identifier", identifier)));
				};
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Peer> peers = peerRetrieval.apply(fcpClient).apply(withMetadataFlag, withVolatileFlag);
			assertThat(sentMessages, sentMessagesMatcher);
			assertThat(peers, peersMatcher);
		}
	}

	@Test
	public void addPeerWithPeerAndTrustLowAndVisibilityNoSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithPeer(createPeer()), LOW, NO);
	}

	@Test
	public void addPeerWithPeerAndTrustNormalAndVisibilityNameOnlySendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithPeer(createPeer()), NORMAL, NAME_ONLY);
	}

	@Test
	public void addPeerWithPeerAndTrustHighAndVisibilityYesSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithPeer(createPeer()), HIGH, YES);
	}

	@Test
	public void addPeerWithNodeRefAndTrustLowAndVisibilityNoSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithNodeRef(createNodeRef()), LOW, NO);
	}

	@Test
	public void addPeerWithNodeRefAndTrustNormalAndVisibilityNameOnlySendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithNodeRef(createNodeRef()), NORMAL, NAME_ONLY);
	}

	@Test
	public void addPeerWithNodeRefAndTrustHighAndVisibilityYesSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithNodeRef(createNodeRef()), HIGH, YES);
	}

	@Test
	public void addPeerWithUrlAndTrustLowAndVisibilityNoSendsCorrectAddPeerMessage() throws Exception {
		addPeerAndVerifySendMessage(addPeerWithUrl(new URL("http://test.test/")), LOW, NO, hasField("URL", equalTo("http://test.test/")));
	}

	@Test
	public void addPeerWithUrlAndTrustNormalAndVisibilityNameOnlySendsCorrectAddPeerMessage() throws Exception {
		addPeerAndVerifySendMessage(addPeerWithUrl(new URL("http://test.test/")), NORMAL, NAME_ONLY, hasField("URL", equalTo("http://test.test/")));
	}

	@Test
	public void addPeerWithUrlAndTrustHighAndVisibilityYesSendsCorrectAddPeerMessage() throws Exception {
		addPeerAndVerifySendMessage(addPeerWithUrl(new URL("http://test.test/")), HIGH, YES, hasField("URL", equalTo("http://test.test/")));
	}

	@Test
	public void addPeerWithFileAndTrustLowAndVisibilityNoSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithFile(), LOW, NO, hasField("File", equalTo("/some/node.ref")));
	}

	@Test
	public void addPeerWithFileAndTrustNormalAndVisibilityNameOnlySendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithFile(), NORMAL, NAME_ONLY, hasField("File", equalTo("/some/node.ref")));
	}

	@Test
	public void addPeerWithFileAndTrustHighAndVisibilityYesSendsCorrectAddPeerMessage() {
		addPeerAndVerifySendMessage(addPeerWithFile(), HIGH, YES, hasField("File", equalTo("/some/node.ref")));
	}

	private static Function<FcpClient, BiConsumer<Trust, Visibility>> addPeerWithPeer(Peer peer) {
		return fcpClient -> (trust, visibility) -> {
			try {
				fcpClient.addPeer(peer, trust, visibility);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static Function<FcpClient, BiConsumer<Trust, Visibility>> addPeerWithNodeRef(NodeRef nodeRef) {
		return fcpClient -> (trust, visibility) -> {
			try {
				fcpClient.addPeer(nodeRef, trust, visibility);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static Function<FcpClient, BiConsumer<Trust, Visibility>> addPeerWithUrl(URL url) {
		return fcpClient -> (trust, visibility) -> {
			try {
				fcpClient.addPeer(url, trust, visibility);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static Function<FcpClient, BiConsumer<Trust, Visibility>> addPeerWithFile() {
		return fcpClient -> (trust, visibility) -> {
			try {
				fcpClient.addPeer("/some/node.ref", trust, visibility);
			} catch (IOException | FcpException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static void addPeerAndVerifySendMessage(Function<FcpClient, BiConsumer<Trust, Visibility>> addPeer, Trust trust, Visibility visibility) {
		addPeerAndVerifySendMessage(addPeer, trust, visibility, anything());
	}

	private static void addPeerAndVerifySendMessage(Function<FcpClient, BiConsumer<Trust, Visibility>> addPeer, Trust trust, Visibility visibility, Matcher<? super FcpMessage> messageMatcher) {
		List<FcpMessage> sentMessages = new ArrayList<>();
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("AddPeer")) {
				sentMessages.add(message);
				return (listener, connection) -> listener.receivedPeer(connection, new Peer(null));
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			addPeer.apply(fcpClient).accept(trust, visibility);
			assertThat(sentMessages, contains(allOf(hasField("Trust", equalTo(trust.name())), hasField("Visibility", equalTo(visibility.name())), messageMatcher)));
		}
	}

	@Test
	public void modifyPeerSendsCorrectMessage() throws Exception {
		List<FcpMessage> sentMessages = new ArrayList<>();
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ModifyPeer")) {
				sentMessages.add(message);
				return (listener, connection) -> listener.receivedPeer(connection, createPeer());
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			fcpClient.modifyPeer(createPeer(), modifyPeer -> modifyPeer.setAllowLocalAddresses(true));
			assertThat(sentMessages, contains(hasField("AllowLocalAddresses", equalTo("true"))));
		}
	}

	@Test
	public void removePeerSendsRemovePeerMessageWithIdentityAsNodeIdentifier() throws Exception {
		List<FcpMessage> sentMessages = new ArrayList<>();
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("RemovePeer")) {
				sentMessages.add(message);
				FcpMessage peerRemovedMessage = new FcpMessage("PeerRemoved");
				peerRemovedMessage.put("NodeIdentifier", message.getField("NodeIdentifier"));
				return (listener, connection) -> listener.receivedPeerRemoved(connection, new PeerRemoved(peerRemovedMessage));
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			fcpClient.removePeer(createPeer());
			assertThat(sentMessages, contains(allOf(hasField("NodeIdentifier", equalTo("identity")))));
		}
	}

	@Test
	public void removePeerWithInvalidNodeIdentifierReturns() throws Exception {
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("RemovePeer")) {
				return (listener, connection) -> listener.receivedUnknownNodeIdentifier(connection, new UnknownNodeIdentifier(message));
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			fcpClient.removePeer(createPeer());
		}
	}

	@Test
	public void removePeerWithInvalidNodeIdentifierIgnoresPositiveResultForDifferentNodeIdentifier() throws Exception {
		removePeerAndVerifyThatNodeIdentifierIsNotBeingIgnored((listener, connection) -> {
			listener.receivedPeerRemoved(connection, new PeerRemoved(new FcpMessage("PeerRemoved").put("NodeIdentifier", "different-node")));
			listener.receivedProtocolError(connection, new ProtocolError(new FcpMessage("ProtocolError").put("Code", "123")));
		});
	}

	@Test
	public void removePeerWithInvalidNodeIdentifierIgnoresNegativeResultForDifferentNodeIdentifier() throws Exception {
		removePeerAndVerifyThatNodeIdentifierIsNotBeingIgnored((listener, connection) -> {
			listener.receivedUnknownNodeIdentifier(connection, new UnknownNodeIdentifier(new FcpMessage("UnknownNodeIdentifier").put("NodeIdentifier", "different-node")));
			listener.receivedProtocolError(connection, new ProtocolError(new FcpMessage("ProtocolError").put("Code", "123")));
		});
	}

	private static void removePeerAndVerifyThatNodeIdentifierIsNotBeingIgnored(BiConsumer<FcpListener, FcpConnection> responseGenerator) {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("RemovePeer"), responseGenerator);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			FcpProtocolException fcpProtocolException = assertThrows(FcpProtocolException.class, () -> fcpClient.removePeer(createPeer()));
			assertThat(fcpProtocolException.getCode(), equalTo(123));
		}
	}

	@Test
	public void getPeerNoteReturnsPeerNote() throws Exception {
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ListPeerNotes")) {
				return (listener, connection) -> {
					FcpMessage receivedMessage = new FcpMessage("PeerNote");
					receivedMessage.setField("NodeIdentifier", message.getField("NodeIdentifier"));
					receivedMessage.setField("PeerNoteType", "1");
					receivedMessage.setField("NoteText", Base64.getEncoder().encodeToString("Peer Note".getBytes(UTF_8)));
					listener.receivedPeerNote(connection, new PeerNote(new FcpMessage("PeerNote").put("NodeIdentifier", "different-node")));
					listener.receivedPeerNote(connection, new PeerNote(receivedMessage));
					listener.receivedEndListPeerNotes(connection, new EndListPeerNotes(new FcpMessage("EndListPeerNotes")));
				};
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			PeerNote peerNote = fcpClient.getPeerNote(createPeer());
			assertThat(peerNote.getNodeIdentifier(), equalTo("identity"));
			assertThat(peerNote.getPeerNoteType(), equalTo(1));
			assertThat(peerNote.getNoteText(), equalTo("Peer Note"));
		}
	}

	@Test
	public void getPeerNoteWithInvalidNodeIdentifierReturnsNull() throws Exception {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPeerNotes"), (listener, connection) -> listener.receivedEndListPeerNotes(connection, new EndListPeerNotes(new FcpMessage("EndListPeerNotes"))));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			PeerNote peerNote = fcpClient.getPeerNote(createPeer());
			assertThat(peerNote, nullValue());
		}
	}

	@Test
	public void modifyPeerNoteSendsCorrectMessage() throws Exception {
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ModifyPeerNote") && message.getField("NoteText").equals("bmV3IG5vdGU=")) {
				return (listener, connection) -> {
					listener.receivedPeerNote(connection, new PeerNote(new FcpMessage("PeerNote").put("NodeIdentifier", message.getField("NodeIdentifier"))));
				};
			}
			return FcpClientTest::doNothing;
		});
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			fcpClient.modifyPeerNote(createPeer(), "new note");
		}
	}

	@Test
	public void modifyPeerNoteWaitsForCorrectPeerNoteReturnMessage() throws Exception {
		FcpConnection fcpConnection = createFcpConnection(message -> {
			if (message.getName().equals("ModifyPeerNote")) {
				return (listener, connection) -> {
					listener.receivedPeerNote(connection, new PeerNote(new FcpMessage("PeerNote").put("NodeIdentifier", "different-node")));
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					listener.receivedPeerNote(connection, new PeerNote(new FcpMessage("PeerNote").put("NodeIdentifier", message.getField("NodeIdentifier"))));
				};
			}
			return FcpClientTest::doNothing;
		});
		Thread thread = new Thread(() -> {
			try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
				fcpClient.modifyPeerNote(createPeer(), "new note");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		thread.start();
		Thread.sleep(1000);
		assertThat(thread.isAlive(), equalTo(true));
	}

	@Test
	public void generatingKeyPairSendsCorrectMessage() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("GenerateSSK"), (listener, connection) -> listener.receivedSSKKeypair(connection, new SSKKeypair(new FcpMessage("SSKKeypair").put("InsertURI", "insert-uri").put("RequestURI", "request-uri"))));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			SSKKeypair keypair = fcpClient.generateKeyPair();
			assertThat(keypair.getInsertURI(), equalTo("insert-uri"));
			assertThat(keypair.getRequestURI(), equalTo("request-uri"));
		}
	}

	@Test
	public void getGetRequestsReturnsGetRequests() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), this::sendRequests);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getGetRequests(false);
			assertThat(requests, contains(isGetRequest(equalTo("get1"))));
		}
	}

	@Test
	public void getPutRequestsReturnsPutRequests() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), this::sendRequests);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getPutRequests(false);
			assertThat(requests, contains(isPutRequest(equalTo("put1")), isPutRequest(equalTo("put2"))));
		}
	}

	@Test
	public void getGetRequestsWithGlobalReturnsGlobalGetRequests() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), this::sendRequests);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getGetRequests(true);
			assertThat(requests, containsInAnyOrder(isGetRequest(equalTo("get1")), isGetRequest(equalTo("get1-global"))));
		}
	}

	@Test
	public void getPutRequestsWithGlobalReturnsGlobalPutRequests() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), this::sendRequests);
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getPutRequests(true);
			assertThat(requests, containsInAnyOrder(
					isPutRequest(equalTo("put1")),
					isPutRequest(equalTo("put2")),
					isPutRequest(equalTo("put1-global")),
					isPutRequest(equalTo("put2-global"))
			));
		}
	}

	@Test
	public void getGetRequestsIsCompleteWhenDataFoundMessageIsReceived() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), sendRequests(this::sendRequests, this::sendDataFound));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getGetRequests(false);
			assertThat(requests, contains(isGetRequest(
					equalTo("get1"),
					matches("complete", Request::isComplete),
					matches("content type", m -> m.getContentType().equals("application/test")),
					matches("data length", m -> m.getLength() == 12345L)
			)));
		}
	}

	@Test
	public void getRequestsIgnoresDataFoundForUnknownIdentifier() throws IOException, FcpException {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), sendRequests(this::sendRequests, this::sendDataFoundForUnknownIdentifier));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getRequests(true);
			assertThat(requests, containsInAnyOrder(
					isRequest(equalTo("get1"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("get1-global"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put1"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put1-global"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put2"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put2-global"), matches("complete", m -> !m.isComplete()))
			));
		}
	}

	private void sendDataFound(FcpListener listener, FcpConnection connection) {
		listener.receivedDataFound(connection, new DataFound(
				new FcpMessage("DataFound").put("Identifier", "get1").put("Metadata.ContentType", "application/test").put("DataLength", "12345"))
		);
	}

	private void sendDataFoundForUnknownIdentifier(FcpListener listener, FcpConnection connection) {
		listener.receivedDataFound(connection, new DataFound(
				new FcpMessage("DataFound").put("Identifier", "unknown").put("Metadata.ContentType", "application/test").put("DataLength", "12345"))
		);
	}

	@Test
	public void getGetRequestIsCompleteAndFailedWhenGetFailedMessageIsReceived() throws Exception {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), sendRequests(this::sendRequests, this::sendGetFailed));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getGetRequests(false);
			assertThat(requests, contains(isGetRequest(
					equalTo("get1"),
					matches("complete", Request::isComplete),
					matches("failed", Request::hasFailed),
					matches("fatal", Request::isFatal),
					matches("error code", m -> m.getErrorCode() == 123)
			)));
		}
	}

	@Test
	public void getRequestIgnoresGetFailedForUnknownIdentifier() throws Exception {
		FcpConnection fcpConnection = createFcpConnectionReactingToSingleMessage(named("ListPersistentRequests"), sendRequests(this::sendRequests, this::sendGetFailedForUnknownIdentifier));
		try (FcpClient fcpClient = new FcpClient(fcpConnection)) {
			Collection<Request> requests = fcpClient.getRequests(true);
			assertThat(requests, containsInAnyOrder(
					isRequest(equalTo("get1"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("get1-global"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put1"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put1-global"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put2"), matches("complete", m -> !m.isComplete())),
					isRequest(equalTo("put2-global"), matches("complete", m -> !m.isComplete()))
			));
		}
	}

	private void sendGetFailed(FcpListener listener, FcpConnection connection) {
		listener.receivedGetFailed(connection, new GetFailed(
				new FcpMessage("GetFailed").put("Identifier", "get1").put("Fatal", "true").put("Code", "123"))
		);
	}

	private void sendGetFailedForUnknownIdentifier(FcpListener listener, FcpConnection connection) {
		listener.receivedGetFailed(connection, new GetFailed(
				new FcpMessage("GetFailed").put("Identifier", "unknown"))
		);
	}

	private void sendRequests(FcpListener listener, FcpConnection connection) {
		listener.receivedPersistentGet(connection, new PersistentGet(new FcpMessage("PersistentGet").put("Identifier", "get1").put("Global", "false")));
		listener.receivedPersistentPut(connection, new PersistentPut(new FcpMessage("PersistentPut").put("Identifier", "put1").put("Global", "false")));
		listener.receivedPersistentPut(connection, new PersistentPut(new FcpMessage("PersistentPut").put("Identifier", "put2").put("Global", "false")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir1").put("Global", "false")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir2").put("Global", "false")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir3").put("Global", "false")));
		listener.receivedPersistentGet(connection, new PersistentGet(new FcpMessage("PersistentGet").put("Identifier", "get1-global").put("Global", "true")));
		listener.receivedPersistentPut(connection, new PersistentPut(new FcpMessage("PersistentPut").put("Identifier", "put1-global").put("Global", "true")));
		listener.receivedPersistentPut(connection, new PersistentPut(new FcpMessage("PersistentPut").put("Identifier", "put2-global").put("Global", "true")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir1-global").put("Global", "true")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir2-global").put("Global", "true")));
		listener.receivedPersistentPutDir(connection, new PersistentPutDir(new FcpMessage("PersistentPutDir").put("Identifier", "putdir3-global").put("Global", "true")));
		listener.receivedEndListPersistentRequests(connection, new EndListPersistentRequests(new FcpMessage("EndListPersistentRequests")));
	}

	@SafeVarargs
	private final BiConsumer<FcpListener, FcpConnection> sendRequests(BiConsumer<FcpListener, FcpConnection>... messageConsumers) {
		return (listener, connection) -> {
			stream(messageConsumers).forEach(consumer -> consumer.accept(listener, connection));
		};
	}

	private static void doNothing(FcpListener listener, FcpConnection connection) {
		// do nothing.
	}

	private static FcpConnection createFcpConnection() {
		return createFcpConnection(m -> FcpClientTest::doNothing);
	}

	private static FcpConnection createFcpConnectionReactingToSingleMessage(Predicate<FcpMessage> messageFilter, BiConsumer<FcpListener, FcpConnection> messageConsumer) {
		return createFcpConnection(message -> {
			if (messageFilter.test(message)) {
				return messageConsumer;
			}
			return FcpClientTest::doNothing;
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
			public void connect() throws IllegalStateException {
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
				BiConsumer<FcpListener, FcpConnection> listenerNotifier = messageConsumer.apply(fcpMessage);
				listeners.forEach(listener -> new Thread(() -> listenerNotifier.accept(listener, this)).start());
			}
		};
	}

	private static Predicate<FcpMessage> named(String name) {
		return message -> message.getName().equals(name);
	}

	@Rule
	public final Timeout timeout = Timeout.seconds(5);

}
