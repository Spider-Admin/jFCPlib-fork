/*
 * jFCPlib - AddPeer.java - Copyright © 2008–2016 David Roden
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

import java.net.URL;

/**
 * The “AddPeer” request adds a peer to the node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class AddPeer extends FcpMessage {

	/**
	 * Represents the trust values for a peer.
	 */
	public enum Trust {LOW, NORMAL, HIGH}

	/**
	 * Represents the visibility values for a peer.
	 */
	public enum Visibility {NO, NAME_ONLY, YES}

	/**
	 * Creates a new “AddPeer” request.
	 */
	private AddPeer(Trust trust, Visibility visibility) {
		super("AddPeer");
		setField("Trust", trust.name());
		setField("Visibility", visibility.name());
	}

	/**
	 * Creates a new “AddPeer” request that reads the noderef of the peer from
	 * the given file.
	 *
	 * @param trust The trust values for the new peer
	 * @param visibility The visibility of the new peer
	 * @param file The file to read the noderef from
	 */
	public AddPeer(Trust trust, Visibility visibility, String file) {
		this(trust, visibility);
		setField("File", file);
	}

	public AddPeer(Trust trust, Visibility visibility, String identifier, String file) {
		this(trust, visibility, file);
		setField("Identifier", identifier);
	}

	/**
	 * Creates a new “AddPeer” request that reads the noderef of the peer from
	 * the given URL.
	 *
	 * @param trust The trust values for the new peer
	 * @param visibility The visibility of the new peer
	 * @param url The URL to read the noderef from
	 */
	public AddPeer(Trust trust, Visibility visibility, URL url) {
		this(trust, visibility);
		setField("URL", String.valueOf(url));
	}

	public AddPeer(Trust trust, Visibility visibility, String identifier, URL url) {
		this(trust, visibility, url);
		setField("Identifier", identifier);
	}

	/**
	 * Creates a new “AddPeer” request that adds the peer given by the noderef.
	 *
	 * @param trust The trust values for the new peer
	 * @param visibility The visibility of the new peer
	 * @param nodeRef The noderef of the peer
	 */
	public AddPeer(Trust trust, Visibility visibility, NodeRef nodeRef) {
		this(trust, visibility);
		setNodeRef(nodeRef);
	}

	public AddPeer(Trust trust, Visibility visibility, String identifier, NodeRef nodeRef) {
		this(trust, visibility, nodeRef);
		setField("Identifier", identifier);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Sets the noderef of the peer to add.
	 *
	 * @param nodeRef
	 *            The noderef of the peer
	 */
	private void setNodeRef(NodeRef nodeRef) {
		setField("lastGoodVersion", String.valueOf(nodeRef.getLastGoodVersion()));
		setField("opennet", String.valueOf(nodeRef.isOpennet()));
		setField("identity", nodeRef.getIdentity());
		setField("myName", nodeRef.getMyName());
		setField("location", String.valueOf(nodeRef.getLocation()));
		setField("testnet", String.valueOf(nodeRef.isTestnet()));
		setField("version", String.valueOf(nodeRef.getVersion()));
		setField("physical.udp", nodeRef.getPhysicalUDP());
		setField("ark.pubURI", nodeRef.getARK().getPublicURI());
		setField("ark.number", String.valueOf(nodeRef.getARK().getNumber()));
		setField("dsaPubKey.y", nodeRef.getDSAPublicKey());
		setField("dsaGroup.g", nodeRef.getDSAGroup().getBase());
		setField("dsaGroup.p", nodeRef.getDSAGroup().getPrime());
		setField("dsaGroup.q", nodeRef.getDSAGroup().getSubprime());
		setField("auth.negTypes", FcpUtils.encodeMultiIntegerField(nodeRef.getNegotiationTypes()));
		setField("sig", nodeRef.getSignature());
	}

}
