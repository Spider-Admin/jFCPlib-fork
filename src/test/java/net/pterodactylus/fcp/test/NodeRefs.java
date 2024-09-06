package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.ARK;
import net.pterodactylus.fcp.DSAGroup;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Version;

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
		nodeRef.setSignature("signature");
		return nodeRef;
	}

}
