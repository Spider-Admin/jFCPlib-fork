package net.pterodactylus.fcp;

import net.pterodactylus.fcp.test.NodeRefs;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

public class FcpListenerManagerTest {

	@Test
	public void listenerCanBeAdded() {
		DispatchingFcpAdapter fcpListener = new DispatchingFcpAdapter();
		AtomicReference<NodeHello> receivedNodeHello = new AtomicReference<>();
		fcpListener.addListener("NodeHello", receivedNodeHello::set);
		fcpListenerManager.addListener(fcpListener);
		NodeHello nodeHello = new NodeHello(null);
		fcpListenerManager.fireReceivedNodeHello(nodeHello);
		assertThat(receivedNodeHello.get(), sameInstance(nodeHello));
	}

	@Test
	public void listenerCanBeRemoved() {
		DispatchingFcpAdapter fcpListener = new DispatchingFcpAdapter();
		AtomicBoolean nodeHelloReceived = new AtomicBoolean();
		fcpListener.addListener("NodeHello", nodeHello -> nodeHelloReceived.set(true));
		fcpListenerManager.addListener(fcpListener);
		fcpListenerManager.removeListener(fcpListener);
		fcpListenerManager.fireReceivedNodeHello(new NodeHello(null));
		assertThat(nodeHelloReceived.get(), equalTo(false));
	}

	@Test
	public void nodeHelloIsForwardedToListeners() {
		runTestForListenerMethod("NodeHello", () -> new NodeHello(null), fcpListenerManager::fireReceivedNodeHello);
	}

	@Test
	public void closeConnectionDuplicateClientNameIsForwardedToListeners() {
		runTestForListenerMethod("CloseConnectionDuplicateClientName", () -> new CloseConnectionDuplicateClientName(null), fcpListenerManager::fireReceivedCloseConnectionDuplicateClientName);
	}

	@Test
	public void sskKeypairIsForwardedToListeners() {
		runTestForListenerMethod("SSKKeypair", () -> new SSKKeypair(null), fcpListenerManager::fireReceivedSSKKeypair);
	}

	@Test
	public void peerIsForwardedToListeners() {
		runTestForListenerMethod("Peer", () -> new Peer(null), fcpListenerManager::fireReceivedPeer);
	}

	@Test
	public void endListPeersIsForwardedToListeners() {
		runTestForListenerMethod("EndListPeers", () -> new EndListPeers(null), fcpListenerManager::fireReceivedEndListPeers);
	}

	@Test
	public void peerNoteIsForwardedToListeners() {
		runTestForListenerMethod("PeerNote", () -> new PeerNote(null), fcpListenerManager::fireReceivedPeerNote);
	}

	@Test
	public void endListPeerNotesIsForwardedToListeners() {
		runTestForListenerMethod("EndListPeerNotes", () -> new EndListPeerNotes(null), fcpListenerManager::fireReceivedEndListPeerNotes);
	}

	@Test
	public void peerRemovedIsForwardedToListeners() {
		runTestForListenerMethod("PeerRemoved", () -> new PeerRemoved(null), fcpListenerManager::fireReceivedPeerRemoved);
	}

	@Test
	public void nodeDataIsForwardedToListeners() {
		FcpMessage baseMessage = new FcpMessage("");
		NodeRefs.copyNodeRefToMessage(NodeRefs.createNodeRef()).accept(baseMessage);
		runTestForListenerMethod("NodeData", () -> new NodeData(baseMessage), fcpListenerManager::fireReceivedNodeData);
	}

	@Test
	public void testDDAReplyIsForwardedToListeners() {
		runTestForListenerMethod("TestDDAReply", () -> new TestDDAReply(null), fcpListenerManager::fireReceivedTestDDAReply);
	}

	@Test
	public void testDDACompleteIsForwardedToListeners() {
		runTestForListenerMethod("TestDDAComplete", () -> new TestDDAComplete(null), fcpListenerManager::fireReceivedTestDDAComplete);
	}

