package net.pterodactylus.fcp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class DispatchingFcpAdapterTest {

	@Test
	public void nodeHelloIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("NodeHello", new NodeHello(null), (adapter, message) -> adapter.receivedNodeHello(null, message));
	}

	@Test
	public void closedConnectionDuplicateClientNameIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("CloseConnectionDuplicateClientName", new CloseConnectionDuplicateClientName(null), (adapter, message) -> adapter.receivedCloseConnectionDuplicateClientName(null, message));
	}

	@Test
	public void sskKeypairIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("SSKKeypair", new SSKKeypair(null), (adapter, message) -> adapter.receivedSSKKeypair(null, message));
	}

	@Test
	public void peerIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("Peer", new Peer(null), (adapter, message) -> adapter.receivedPeer(null, message));
	}

	@Test
	public void endListPeersIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("EndListPeers", new EndListPeers(null), (adapter, message) -> adapter.receivedEndListPeers(null, message));
	}

	@Test
	public void peerNoteIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PeerNote", new PeerNote(null), (adapter, message) -> adapter.receivedPeerNote(null, message));
	}

	@Test
	public void endListPeerNotesIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("EndListPeerNotes", new EndListPeerNotes(null), (adapter, message) -> adapter.receivedEndListPeerNotes(null, message));
	}

	@Test
	public void peerRemovedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PeerRemoved", new PeerRemoved(null), (adapter, message) -> adapter.receivedPeerRemoved(null, message));
	}

	@Test
	public void nodeDataIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("NodeData", createNodeDataMessage(), (adapter, message) -> adapter.receivedNodeData(null, message));
	}

	private NodeData createNodeDataMessage() {
		FcpMessage receivedMessage = new FcpMessage("");
		receivedMessage.setField("ark.pubURI", "");
		receivedMessage.setField("ark.number", "0");
		receivedMessage.setField("auth.negTypes", "0");
		receivedMessage.setField("version", "0,1,2,3");
		receivedMessage.setField("lastGoodVersion", "0,1,2,3");
		return new NodeData(receivedMessage);
	}

	@Test
	public void testDDAReplyIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("TestDDAReply", new TestDDAReply(null), (adapter, message) -> adapter.receivedTestDDAReply(null, message));
	}

	@Test
	public void testDDACompleteIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("TestDDAComplete", new TestDDAComplete(null), (adapter, message) -> adapter.receivedTestDDAComplete(null, message));
	}

	@Test
	public void persistentGetIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PersistentGet", new PersistentGet(null), (adapter, message) -> adapter.receivedPersistentGet(null, message));
	}

	@Test
	public void persistentPutIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PersistentPut", new PersistentPut(null), (adapter, message) -> adapter.receivedPersistentPut(null, message));
	}

	@Test
	public void endListPersistentRequestsIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("EndListPersistentRequests", new EndListPersistentRequests(null), (adapter, message) -> adapter.receivedEndListPersistentRequests(null, message));
	}

	@Test
	public void uriGeneratedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("URIGenerated", new URIGenerated(null), (adapter, message) -> adapter.receivedURIGenerated(null, message));
	}

	@Test
	public void dataFoundIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("DataFound", new DataFound(null), (adapter, message) -> adapter.receivedDataFound(null, message));
	}

	@Test
	public void allDataIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("AllData", new AllData(null, null), (adapter, message) -> adapter.receivedAllData(null, message));
	}

	@Test
	public void simpleProgressIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("SimpleProgress", new SimpleProgress(null), (adapter, message) -> adapter.receivedSimpleProgress(null, message));
	}

	@Test
	public void startedCompressionIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("StartedCompression", new StartedCompression(null), (adapter, message) -> adapter.receivedStartedCompression(null, message));
	}

	@Test
	public void finishedCompressionIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("FinishedCompression", new FinishedCompression(null), (adapter, message) -> adapter.receivedFinishedCompression(null, message));
	}

	@Test
	public void unknownPeerNoteTypeIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("UnknownPeerNoteType", new UnknownPeerNoteType(null), (adapter, message) -> adapter.receivedUnknownPeerNoteType(null, message));
	}

	@Test
	public void unknownNodeIdentifierIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("UnknownNodeIdentifier", new UnknownNodeIdentifier(null), (adapter, message) -> adapter.receivedUnknownNodeIdentifier(null, message));
	}

	@Test
	public void configDataIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("ConfigData", new ConfigData(null), (adapter, message) -> adapter.receivedConfigData(null, message));
	}

	@Test
	public void getFailedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("GetFailed", new GetFailed(null), (adapter, message) -> adapter.receivedGetFailed(null, message));
	}

	@Test
	public void putFailedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PutFailed", new PutFailed(null), (adapter, message) -> adapter.receivedPutFailed(null, message));
	}

	@Test
	public void identifierCollisionIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("IdentifierCollision", new IdentifierCollision(null), (adapter, message) -> adapter.receivedIdentifierCollision(null, message));
	}

	@Test
	public void persistentPutDirIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PersistentPutDir", new PersistentPutDir(null), (adapter, message) -> adapter.receivedPersistentPutDir(null, message));
	}

	@Test
	public void persistentRequestRemovedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PersistentRequestRemoved", new PersistentRequestRemoved(null), (adapter, message) -> adapter.receivedPersistentRequestRemoved(null, message));
	}

	@Test
	public void subscribedUSKIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("SubscribedUSK", new SubscribedUSK(null), (adapter, message) -> adapter.receivedSubscribedUSK(null, message));
	}

	@Test
	public void subscribedUSKUpdateIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("SubscribedUSKUpdate", new SubscribedUSKUpdate(null), (adapter, message) -> adapter.receivedSubscribedUSKUpdate(null, message));
	}

	@Test
	public void pluginInfoIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PluginInfo", new PluginInfo(null), (adapter, message) -> adapter.receivedPluginInfo(null, message));
	}

	@Test
	public void pluginRemovedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PluginRemoved", new PluginRemoved(null), (adapter, message) -> adapter.receivedPluginRemoved(null, message));
	}

	@Test
	public void fcpPluginReplyIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("FCPPluginReply", new FCPPluginReply(null, null), (adapter, message) -> adapter.receivedFCPPluginReply(null, message));
	}

	@Test
	public void persistentRequestModifiedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PersistentRequestModified", new PersistentRequestModified(null), (adapter, message) -> adapter.receivedPersistentRequestModified(null, message));
	}

	@Test
	public void putSuccessfulIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PutSuccessful", new PutSuccessful(null), (adapter, message) -> adapter.receivedPutSuccessful(null, message));
	}

	@Test
	public void putFetchableIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("PutFetchable", new PutFetchable(null), (adapter, message) -> adapter.receivedPutFetchable(null, message));
	}

	@Test
	public void sentFeedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("SentFeed", new SentFeed(null), (adapter, message) -> adapter.receivedSentFeed(null, message));
	}

	@Test
	public void receivedBookmarkFeedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("ReceivedBookmarkFeed", new ReceivedBookmarkFeed(null), (adapter, message) -> adapter.receivedBookmarkFeed(null, message));
	}

	@Test
	public void protocolErrorIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("ProtocolError", new ProtocolError(null), (adapter, message) -> adapter.receivedProtocolError(null, message));
	}

	@Test
	public void messageIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("Message", new FcpMessage(null), (adapter, message) -> adapter.receivedMessage(null, message));
	}

	@Test
	public void connectionClosedIsDispatchedCorrectly() {
		dispatchMessageAndVerifyDispatch("ConnectionClosed", new Exception("Test"), (adapter, throwable) -> adapter.connectionClosed(null, throwable));
	}

	@Test
	public void multipleListenersCanBeAttachedToOneMessage() {
		DispatchingFcpAdapter adapter = new DispatchingFcpAdapter();
		List<Object> receivedMessages = new ArrayList<>();
		adapter.addListener("Message", receivedMessages::add);
		adapter.addListener("Message", receivedMessages::add);
		FcpMessage message = new FcpMessage("Test");
		adapter.receivedMessage(null, message);
		assertThat(receivedMessages, contains(message, message));
	}

	private <T> void dispatchMessageAndVerifyDispatch(String messageName, T message, BiConsumer<FcpListener, T> messageDispatcher) {
		DispatchingFcpAdapter adapter = new DispatchingFcpAdapter();
		AtomicReference<Object> receivedMessage = new AtomicReference<>();
		adapter.addListener(messageName, receivedMessage::set);
		messageDispatcher.accept(adapter, message);
		assertThat(receivedMessage.get(), equalTo(message));
	}

}
