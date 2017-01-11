package ua.in.smartdev.incrementalbuild.services;

import org.apache.maven.model.FileSet;
import org.codehaus.plexus.component.annotations.Component;

import rx.Observable;
import ua.in.smartdev.incrementalbuild.filesystem.FileSystemScanner;
import ua.in.smartdev.incrementalbuild.model.FileSetState;
import ua.in.smartdev.incrementalbuild.model.FileState;

@Component(role = FileStateService.class)
public class FileStateService {
	
	public Observable<FileSetState> fetchState(FileSet files) {
		FileSystemScanner scanner = new FileSystemScanner();
		scanner.setIncludes(files.getIncludes());
		scanner.setExcludes(files.getExcludes());
		return scanner.scan()
				      .map(FileState.FILE_TO_STATE)
				      .collect(FileSetState.NEW, FileSetState.ADD);
	}
	
	
	
}
