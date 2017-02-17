package ua.in.smartdev.incrementalbuild.managers;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import rx.Observable;
import ua.in.smartdev.incrementalbuild.model.IncrementalBuildSpecification;
import ua.in.smartdev.incrementalbuild.model.ProjectState;
import ua.in.smartdev.incrementalbuild.model.ProjectStateUpdateResult;


@Component(role = IncrementalBuildManager.class)
public class IncrementalBuildManager {

	@Requirement
	Logger logger;
	
	
	public Observable<ProjectState> discoverProjectState(MavenProject mavenProject) {
		String projectId = mavenProject.getGroupId() + ":" + mavenProject.getArtifactId();
		IncrementalBuildSpecification specification = new IncrementalBuildSpecification();
		specification.setProjectId(projectId);
		
		ProjectState currentState = new ProjectState();
		currentState.setProjectId(projectId);
		currentState.setMavenProject(mavenProject);
		return Observable.just(currentState);
	}
	

	public Observable<ProjectStateUpdateResult> updateProjectState(ProjectState currentState) {
		ProjectStateUpdateResult result = new ProjectStateUpdateResult();
		result.setProjectId(currentState.getProjectId());
		result.setDurationInMs(100);
		return Observable.just(result);
	}
}
