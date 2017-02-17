package ua.in.smartdev.incrementalbuild.model;

import org.apache.maven.project.MavenProject;

public class ProjectState {

	private String projectId;
	private MavenProject mavenProject;
	private IncrementalBuildSpecification specification;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public MavenProject getMavenProject() {
		return mavenProject;
	}

	public void setMavenProject(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	public IncrementalBuildSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(IncrementalBuildSpecification specification) {
		this.specification = specification;
	}
	
	
	
}
