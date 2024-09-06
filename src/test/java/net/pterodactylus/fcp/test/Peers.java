package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.Peer;

import java.util.function.Consumer;

public class Peers {

	public static Peer createPeer() {
		return createPeer(m -> {
		});
	}

	public static Consumer<FcpMessage> addVolatileFields() {
		return peerMessage -> {
			peerMessage.setField("volatile.a", "A1");
			peerMessage.setField("volatile.b", "B2");
			peerMessage.setField("volatile.c", "C3");
		};
	}

	public static Consumer<FcpMessage> addMetadataFields() {
		return peerMessage -> {
			peerMessage.setField("metadata.a", "MA1");
			peerMessage.setField("metadata.b", "MB2");
			peerMessage.setField("metadata.c", "MC3");
		};
	}

	public static Peer createPeer(Consumer<FcpMessage> messageModifier) {
		FcpMessage peerMessage = new FcpMessage("Peer");
		peerMessage.setField("Identifier", "identifier");
		peerMessage.setField("ark.number", "123");
		peerMessage.setField("ark.privURI", "ark-private-uri");
		peerMessage.setField("ark.pubURI", "ark-public-uri");
		peerMessage.setField("auth.negTypes", "1;2;3");
		peerMessage.setField("dsaGroup.g", "dsa-group-base");
		peerMessage.setField("dsaGroup.p", "dsa-group-prime");
		peerMessage.setField("dsaGroup.q", "dsa-group-subprime");
		peerMessage.setField("dsaPubKey.y", "dsa-public-key");
		peerMessage.setField("identity", "identity");
		peerMessage.setField("lastGoodVersion", "Node,0.1.2,1.2.3,234");
		peerMessage.setField("location", "0.4");
		peerMessage.setField("myName", "Test Node");
		peerMessage.setField("physical.udp", "physical-udp");
		peerMessage.setField("sig", "signature");
		peerMessage.setField("version", "TestNode,1.2.3,2.3.4,345");
		messageModifier.accept(peerMessage);
		return new Peer(peerMessage);
	}

}
