package ua.in.smartdev.incrementalbuild.model;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileSetState {
		
	private Map<File, FileState> states = new ConcurrentHashMap<File, FileState>();
	
	public void add(FileState newState) {
		states.put(newState.getFile(), newState);
	}
	
	
}
