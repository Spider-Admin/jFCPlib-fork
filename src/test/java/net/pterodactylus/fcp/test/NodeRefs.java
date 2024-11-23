package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.ARK;
import net.pterodactylus.fcp.DSAGroup;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Version;

import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class NodeRefs {

	public static NodeRef createNodeRef() {
		NodeRef nodeRef = new NodeRef();
		nodeRef.setARK(new ARK("public-ark", "1"));
		nodeRef.setLocation(0.4);
		nodeRef.setDSAGroup(new DSAGroup("dsa-base", "dsa-prime", "dsa-subprime"));
		nodeRef.setOpennet(true);
		nodeRef.setDSAPublicKey("dsa-public-key");
		nodeRef.setVersion(new Version("TestNode", "1.2.3", "2.3.4", 345));
		nodeRef.setPhysicalUDP("physical-udp");
		nodeRef.setNegotiationTypes(new int[] { 1, 2, 3 });
		nodeRef.setIdentity("identity");
		nodeRef.setName("name");
		nodeRef.setLastGoodVersion(new Version("ProdNode", "1.0", "2.0", 300));
		nodeRef.setSignature("signature");
		return nodeRef;
	}

	public static Consumer<FcpMessage> copyNodeRefToMessage(NodeRef nodeRef) {
		return message -> {
			message.put("ark.pubURI", nodeRef.getARK().getPublicURI());
			message.put("ark.number", String.valueOf(nodeRef.getARK().getNumber()));
			message.put("location", String.valueOf(nodeRef.getLocation()));
			message.put("dsaGroup.g", nodeRef.getDSAGroup().getBase());
			message.put("dsaGroup.p", nodeRef.getDSAGroup().getPrime());
			message.put("dsaGroup.q", nodeRef.getDSAGroup().getSubprime());
			message.put("opennet", String.valueOf(nodeRef.isOpennet()));
			message.put("dsaPubKey.y", nodeRef.getDSAPublicKey());
			message.put("version", nodeRef.getVersion().toString());
			message.put("physical.udp", nodeRef.getPhysicalUDP());
			message.put("auth.negTypes", stream(nodeRef.getNegotiationTypes()).mapToObj(String::valueOf).collect(joining(";")));
			message.put("identity", nodeRef.getIdentity());
			message.put("name", nodeRef.getMyName());
			message.put("lastGoodVersion", nodeRef.getLastGoodVersion().toString());
			message.put("signature", nodeRef.getSignature());
		};
	}

}
