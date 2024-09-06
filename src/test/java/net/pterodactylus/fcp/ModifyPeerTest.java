package net.pterodactylus.fcp;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.function.BiConsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

public class ModifyPeerTest {

	@Test
	public void newModifyPeerMessageDoesNotContainAnyOfTheModifications() {
		ModifyPeer modifyPeer = new ModifyPeer("identifier", "node-identifier");
		assertThat(modifyPeer.getFields(), allOf(
				aMapWithSize(2),
				hasEntry("Identifier", "identifier"),
				hasEntry("NodeIdentifier", "node-identifier")
		));
	}

	@Test
	public void modifyPeerMessageWithEnabledTrueContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setEnabled, true, "IsDisabled", equalTo("false"));
	}

	@Test
	public void modifyPeerMessageWithEnabledFalseContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setEnabled, false, "IsDisabled", equalTo("true"));
	}

	@Test
	public void modifyPeerMessageWithAllowLocalAddressesTrueContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setAllowLocalAddresses, true, "AllowLocalAddresses", equalTo("true"));
	}

	@Test
	public void modifyPeerMessageWithAllowLocalAddressesFalseContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setAllowLocalAddresses, false, "AllowLocalAddresses", equalTo("false"));
	}

	@Test
	public void modifyPeerMessageWithListenOnlyTrueContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setListenOnly, true, "IsListenOnly", equalTo("true"));
	}

	@Test
	public void modifyPeerMessageWithListenOnlyFalseContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setListenOnly, false, "IsListenOnly", equalTo("false"));
	}

	@Test
	public void modifyPeerMessageWithBurstOnlyTrueContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setBurstOnly, true, "IsBurstOnly", equalTo("true"));
	}

	@Test
	public void modifyPeerMessageWithBurstOnlyFalseContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setBurstOnly, false, "IsBurstOnly", equalTo("false"));
	}

	@Test
	public void modifyPeerMessageWithIgnoreSourcePortTrueContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setIgnoreSourcePort, true, "IgnoreSourcePort", equalTo("true"));
	}

	@Test
	public void modifyPeerMessageWithIgnoreSourcePortFalseContainsCorrectField() {
		setFeatureAndVerifyMessage(ModifyPeer::setIgnoreSourcePort, false, "IgnoreSourcePort", equalTo("false"));
	}

	private void setFeatureAndVerifyMessage(BiConsumer<ModifyPeer, Boolean> featureSetter, boolean featureValue, String fieldName, Matcher<? super String> fieldValue) {
		ModifyPeer modifyPeer = new ModifyPeer("identifier", "node-identifier");
		featureSetter.accept(modifyPeer, featureValue);
		assertThat(modifyPeer.getFields(), hasEntry(equalTo(fieldName), fieldValue));
	}

}