	@Test
	public void persistentGetIsForwardedToListeners() {
		runTestForListenerMethod("PersistentGet", () -> new PersistentGet(null), fcpListenerManager::fireReceivedPersistentGet);
	}

	@Test
	public void persistentPutIsForwardedToListeners() {
		runTestForListenerMethod("PersistentPut", () -> new PersistentPut(null), fcpListenerManager::fireReceivedPersistentPut);
	}

	@Test
	public void endListPersistentRequestsIsForwardedToListeners() {
		runTestForListenerMethod("EndListPersistentRequests", () -> new EndListPersistentRequests(null), fcpListenerManager::fireReceivedEndListPersistentRequests);
	}

	@Test
	public void uriGeneratedIsForwardedToListeners() {
		runTestForListenerMethod("URIGenerated", () -> new URIGenerated(null), fcpListenerManager::fireReceivedURIGenerated);
	}

	@Test
	public void dataFoundIsForwardedToListeners() {
		runTestForListenerMethod("DataFound", () -> new DataFound(null), fcpListenerManager::fireReceivedDataFound);
	}

	@Test
	public void allDataIsForwardedToListeners() {
		runTestForListenerMethod("AllData", () -> new AllData(null, null), fcpListenerManager::fireReceivedAllData);
	}

	@Test
	public void simpleProgressIsForwardedToListeners() {
		runTestForListenerMethod("SimpleProgress", () -> new SimpleProgress(null), fcpListenerManager::fireReceivedSimpleProgress);
	}

	@Test
	public void startedCompressionIsForwardedToListeners() {
		runTestForListenerMethod("StartedCompression", () -> new StartedCompression(null), fcpListenerManager::fireReceivedStartedCompression);
	}

	@Test
	public void finishedCompressionIsForwardedToListeners() {
		runTestForListenerMethod("FinishedCompression", () -> new FinishedCompression(null), fcpListenerManager::fireReceivedFinishedCompression);
	}

	@Test
	public void unknownPeerNoteTypeIsForwardedToListeners() {
		runTestForListenerMethod("UnknownPeerNoteType", () -> new UnknownPeerNoteType(null), fcpListenerManager::fireReceivedUnknownPeerNoteType);
	}

	@Test
	public void unknownNodeIdentifierIsForwardedToListeners() {
		runTestForListenerMethod("UnknownNodeIdentifier", () -> new UnknownNodeIdentifier(null), fcpListenerManager::fireReceivedUnknownNodeIdentifier);
	}

	@Test
	public void configDataIsForwardedToListeners() {
		runTestForListenerMethod("ConfigData", () -> new ConfigData(null), fcpListenerManager::fireReceivedConfigData);
	}

	@Test
	public void getFailedIsForwardedToListeners() {
		runTestForListenerMethod("GetFailed", () -> new GetFailed(null), fcpListenerManager::fireReceivedGetFailed);
	}

	@Test
	public void putFailedIsForwardedToListeners() {
		runTestForListenerMethod("PutFailed", () -> new PutFailed(null), fcpListenerManager::fireReceivedPutFailed);
	}

	@Test
	public void identifierCollisionIsForwardedToListeners() {
		runTestForListenerMethod("IdentifierCollision", () -> new IdentifierCollision(null), fcpListenerManager::fireReceivedIdentifierCollision);
	}

	@Test
	public void persistentPutDirIsForwardedToListeners() {
		runTestForListenerMethod("PersistentPutDir", () -> new PersistentPutDir(null), fcpListenerManager::fireReceivedPersistentPutDir);
	}

	@Test
	public void persistentRequestRemovedIsForwardedToListeners() {
		runTestForListenerMethod("PersistentRequestRemoved", () -> new PersistentRequestRemoved(null), fcpListenerManager::fireReceivedPersistentRequestRemoved);
	}

	@Test
	public void subscribedUSKIsForwardedToListeners() {
		runTestForListenerMethod("SubscribedUSK", () -> new SubscribedUSK(null), fcpListenerManager::fireReceivedSubscribedUSK);
	}

