package ua.in.smartdev.incrementalbuild.filesystem;

import java.io.File;

import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.apache.maven.shared.utils.io.ScanConductor;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.exceptions.Exceptions;

public class FileSystemScanner {

	private File baseDirectory = new File(".");
	
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
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(baseDirectory);
			scanner.setScanConductor(new Visitor(subscriber));
			scanner.scan();
		} catch (Throwable e) {
			error = e;
		} finally {
			if (error != null) {
				Exceptions.throwOrReport(error, subscriber);	
			} else {
				subscriber.onCompleted();
			}
		}
	}
  		
	static class Visitor implements ScanConductor {

		private Subscriber<? super File> subscriber;
		
		public Visitor(Subscriber<? super File> subscriber) {
			this.subscriber = subscriber;
		}

		@Override
		public ScanAction visitDirectory(String name, File directory) {
			return onNext(directory);
		}

		@Override
		public ScanAction visitFile(String name, File file) {
			return onNext(file);
		}
		
		private ScanAction onNext(File file) {
			if (subscriber.isUnsubscribed()) {
				return ScanAction.ABORT;
			}
			subscriber.onNext(file);
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
	
	
}

