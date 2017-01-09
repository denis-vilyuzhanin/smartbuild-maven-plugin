package ua.in.smartdev.incrementalbuild.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rx.Observable;
import rx.functions.Action1;
import ua.in.smartdev.incrementalbuild.filesystem.FileSystemScanner;

import static org.junit.Assert.*;

public class FileSystemScannerTest {

	@Rule
	public TemporaryFolder baseDirectory = new TemporaryFolder();
	
	FileReceiver fileReceiver;
	
	FileSystemScanner scanner;
	
	@Before
	public void init() throws IOException {
		baseDirectory.create();
		assertTrue(baseDirectory.getRoot().exists());
		fileReceiver = new FileReceiver();
		
		scanner = new FileSystemScanner();
		scanner.setBaseDirectory(baseDirectory.getRoot());
	}
	
	@Test
	public void scanEmptyFolder() {
		//given
		Set<File> expected = files(baseDirectory.getRoot());
		//when
		scanner.scan().subscribe(fileReceiver);
		//then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}
	
	@Test
	public void scanFolderWithFiles() throws IOException {
		//given
		File file1 = baseDirectory.newFile("file1");
		File file2 = baseDirectory.newFile("file2");
		Set<File> expected = files(baseDirectory.getRoot(), file1, file2);
		//when
		scanner.scan().subscribe(fileReceiver);
		//then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}
	
	@Test
	public void scanFolderWithFolders() throws IOException {
		//given
		File folder1 = baseDirectory.newFolder("folder1");
		File folder2 = baseDirectory.newFolder("folder2");
		Set<File> expected = files(baseDirectory.getRoot(), folder1, folder2);
		//when
		scanner.scan().subscribe(fileReceiver);
		//then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}
	
	@Test
	public void scanTree() throws IOException {
		//given
		File fileInRoot = baseDirectory.newFile("fileInRoot");
		File folder1 = baseDirectory.newFolder("folder1");
		File file1InFolder1 = newFileInFolder(folder1, "file1InFolder1");
		File file2InFolder1 = newFileInFolder(folder1, "file2InFolder1");
		
		File folder2 = baseDirectory.newFolder("folder2");
		File folderInFolder2 = newFolderInFolder(folder2, "folderInFolder2");
		File fileInFolderInFolder2 = newFileInFolder(folder1, "fileInFolderInFolder2");
		
		Set<File> expected = files(baseDirectory.getRoot(), 
				                   fileInRoot,
				                   folder1,
				                   file1InFolder1,
				                   file2InFolder1,
				                   folder2,
				                   folderInFolder2,
				                   fileInFolderInFolder2);
		//when
		scanner.scan().subscribe(fileReceiver);
		//then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}
	
	
	private Set<File> files(File ...files) {
		return new HashSet<File>(Arrays.asList(files));
	}
	
	private File newFileInFolder(File parent, String name) throws IOException {
		File newFile = new File(parent, name);
		assertTrue("Can't create  new file: " + newFile, newFile.createNewFile());
		return newFile;
	}
	
	private File newFolderInFolder(File parent, String name) {
		File newFolder = new File(parent, name);
		assertTrue("Can't create  new folder: " + newFolder, newFolder.mkdir());
		return newFolder;
	}
	
	class FileReceiver implements Action1<File> {

		private Set<File> receivedFiles = new HashSet<File>();

		@Override
		public void call(File nextFile) {
			receivedFiles.add(nextFile);
		}

		public Set<File> getReceivedFiles() {
			return receivedFiles;
		}
		
	}
}
