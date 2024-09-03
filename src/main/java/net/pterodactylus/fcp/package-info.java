/**
 * Package that holds all the message types that are used in the communication
 * with a Freenet Node.
 *
 * <h2>Usage</h2>
 * <p>
 * This library was designed to implement the full range of the Freenet Client
 * Protocol, Version 2.0. At the moment the library provides a rather low-level
 * approach, wrapping each FCP message into its own object but some kind of
 * high-level client that does not require any interfaces to be implemented
 * will probably provided as well.
 * </p>
 * <p>
 * First, create a connection to the node:
 *</p>
 * <pre>
 * FcpConnection fcpConnection = new DefaultFcpConnection();
 * </pre>
 *
 * Now implement the {@link net.pterodactylus.fcp.FcpListener} interface
 * or extend {@link net.pterodactylus.fcp.FcpAdapter} to create a listener
 * to {@link net.pterodactylus.fcp.FcpConnection#addFcpListener(net.pterodactylus.fcp.FcpListener) register}
 * with the FCP connection, call {@link net.pterodactylus.fcp.FcpConnection#connect()},
 * and handle all incoming events.
 *
 * <pre>
 * public class MyClass implements FcpListener {
 *
 * 	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
 * 		…
 * 	}
 *
 * 	// implement all further methods here
 *
 * }
 * </pre>
 */

package net.pterodactylus.fcp;
