package ua.in.smartdev.incrementalbuild.model;

import java.io.File;
import java.util.Date;

import rx.functions.Func1;

public class FileState {

	private File file;
	private Date lastModifiedDate;
	
	public FileState(File file, Date lastModifiedDate) {
		this.file = file;
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "FileState [file=" + file + ", lastModifiedDate=" + lastModifiedDate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileState other = (FileState) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (lastModifiedDate == null) {
			if (other.lastModifiedDate != null)
				return false;
		} else if (!lastModifiedDate.equals(other.lastModifiedDate))
			return false;
		return true;
	}
	
}
