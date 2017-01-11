package ua.in.smartdev.incrementalbuild.model;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.functions.Action2;
import rx.functions.Func0;

public class FileSetState {

	public static final Func0<FileSetState> NEW = new Func0<FileSetState>() {
		
		@Override
		public FileSetState call() {
			return new FileSetState();
		}
	};
	
	public static final Action2<FileSetState, FileState> ADD = new Action2<FileSetState, FileState>() {

		@Override
		public void call(FileSetState setSate, FileState fileState) {
			setSate.add(fileState);
		}
		
	};
	
	private Map<File, FileState> states = new ConcurrentHashMap<File, FileState>();
	
	public void add(FileState newState) {
		states.put(newState.getFile(), newState);
	}
	
	
}
