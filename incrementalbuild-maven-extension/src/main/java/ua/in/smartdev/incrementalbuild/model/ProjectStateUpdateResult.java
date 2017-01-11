package ua.in.smartdev.incrementalbuild.model;

public class ProjectStateUpdateResult {

	private String projectId;
	private long durationInMs;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public long getDurationInMs() {
		return durationInMs;
	}

	public void setDurationInMs(long durationInMs) {
		this.durationInMs = durationInMs;
	}
	
	
	
}
