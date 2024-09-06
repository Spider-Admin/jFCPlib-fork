package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.ArkMatchers.isArk;
import static net.pterodactylus.fcp.test.DsaGroupMatchers.isDsaGroup;
import static net.pterodactylus.fcp.test.Peers.addMetadataFields;
import static net.pterodactylus.fcp.test.Peers.addVolatileFields;
import static net.pterodactylus.fcp.test.Peers.createPeer;
import static net.pterodactylus.fcp.test.VersionMatchers.isVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public class PeerTest {

	@Test
	public void peerReturnsCorrectNodeRef() {
		NodeRef nodeRef = peer.getNodeRef();
		assertThat(nodeRef.getARK(), isArk(equalTo("ark-public-uri"), equalTo("ark-private-uri"), equalTo(123)));
		assertThat(nodeRef.getDSAGroup(), isDsaGroup(equalTo("dsa-group-base"), equalTo("dsa-group-prime"), equalTo("dsa-group-subprime")));
		assertThat(nodeRef.getDSAPublicKey(), equalTo("dsa-public-key"));
		assertThat(nodeRef.getIdentity(), equalTo("identity"));
		assertThat(nodeRef.getLastGoodVersion(), isVersion(equalTo("Node"), equalTo("0.1.2"), equalTo("1.2.3"), equalTo(234)));
		assertThat(nodeRef.getLocation(), equalTo(0.4));
		assertThat(nodeRef.getMyName(), equalTo("Test Node"));
		assertThat(nodeRef.getNegotiationTypes(), equalTo(new int[] { 1, 2, 3 }));
		assertThat(nodeRef.getPhysicalUDP(), equalTo("physical-udp"));
		assertThat(nodeRef.getSignature(), equalTo("signature"));
		assertThat(nodeRef.getVersion(), isVersion(equalTo("TestNode"), equalTo("1.2.3"), equalTo("2.3.4"), equalTo(345)));
	}

	@Test
	public void peerReturnsCorrectIdentifier() {
		assertThat(peer.getIdentifier(), equalTo("identifier"));
	}

	@Test
	public void peerReturnsCorrectPhysicalUdp() {
		assertThat(peer.getPhysicalUDP(), equalTo("physical-udp"));
	}

	@Test
	public void peerReturnsOpennetIfFieldIsSetToTrue() {
		assertThat(createPeer(m -> m.setField("opennet", "true")).isOpennet(), equalTo(true));
	}

	@Test
	public void peerReturnsNotOpennetIfFieldIsSetToFalse() {
		assertThat(createPeer(m -> m.setField("opennet", "false")).isOpennet(), equalTo(false));
	}

	@Test
	public void peerReturnsNotOpennetIfFieldIsNotSet() {
		assertThat(createPeer(m -> m.setField("opennet", "")).isOpennet(), equalTo(false));
	}

	@Test
	public void peerReturnsSeedIfFieldIsSetToTrue() {
		assertThat(createPeer(m -> m.setField("seed", "true")).isSeed(), equalTo(true));
	}

	@Test
	public void peerReturnsNotSeedIfFieldIsSetToFalse() {
		assertThat(createPeer(m -> m.setField("seed", "false")).isSeed(), equalTo(false));
	}

	@Test
	public void peerReturnsNotSeedIfFieldIsNotSet() {
		assertThat(createPeer(m -> m.setField("seed", "")).isSeed(), equalTo(false));
	}

	@Test
	public void peerReturnsDsaPublicKeyCorrectly() {
		assertThat(peer.getDSAPublicKey(), equalTo("dsa-public-key"));
	}

	@Test
	public void peerReturnsDsaGroupCorrectly() {
		assertThat(peer.getDSAGroup(), isDsaGroup(equalTo("dsa-group-base"), equalTo("dsa-group-prime"), equalTo("dsa-group-subprime")));
	}

	@Test
	public void peerReturnsCorrectLastGoodVersion() {
		assertThat(peer.getLastGoodVersion(), isVersion(equalTo("Node"), equalTo("0.1.2"), equalTo("1.2.3"), equalTo(234)));
	}

	@Test
	public void peerReturnsCorrectArk() {
		assertThat(peer.getARK(), isArk(equalTo("ark-public-uri"), equalTo("ark-private-uri"), equalTo(123)));
	}

	@Test
	public void peerReturnsCorrectIdentity() {
		assertThat(peer.getIdentity(), equalTo("identity"));
	}

	@Test
	public void peerReturnsCorrectName() {
		assertThat(peer.getMyName(), equalTo("Test Node"));
	}

	@Test
	public void peerReturnsCorrectLocation() {
		assertThat(peer.getLocation(), equalTo(0.4));
	}

	@Test
	public void peerReturnsTestnetIfFieldIsSetToTrue() {
		assertThat(createPeer(m -> m.setField("testnet", "true")).isTestnet(), equalTo(true));
	}

	@Test
	public void peerReturnsNotTestnetIfFieldIsSetToFalse() {
		assertThat(createPeer(m -> m.setField("testnet", "false")).isTestnet(), equalTo(false));
	}

	@Test
	public void peerReturnsNotTestnetIfFieldIsNotSet() {
		assertThat(createPeer(m -> m.setField("testnet", "")).isTestnet(), equalTo(false));
	}

	@Test
	public void peerReturnsCorrectVersion() {
		assertThat(peer.getVersion(), isVersion(equalTo("TestNode"), equalTo("1.2.3"), equalTo("2.3.4"), equalTo(345)));
	}

	@Test
	public void peerReturnsCorrectNegotiationTypes() {
		assertThat(peer.getNegotiationTypes(), equalTo(new int[] { 1, 2, 3 }));
	}

	@Test
	public void peerReturnsCorrectVolatileFields() {
		assertThat(createPeer(addVolatileFields()).getVolatileFields(), allOf(
				aMapWithSize(3),
				hasEntry("volatile.a", "A1"),
				hasEntry("volatile.b", "B2"),
				hasEntry("volatile.c", "C3")
		));
	}

	@Test
	public void peerReturnsCorrectVolatileField() {
		assertThat(createPeer(addVolatileFields()).getVolatile("a"), equalTo("A1"));
	}

	@Test
	public void peerReturnsCorrectMetadataFields() {
		assertThat(createPeer(addMetadataFields()).getMetadataFields(), allOf(
				aMapWithSize(3),
				hasEntry("metadata.a", "MA1"),
				hasEntry("metadata.b", "MB2"),
				hasEntry("metadata.c", "MC3")
		));
	}

	@Test
	public void peerReturnsCorrectMetadataField() {
		assertThat(createPeer(addMetadataFields()).getMetadata("a"), equalTo("MA1"));
	}

	private final Peer peer = createPeer();

}
