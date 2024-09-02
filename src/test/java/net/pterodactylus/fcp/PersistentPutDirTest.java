package net.pterodactylus.fcp;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class PersistentPutDirTest {

	@Test
	public void nameOfMessageIsRetrievedFromReceivedMessage() {
		PersistentPutDir persistentPutDir = new PersistentPutDir(new FcpMessage("Test"));
		assertThat(persistentPutDir.getName(), equalTo("Test"));
	}

	@Test
	public void identifierOfMessageIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Identifier", "TestRequest", PersistentPutDir::getIdentifier, equalTo("TestRequest"));
	}

	@Test
	public void uriOfMessageIsRetreivedFromReceivedMessage() {
		createPersistentPutDirAndMatch("URI", "TestURI", PersistentPutDir::getURI, equalTo("TestURI"));
	}

	@Test
	public void verbosityOfMessageIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Verbosity", "999", persistentPutDir -> persistentPutDir.getVerbosity().toString(), equalTo("999"));
	}

	@Test
	public void verbosityNoneIsReturnedWhenVerbosityCannotBeParsed() {
		createPersistentPutDirAndMatch("Verbosity", "Invalid", PersistentPutDir::getVerbosity, equalTo(Verbosity.NONE));
	}

	@Test
	public void priorityOfMessageIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("PriorityClass", "2", PersistentPutDir::getPriority, equalTo(Priority.immediateSplitfile));
	}

	@Test
	public void priorityUnknownIsReturnedWhenPriorityCannotBeParsed() {
		createPersistentPutDirAndMatch("PriorityClass", "invalid", PersistentPutDir::getPriority, equalTo(Priority.unknown));
	}

	@Test
	public void globalOfMessageIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Global", "true", PersistentPutDir::isGlobal, equalTo(true));
	}

	@Test
	public void putDirTypeIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("PutDirType", "complex", PersistentPutDir::getPutDirType, equalTo("complex"));
	}

	@Test
	public void compatibilityModeIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("CompatibilityMode", "COMPAT_CURRENT", PersistentPutDir::getCompatibilityMode, equalTo("COMPAT_CURRENT"));
	}

	@Test
	public void maxRetriesOfMessageIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("MaxRetries", "3", PersistentPutDir::getMaxRetries, equalTo(3));
	}

	@Test
	public void minusTwoIsReturnedWhenMaxRetriesCannotBeParsed() {
		// I actually do not know anymore why -2 is returned…
		createPersistentPutDirAndMatch("MaxRetries", "unknown", PersistentPutDir::getMaxRetries, equalTo(-2));
	}

	@Test
	public void fileCountIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Count", "17", PersistentPutDir::getFileCount, equalTo(17));
	}

	@Test
	public void fileNameOfFirstFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.0.Name", "first.txt", p -> p.getFileName(0), equalTo("first.txt"));
	}

	@Test
	public void fileNameOfSecondFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.1.Name", "second.txt", p -> p.getFileName(1), equalTo("second.txt"));
	}

	@Test
	public void fileLengthOfFirstFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.0.DataLength", "12345", p -> p.getFileDataLength(0), equalTo(12345L));
	}

	@Test
	public void fileLengthOfThirdFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.2.DataLength", "23456", p -> p.getFileDataLength(2), equalTo(23456L));
	}

	@Test
	public void uploadSourceOfFirstFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.0.UploadFrom", "disk", p -> p.getFileUploadFrom(0), equalTo(UploadFrom.disk));
	}

	@Test
	public void uploadSourceOfFourthFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.3.UploadFrom", "direct", p -> p.getFileUploadFrom(3), equalTo(UploadFrom.direct));
	}

	@Test
	public void uploadSourceOfFifthFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.4.UploadFrom", "redirect", p -> p.getFileUploadFrom(4), equalTo(UploadFrom.redirect));
	}

	@Test
	public void contentTypeOfFirstFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.0.Metadata.ContentType", "text/plain", p -> p.getFileMetadataContentType(0), equalTo("text/plain"));
	}

	@Test
	public void contentTypeIsNullIfAFileDoesNotHaveAContentType() {
		createPersistentPutDirAndMatch("Files.0.Metadata.ContentType", "text/plain", p -> p.getFileMetadataContentType(1), nullValue());
	}

	@Test
	public void nameOfFirstOnDiskFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.0.Filename", "first.txt", p -> p.getFileFilename(0), equalTo("first.txt"));
	}

	@Test
	public void nameOfSecondOnDiskFileIsRetrievedFromReceivedMessage() {
		createPersistentPutDirAndMatch("Files.1.Filename", "second.txt", p -> p.getFileFilename(1), equalTo("second.txt"));
	}

	private static <E> void createPersistentPutDirAndMatch(String field, String value, Function<PersistentPutDir, E> extractor, Matcher<? super E> matcher) {
		PersistentPutDir persistentPutDir = createPersistentPutDir(field, value);
		assertThat(extractor.apply(persistentPutDir), matcher);
	}

	private static PersistentPutDir createPersistentPutDir(String field, String value) {
		return new PersistentPutDir(createReceivedMessage(field, value));
	}

	private static FcpMessage createReceivedMessage(String field, String value) {
		FcpMessage receivedMessage = new FcpMessage("Test");
		receivedMessage.setField(field, value);
		return receivedMessage;
	}

}
