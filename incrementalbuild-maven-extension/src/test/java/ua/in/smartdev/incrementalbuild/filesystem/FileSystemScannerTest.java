package ua.in.smartdev.incrementalbuild.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
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
		// given
		Set<File> expected = files(baseDirectory.getRoot());
		// when
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void scanFolderWithFiles() throws IOException {
		// given
		File file1 = baseDirectory.newFile("file1");
		File file2 = baseDirectory.newFile("file2");
		Set<File> expected = files(baseDirectory.getRoot(), file1, file2);
		// when
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void scanFolderWithFolders() throws IOException {
		// given
		File folder1 = baseDirectory.newFolder("folder1");
		File folder2 = baseDirectory.newFolder("folder2");
		Set<File> expected = files(baseDirectory.getRoot(), folder1, folder2);
		// when
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void scanTree() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		// when
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(filesTree.allFilesAndFolders, fileReceiver.getReceivedFiles());
	}

	@Test
	public void excludeAll() throws IOException {
		// given
		new FilesTreeSample();
		scanner.setExcludes(Arrays.asList("**"));
		// when
		scanner.scan().subscribe(fileReceiver);
		// then
		assertTrue("All files and folders must be excluded", fileReceiver.getReceivedFiles().isEmpty());
	}

	@Test
	public void excludeOneFile() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>(filesTree.allFilesAndFolders);
		expected.remove(filesTree.fileInRoot);
		// when
		scanner.setExcludes(Arrays.asList("fileInRoot"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void excludeOneFileInSubFolder() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>(filesTree.allFilesAndFolders);
		expected.remove(filesTree.fileInFolderInFolder2);
		// when
		scanner.setExcludes(Arrays.asList("folder2/folderInFolder2/fileInFolderInFolder2"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void excludeOneFileInSubFolderByPattern() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>(filesTree.allFilesAndFolders);
		expected.remove(filesTree.fileInFolderInFolder2);
		// when
		scanner.setExcludes(Arrays.asList("**/fileInFolderInFolder2"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void includeOneFile() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>();
		expected.add(filesTree.fileInRoot);

		// when
		scanner.setIncludes(Arrays.asList("/fileInRoot"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void includeOneFileInSubFolder() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>();
		expected.add(filesTree.fileInFolderInFolder2);

		// when
		scanner.setIncludes(Arrays.asList("folder2/folderInFolder2/fileInFolderInFolder2"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void includeOneFileInSubFolderByPattern() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		Set<File> expected = new HashSet<File>();
		expected.add(filesTree.fileInFolderInFolder2);

		// when
		scanner.setIncludes(Arrays.asList("**/fileInFolderInFolder2"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void includeByFileExtension() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		File fileTxtInRoot = newFileInFolder(filesTree.root, "fileTxtInRoot.txt");
		File fileTxtInFolder1 = newFileInFolder(filesTree.folder1, "fileTxtInFolder1.txt");
		File fileTxtInFolderInFolder2 = newFileInFolder(filesTree.folderInFolder2, "fileTxtInFolderInFolder2.txt");
		Set<File> expected = new HashSet<File>();
		expected.addAll(Arrays.asList(fileTxtInRoot, fileTxtInFolder1, fileTxtInFolderInFolder2));

		// when
		scanner.setIncludes(Arrays.asList("**/*.txt"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void includeByFileExtensionAndExcludeByFolder() throws IOException {
		// given
		FilesTreeSample filesTree = new FilesTreeSample();
		File fileTxtInRoot = newFileInFolder(filesTree.root, "fileTxtInRoot.txt");
		File fileTxtInFolder1 = newFileInFolder(filesTree.folder1, "fileTxtInFolder1.txt");
		newFileInFolder(filesTree.folderInFolder2, "fileTxtInFolderInFolder2.txt");
		Set<File> expected = new HashSet<File>();
		expected.addAll(Arrays.asList(fileTxtInRoot, fileTxtInFolder1));

		// when
		scanner.setIncludes(Arrays.asList("**/*.txt"));
		scanner.setExcludes(Arrays.asList("folder2/**"));
		scanner.scan().subscribe(fileReceiver);
		// then
		assertEquals(expected, fileReceiver.getReceivedFiles());
	}

	@Test
	public void stopScanning() throws IOException {
		// give
		new FilesTreeSample();
		// when
		scanner.scan().subscribe(new Subscriber<File>() {

			@Override
			public void onStart() {
				super.unsubscribe();
			}

			@Override
			public void onCompleted() {
				fail("Must be unsubscribed on start");
			}

			@Override
			public void onError(Throwable e) {
				fail("Must be unsubscribed on start");
			}

			@Override
			public void onNext(File file) {
				fileReceiver.receive(file);
			}
		});
		// then
		assertTrue(fileReceiver.getReceivedFiles().isEmpty());
	}

	@Test
	public void parallelScanning() throws IOException, InterruptedException {
		// given
		final Thread testThread = Thread.currentThread();
		final AtomicReference<AssertionError> error = new AtomicReference<>();
		final CountDownLatch isCompleted = new CountDownLatch(1);
		new FilesTreeSample();
		// when
		scanner.scan()
		        .subscribeOn(Schedulers.io())
		        .subscribe(new Subscriber<File>() {

			        @Override
			        public void onCompleted() {
				        isCompleted.countDown();
			        }

			        @Override
			        public void onError(Throwable e) {
				        error.set((AssertionError) e);
				        isCompleted.countDown();
			        }

			        @Override
			        public void onNext(File t) {
			        	assertNotEquals(testThread, Thread.currentThread());
			        }
		        });
		
		//then
		isCompleted.await(1, TimeUnit.SECONDS);
		if (error.get() != null) {
			throw error.get();
		}
	}

	private Set<File> files(File... files) {
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
			receive(nextFile);
		}

		public void receive(File nextFile) {
			receivedFiles.add(nextFile);
		}

		public Set<File> getReceivedFiles() {
			return receivedFiles;
		}

	}

	class FilesTreeSample {
		File root;
		File fileInRoot;
		File folder1;
		File file1InFolder1;
		File file2InFolder1;

		File folder2;
		File folderInFolder2;
		File fileInFolderInFolder2;

		Set<File> allFilesAndFolders;

		public FilesTreeSample() throws IOException {
			root = baseDirectory.getRoot();
			fileInRoot = baseDirectory.newFile("fileInRoot");
			folder1 = baseDirectory.newFolder("folder1");
			file1InFolder1 = newFileInFolder(folder1, "file1InFolder1");
			file2InFolder1 = newFileInFolder(folder1, "file2InFolder1");

			folder2 = baseDirectory.newFolder("folder2");
			folderInFolder2 = newFolderInFolder(folder2, "folderInFolder2");
			fileInFolderInFolder2 = newFileInFolder(folderInFolder2, "fileInFolderInFolder2");

			allFilesAndFolders = files(baseDirectory.getRoot(), fileInRoot, folder1, file1InFolder1, file2InFolder1,
			        folder2, folderInFolder2, fileInFolderInFolder2);
		}
	}
}
