/*
 * jFCPlib - FCPPluginMessage.java - Copyright © 2008–2016 David Roden
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

import java.io.InputStream;

/**
 * An “FCPPluginMessage” sends a message with custom parameters and (optional)
 * payload to a plugin.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FCPPluginMessage extends FcpMessage {

	/**
	 * @deprecated Use {@link #FCPPluginMessage(String, String)} instead
	 * @param pluginClass The name of the plugin’s main class
	 */
	@Deprecated
	public FCPPluginMessage(String pluginClass) {
		super("FCPPluginMessage");
		setField("PluginName", pluginClass);
	}

	public FCPPluginMessage(String identifier, String pluginClass) {
		this(pluginClass);
		setField("Identifier", identifier);
	}

	/**
	 * @deprecated Use {@link #FCPPluginMessage(String, String)} instead
	 * @param identifier The identifier of the message
	 */
	@Deprecated
	public void setIdentifier(String identifier) {
		setField("Identifier", identifier);
	}

	/**
	 * Sets a custom parameter for the plugin.
	 *
	 * @param key
	 * 	The key of the parameter
	 * @param value
	 * 	The value of the parameter
	 */
	public void setParameter(String key, String value) {
		setField("Param." + key, value);
	}

	/**
	 * @deprecated Use {@link #setData(InputStream, long)} instead
	 * @param dataLength The length of the additional data in this message
	 */
	@Deprecated
	public void setDataLength(long dataLength) {
		setField("DataLength", String.valueOf(dataLength));
	}

	public void setData(InputStream payloadInputStream, long dataLength) {
		setPayloadInputStream(payloadInputStream);
		setDataLength(dataLength);
	}

}