	@Test
	public void subscribedUSKUpdateIsForwardedToListeners() {
		runTestForListenerMethod("SubscribedUSKUpdate", () -> new SubscribedUSKUpdate(null), fcpListenerManager::fireReceivedSubscribedUSKUpdate);
	}

	@Test
	public void pluginInfoIsForwardedToListeners() {
		runTestForListenerMethod("PluginInfo", () -> new PluginInfo(null), fcpListenerManager::fireReceivedPluginInfo);
	}

	@Test
	public void pluginRemovedIsForwardedToListeners() {
		runTestForListenerMethod("PluginRemoved", () -> new PluginRemoved(null), fcpListenerManager::fireReceivedPluginRemoved);
	}

	@Test
	public void fcpPluginReplyIsForwardedToListeners() {
		runTestForListenerMethod("FCPPluginReply", () -> new FCPPluginReply(null, null), fcpListenerManager::fireReceivedFCPPluginReply);
	}

	@Test
	public void persistentRequestModifiedIsForwardedToListeners() {
		runTestForListenerMethod("PersistentRequestModified", () -> new PersistentRequestModified(null), fcpListenerManager::fireReceivedPersistentRequestModified);
	}

	@Test
	public void putSuccessfulIsForwardedToListeners() {
		runTestForListenerMethod("PutSuccessful", () -> new PutSuccessful(null), fcpListenerManager::fireReceivedPutSuccessful);
	}

	@Test
	public void putFetchableIsForwardedToListeners() {
		runTestForListenerMethod("PutFetchable", () -> new PutFetchable(null), fcpListenerManager::fireReceivedPutFetchable);
	}

	@Test
	public void protocolErrorIsForwardedToListeners() {
		runTestForListenerMethod("ProtocolError", () -> new ProtocolError(null), fcpListenerManager::fireReceivedProtocolError);
	}

	@Test
	public void sentFeedIsForwardedToListeners() {
		runTestForListenerMethod("SentFeed", () -> new SentFeed(null), fcpListenerManager::fireReceivedSentFeed);
	}

	@Test
	public void receivedBookmarkFeedIsForwardedToListeners() {
		runTestForListenerMethod("ReceivedBookmarkFeed", () -> new ReceivedBookmarkFeed(null), fcpListenerManager::fireReceivedBookmarkFeed);
	}

	@Test
	public void receivedMessageIsForwardedToListeners() {
		runTestForListenerMethod("Message", () -> new FcpMessage(null), fcpListenerManager::fireMessageReceived);
	}

	@Test
	public void connectionClosedIsForwardedToListeners() {
		runTestForListenerMethod("ConnectionClosed", Throwable::new, fcpListenerManager::fireConnectionClosed);
	}

	private <T> void runTestForListenerMethod(String eventName, Supplier<T> creator, Consumer<T> listener) {
		AtomicReference<T> firstReceivedCloseConnectionDuplicateClientName = createContainerAndAddListener(eventName);
		AtomicReference<T> secondReceivedCloseConnectionDuplicateClientName = createContainerAndAddListener(eventName);
		T closeConnectionDuplicateClientName = creator.get();
		listener.accept(closeConnectionDuplicateClientName);
		assertThat(firstReceivedCloseConnectionDuplicateClientName.get(), sameInstance(closeConnectionDuplicateClientName));
		assertThat(secondReceivedCloseConnectionDuplicateClientName.get(), sameInstance(closeConnectionDuplicateClientName));
	}

	private <T> AtomicReference<T> createContainerAndAddListener(String event) {
		AtomicReference<T> receivedEventObject = new AtomicReference<>();
		DispatchingFcpAdapter firstListener = new DispatchingFcpAdapter();
		firstListener.addListener(event, receivedEventObject::set);
		fcpListenerManager.addListener(firstListener);
		return receivedEventObject;
	}

	private final FcpListenerManager fcpListenerManager = new FcpListenerManager(null);

}
