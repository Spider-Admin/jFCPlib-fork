/*
 * jFCPlib - LocalSocket.java - Copyright © 2024 David ‘Bombe’ Roden
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

package net.pterodactylus.fcp.test;

import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Opens a local {@link ServerSocket} for use in a unit test.
 *
 * <h2>Usage</h2>
 *
 * <p>
 * Add a &#64;Rule with the {@link LocalServer} to a JUnit test to open a new
 * server socket for each test. The port number of the server socket can be
 * retrieved by {@link #getPort()}, the {@link Socket} (once a connection has
 * been established) is available from {@link #getSocket()}.
 * </p>
 * <p>
 * Only a single connection is made available to clients connecting to the
 * server. The server socket will only be opened before a test is started, and
 * it is closed after the test.
 * </p>
 * <pre>
 * public class ConnectionTest {
 *     &#64;Rule
 *     public final LocalServer localServer = new LocalServer();
 *     &#64;Test
 *     public void serverCanBeConnectedTo() {
 *         Socket socket = new Socket("localhost", localServer.getPort());
 *         assertThat(socket, notNullValue());
 *         socket.getOutputStream().write(123);
 *         assertThat(localServer.getSocket().getInputStream().read(), equalTo(123));
 *     }
 * }
 * </pre>
 */
public class LocalServer extends ExternalResource {

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public Socket getSocket() {
		try {
			connectedLatch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return socket;
	}

	@Override
	protected void before() {
		new Thread(() -> {
			try {
				socket = serverSocket.accept();
				connectedLatch.countDown();
			} catch (IOException e) {
				if (!finished) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	@Override
	protected void after() {
		finished = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public LocalServer() {
		try {
			serverSocket = new ServerSocket(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final ServerSocket serverSocket;
	private final CountDownLatch connectedLatch = new CountDownLatch(1);
	private volatile boolean finished = false;
	private volatile Socket socket;

}
