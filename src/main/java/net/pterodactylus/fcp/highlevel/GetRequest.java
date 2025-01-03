/*
 * jFCPlib - GetRequest.java - Copyright © 2009–2016 David Roden
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

package net.pterodactylus.fcp.highlevel;

import net.pterodactylus.fcp.PersistentGet;

/**
 * High-level wrapper around {@link PersistentGet}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetRequest extends Request {

	/**
	 * Creates a new get request.
	 *
	 * @param persistentGet
	 *            The persistent Get request to wrap
	 */
	GetRequest(PersistentGet persistentGet) {
		super(persistentGet.getIdentifier(), persistentGet.getURI(), persistentGet.getClientToken(), persistentGet.isGlobal());
	}

}
