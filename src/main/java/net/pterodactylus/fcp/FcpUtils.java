/*
 * jFCPlib - FcpUtils.java - Copyright © 2008–2016 David Roden
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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Helper class with utility methods for the FCP protocol.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpUtils {

	/** Counter for unique identifiers. */
	private static AtomicLong counter = new AtomicLong();

	/**
	 * Returns a unique identifier.
	 *
	 * @return A unique identifier
	 */
	public static String getUniqueIdentifier() {
		return format("%d-%d", currentTimeMillis(), counter.getAndIncrement());
	}

	/**
	 * Parses an integer field, separated by ‘;’ and returns the parsed values.
	 *
	 * @param field
	 *            The field to parse
	 * @return An array with the parsed values
	 * @throws NumberFormatException
	 *             if a value can not be converted to a number
	 */
	public static int[] decodeMultiIntegerField(String field) throws NumberFormatException {
		return stream(field.split(";")).mapToInt(Integer::parseInt).toArray();
	}

	/**
	 * Encodes the given integer array into a string, separating the values by
	 * ‘;’.
	 *
	 * @param values
	 *            The values to encode
	 * @return The encoded values
	 */
	public static String encodeMultiIntegerField(int[] values) {
		return stream(values).mapToObj(String::valueOf).collect(joining(";"));
	}

	/**
	 * Encodes the given string array into a string, separating the values by
	 * ‘;’.
	 *
	 * @param values
	 *            The values to encode
	 * @return The encoded values
	 */
	public static String encodeMultiStringField(String[] values) {
		return join(";", values);
	}

	/**
	 * Tries to parse the given string into an int, returning <code>-1</code>
	 * if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @return The parsed int, or <code>-1</code>
	 */
	public static int safeParseInt(String value) {
		return safeParseInt(value, -1);
	}

	/**
	 * Tries to parse the given string into an int, returning
	 * <code>defaultValue</code> if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @param defaultValue
	 *            The value to return if the string can not be parsed.
	 * @return The parsed int, or <code>defaultValue</code>
	 */
	public static int safeParseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe1) {
			return defaultValue;
		}
	}

	/**
	 * Tries to parse the given string into an long, returning <code>-1</code>
	 * if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @return The parsed long, or <code>-1</code>
	 */
	public static long safeParseLong(String value) {
		return safeParseLong(value, -1);
	}

	/**
	 * Tries to parse the given string into an long, returning
	 * <code>defaultValue</code> if the string can not be parsed.
	 *
	 * @param value
	 *            The string to parse
	 * @param defaultValue
	 *            The value to return if the string can not be parsed.
	 * @return The parsed long, or <code>defaultValue</code>
	 */
	public static long safeParseLong(String value, long defaultValue) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException nfe1) {
			return defaultValue;
		}
	}

	/**
	 * Closes the given socket.
	 *
	 * @param socket
	 *            The socket to close
	 */
	public static void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ioe1) {
				/* ignore. */
			}
		}
	}

	/**
	 * Closes the given Closeable.
	 *
	 * @param closeable
	 *            The Closeable to close
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ioe1) {
				/* ignore. */
			}
		}
	}

	/**
	 * Copies as many bytes as possible (i.e. until {@link InputStream#read()}
	 * returns <code>-1</code>) from the source input stream to the destination
	 * output stream.
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination) throws IOException {
		copy(source, destination, -1);
	}

	/**
	 * Copies <code>length</code> bytes from the source input stream to the
	 * destination output stream. If <code>length</code> is <code>-1</code> as
	 * much bytes as possible will be copied (i.e. until
	 * {@link InputStream#read()} returns <code>-1</code> to signal the end of
	 * the stream).
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination, long length) throws IOException {
		copy(source, destination, length, 1 << 16);
	}

	/**
	 * Copies <code>length</code> bytes from the source input stream to the
	 * destination output stream. If <code>length</code> is <code>-1</code> as
	 * much bytes as possible will be copied (i.e. until
	 * {@link InputStream#read()} returns <code>-1</code> to signal the end of
	 * the stream).
	 *
	 * @param source
	 *            The input stream to read from
	 * @param destination
	 *            The output stream to write to
	 * @param length
	 *            The number of bytes to copy
	 * @param bufferSize
	 *            The buffer size
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void copy(InputStream source, OutputStream destination, long length, int bufferSize) throws IOException {
		long remaining = length;
		byte[] buffer = new byte[bufferSize];
		int read = 0;
		while ((remaining == -1) || (remaining > 0)) {
			read = source.read(buffer, 0, ((remaining > bufferSize) || (remaining == -1)) ? bufferSize : (int) remaining);
			if (read == -1) {
				if (length == -1) {
					return;
				}
				throw new EOFException("stream reached eof");
			}
			destination.write(buffer, 0, read);
			if (remaining > 0) {
				remaining -= read;
			}
		}
	}

}
