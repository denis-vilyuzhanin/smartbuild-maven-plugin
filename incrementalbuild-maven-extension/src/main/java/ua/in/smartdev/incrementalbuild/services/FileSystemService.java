package ua.in.smartdev.incrementalbuild.services;

import java.io.File;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class FileSystemService {

	public Observable<File> fetchAllFilesRecursvely(File baseFolder) {
		
		return Observable.empty();
	}
}
