package ua.in.smartdev.incrementalbuild.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.ScanConductor;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.exceptions.Exceptions;

public class FileSystemScanner {

	private File baseDirectory = new File(".");
	private List<String> excludes = Collections.emptyList();
	private List<String> includes = Collections.emptyList();
	
	public Observable<File> scan() {
		return Observable.create(new OnSubscribe<File>() {

			@Override
			public void call(Subscriber<? super File> subscriber) {
				scan(subscriber);
			}
		});
	}

	private void scan(final Subscriber<? super File> subscriber) {
		Throwable error = null;
		try {
			File baseDirectory = this.baseDirectory;
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(baseDirectory);
			if (!includes.isEmpty()) {
				scanner.setIncludes(includes.toArray(new String[includes.size()]));
			}
			if (!excludes.isEmpty()) {
				scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
			}
			scanner.setScanConductor(new Conductor(subscriber));
			scanner.scan();
			if (subscriber.isUnsubscribed()) {
				return;
			}
			SortedSet<String> filesAndFolders = new TreeSet<String>();
			filesAndFolders.addAll(Arrays.asList(scanner.getIncludedFiles()));
			filesAndFolders.addAll(Arrays.asList(scanner.getIncludedDirectories()));
			for(String file : filesAndFolders) {
				subscriber.onNext(new File(baseDirectory, file));
			}
			subscriber.onCompleted();
		} catch (Throwable e) {
			Exceptions.throwOrReport(error, subscriber);
		}
	}
  		
	static class Conductor implements ScanConductor {

		private Subscriber<? super File> subscriber;
		
		public Conductor(Subscriber<? super File> subscriber) {
			this.subscriber = subscriber;
		}

		@Override
		public ScanAction visitDirectory(String name, File directory) {
			return nextAction(directory);
		}

		@Override
		public ScanAction visitFile(String name, File file) {
			return nextAction(file);
		}
		
		private ScanAction nextAction(File file) {
			if (subscriber.isUnsubscribed()) {
				return ScanAction.ABORT;
			}
			return ScanAction.CONTINUE;
		}
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(File baseDirectory) {
		if (baseDirectory == null) {
			throw new NullPointerException("baseDirectory can't be null");
		}
		this.baseDirectory = baseDirectory;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		if (excludes == null) {
			throw new NullPointerException("Excludes list can't be null");
		}
		this.excludes = Collections.unmodifiableList(new ArrayList<String>(excludes));
	}
	
	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(List<String> includes) {
		if (includes == null) {
			throw new IllegalArgumentException("Includes list can't be null");
		}
		this.includes = includes;
	}


	
}

