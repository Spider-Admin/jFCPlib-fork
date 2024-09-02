package net.pterodactylus.fcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * {@link FcpListener} implementation that dispatches messages to event
 * listeners that are really just {@link Consumer}s. All methods named
 * “receivedX” from the {@link FcpListener} interface can be dispatched by
 * calling {@link #addListener(String, Consumer)} with “X” as the message
 * name. Registering a listener for the method
 * {@link FcpListener#connectionClosed(FcpConnection, Throwable)} uses
 * {@code "ConnectionClosed"} as message name, and the given consumer
 * will receive the throwable.
 */
public class DispatchingFcpAdapter implements FcpListener {

	public <T> void addListener(String message, Consumer<T> eventListener) {
		eventListeners.merge(message, singletonList((Consumer<Object>) eventListener), (l1, l2) -> {
			List<Consumer<? super Object>> merged = new ArrayList<>(l1);
			merged.addAll(l2);
			return merged;
		});
	}

	@Override
	public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
		dispatchEvent("NodeHello", nodeHello);
	}

	@Override
	public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		dispatchEvent("CloseConnectionDuplicateClientName", closeConnectionDuplicateClientName);
	}

	@Override
	public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
		dispatchEvent("SSKKeypair", sskKeypair);
	}

	@Override
	public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
		dispatchEvent("Peer", peer);
	}

	@Override
	public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		dispatchEvent("EndListPeers", endListPeers);
	}

	@Override
	public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		dispatchEvent("PeerNote", peerNote);
	}

	@Override
	public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		dispatchEvent("EndListPeerNotes", endListPeerNotes);
	}

	@Override
	public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		dispatchEvent("PeerRemoved", peerRemoved);
	}

	@Override
	public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
		dispatchEvent("NodeData", nodeData);
	}

	@Override
	public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		dispatchEvent("TestDDAReply", testDDAReply);
	}

	@Override
	public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		dispatchEvent("TestDDAComplete", testDDAComplete);
	}

	@Override
	public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		dispatchEvent("PersistentGet", persistentGet);
	}

	@Override
	public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		dispatchEvent("PersistentPut", persistentPut);
	}

	@Override
	public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
		dispatchEvent("EndListPersistentRequests", endListPersistentRequests);
	}

	@Override
	public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		dispatchEvent("URIGenerated", uriGenerated);
	}

	@Override
	public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		dispatchEvent("DataFound", dataFound);
	}

	@Override
	public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		dispatchEvent("AllData", allData);
	}

	@Override
	public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		dispatchEvent("SimpleProgress", simpleProgress);
	}

	@Override
	public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		dispatchEvent("StartedCompression", startedCompression);
	}

	@Override
	public void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		dispatchEvent("FinishedCompression", finishedCompression);
	}

	@Override
	public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		dispatchEvent("UnknownPeerNoteType", unknownPeerNoteType);
	}

	@Override
	public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
		dispatchEvent("UnknownNodeIdentifier", unknownNodeIdentifier);
	}

	@Override
	public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		dispatchEvent("ConfigData", configData);
	}

	@Override
	public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		dispatchEvent("GetFailed", getFailed);
	}

	@Override
	public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		dispatchEvent("PutFailed", putFailed);
	}

	@Override
	public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		dispatchEvent("IdentifierCollision", identifierCollision);
	}

	@Override
	public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		dispatchEvent("PersistentPutDir", persistentPutDir);
	}

	@Override
	public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
		dispatchEvent("PersistentRequestRemoved", persistentRequestRemoved);
	}

	@Override
	public void receivedSubscribedUSK(FcpConnection fcpConnection, SubscribedUSK subscribedUSK) {
		dispatchEvent("SubscribedUSK", subscribedUSK);
	}

	@Override
	public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		dispatchEvent("SubscribedUSKUpdate", subscribedUSKUpdate);
	}

	@Override
	public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		dispatchEvent("PluginInfo", pluginInfo);
	}

	@Override
	public void receivedPluginRemoved(FcpConnection fcpConnection, PluginRemoved pluginRemoved) {
		dispatchEvent("PluginRemoved", pluginRemoved);
	}

	@Override
	public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		dispatchEvent("FCPPluginReply", fcpPluginReply);
	}

	@Override
	public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
		dispatchEvent("PersistentRequestModified", persistentRequestModified);
	}

	@Override
	public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
		dispatchEvent("PutSuccessful", putSuccessful);
	}

	@Override
	public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		dispatchEvent("PutFetchable", putFetchable);
	}

	@Override
	public void receivedSentFeed(FcpConnection source, SentFeed sentFeed) {
		dispatchEvent("SentFeed", sentFeed);
	}

	@Override
	public void receivedBookmarkFeed(FcpConnection fcpConnection, ReceivedBookmarkFeed receivedBookmarkFeed) {
		dispatchEvent("ReceivedBookmarkFeed", receivedBookmarkFeed);
	}

	@Override
	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		dispatchEvent("ProtocolError", protocolError);
	}

	@Override
	public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		dispatchEvent("Message", fcpMessage);
	}

	@Override
	public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
		dispatchEvent("ConnectionClosed", throwable);
	}

	private void dispatchEvent(String name, Object message) {
		eventListeners.getOrDefault(name, emptyList()).forEach(eventListener -> eventListener.accept(message));
	}

	private final Map<String, List<Consumer<Object>>> eventListeners = new HashMap<>();

}
