package ua.in.smartdev.incrementalbuild.services;

import org.codehaus.plexus.component.annotations.Component;

import rx.Observable;
import ua.in.smartdev.incrementalbuild.model.ProjectState;
import ua.in.smartdev.incrementalbuild.model.IncrementalBuildSpecification;

@Component(role = ProjectStateService.class)
public class ProjectStateService {


	public Observable<ProjectState> rememberProjectState(IncrementalBuildSpecification projectSpecification) {
		ProjectState state = new ProjectState();
		state.setProjectId(projectSpecification.getProjectId());
		return Observable.just(state);
	}
}
