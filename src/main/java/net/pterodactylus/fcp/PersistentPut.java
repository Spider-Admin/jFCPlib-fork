/*
 * jFCPlib - PersistentPut.java - Copyright © 2008–2016 David Roden
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

/**
 * A “PersistentPut” message notifies a client about a persistent
 * {@link ClientPut} request.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PersistentPut extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PersistentPut” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	public PersistentPut(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the client token of the request.
	 *
	 * @return The client token of the request
	 */
	public String getClientToken() {
		return getField("ClientToken");
	}

	/**
	 * Returns the data length of the request.
	 *
	 * @return The data length of the request, or <code>-1</code> if the length
	 *         could not be parsed
	 */
	public long getDataLength() {
		return FcpUtils.safeParseLong(getField("DataLength"));
	}

	/**
	 * Returns whether the request is on the global queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> otherwise
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The identifier of the request
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns the maximum number of retries for failed blocks. When
	 * <code>-1</code> is returned each block is tried forever.
	 *
	 * @return The maximum number of retries for failed blocks, or
	 *         <code>-1</code> for unlimited retries, or <code>-2</code> if the
	 *         number of retries could not be parsed
	 */
	public int getMaxRetries() {
		return FcpUtils.safeParseInt(getField("MaxRetries"));
	}

	/**
	 * Returns the content type of the data.
	 *
	 * @return The content type
	 */
	public String getMetadataContentType() {
		return getField("Metadata.ContentType");
	}

	/**
	 * Returns the persistence of the request.
	 *
	 * @return The persistence of the request
	 */
	public Persistence getPersistence() {
		return Persistence.valueOf(getField("Persistence"));
	}

	/**
	 * Returns the priority of the request.
	 *
	 * @return The priority of the request, or {@link Priority#unknown} if the
	 *         priority could not be parsed
	 */
	public Priority getPriority() {
		return Priority.values()[FcpUtils.safeParseInt(getField("PriorityClass"), Priority.unknown.ordinal())];
	}

	/**
	 * Returns whether this request has started.
	 *
	 * @return <code>true</code> if the request has started, <code>false</code>
	 *         otherwise
	 */
	public boolean isStarted() {
		return Boolean.valueOf(getField("Started"));
	}

	/**
	 * Returns the filename of the original request, if specified.
	 *
	 * @return The filename of the original request, or {@code null} if none was given
	 */
	public String getFilename() {
		return getField("Filename");
	}

	/**
	 * Returns the target filename of the request.
	 *
	 * @return The target filename of the request
	 */
	public String getTargetFilename() {
		return getField("TargetFilename");
	}

	/**
	 * Returns the upload source of the request.
	 *
	 * @return The upload source of the request
	 */
	public UploadFrom getUploadFrom() {
		return UploadFrom.valueOf(getField("UploadFrom"));
	}

	/**
	 * Returns the target URI of the request.
	 *
	 * @return The target URI of the request
	 */
	public String getURI() {
		return getField("URI");
	}

	/**
	 * Returns the private URI of the original request, if specified.
	 *
	 * @return The private URI of the original request, or {@code null} if none was given
	 */
	public String getPrivateURI() {
		return getField("PrivateURI");
	}

	/**
	 * Returns the target URI of the original request, if specified.
	 *
	 * @return The target URI of the original request, or {@code null} if none was given
	 */
	public String getTargetURI() {
		return getField("TargetURI");
	}

	/**
	 * Returns the verbosity of the request.
	 *
	 * @return The verbosity of the request
	 */
	public Verbosity getVerbosity() {
		return Verbosity.valueOf(getField("Verbosity"));
	}

	public boolean isBinaryBlob() {
		return Boolean.parseBoolean(getField("BinaryBlob"));
	}

	/**
	 * Returns the compatibility mode of the original request.
	 *
	 * @return The compatibility mode of the original request, or {@code null} if none was given
	 */
	public String getCompatibilityMode() {
		return getField("CompatibilityMode");
	}

	public boolean isDontCompress() {
		return Boolean.parseBoolean(getField("DontCompress"));
	}

	/**
	 * Returns the compression codecs of the original request.
	 *
	 * @return The compression codecs of the original request, or {@code null} if none were given
	 */
	public String getCodecs() {
		return getField("Codecs");
	}

	public boolean isRealTime() {
		return Boolean.parseBoolean(getField("RealTime"));
	}

	/**
	 * Returns the splitfile crypto key of the original request.
	 *
	 * @return The splitfile crypto key of the original request, or {@code null} if none was given
	 */
	public String getSplitfileCryptoKey() {
		return getField("SplitfileCryptoKey");
	}

}
