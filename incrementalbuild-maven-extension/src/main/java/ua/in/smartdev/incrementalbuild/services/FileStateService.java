package ua.in.smartdev.incrementalbuild.services;

import java.io.File;
import java.util.Date;

import org.apache.maven.model.FileSet;
import org.codehaus.plexus.component.annotations.Component;

import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
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
				      .map(doCreateFileState())
				      .collect(doCreateNewFileSetState(), doAddFileStateToSet());
	}
	
	
	Func1<File, FileState> doCreateFileState() {
		return new Func1<File, FileState>() {

			@Override
			public FileState call(File file) {
				return new FileState(file, new Date(file.lastModified()));
			}
		};
	}
	
	Func0<FileSetState> doCreateNewFileSetState() {
		return new Func0<FileSetState>() {
			
			@Override
			public FileSetState call() {
				return new FileSetState();
			}
		};
	}
	
	Action2<FileSetState, FileState> doAddFileStateToSet() {
		return new Action2<FileSetState, FileState>() {

			@Override
			public void call(FileSetState setSate, FileState fileState) {
				setSate.add(fileState);
			}
			
		};

	}
}
