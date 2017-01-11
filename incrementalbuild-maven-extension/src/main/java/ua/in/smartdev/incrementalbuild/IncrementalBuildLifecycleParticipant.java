package ua.in.smartdev.incrementalbuild;

import static ua.in.smartdev.incrementalbuild.Constants.CURRENT_PROJECT_STATE_CONTEXT_ATTRIBUTE;
import static ua.in.smartdev.incrementalbuild.Constants.INCREMENTAL_BUILD_ENABLED;
import static ua.in.smartdev.incrementalbuild.Constants.INCREMENTAL_BUILD_ENABLED_ALIAS;
import static ua.in.smartdev.incrementalbuild.Constants.INCREMENTAL_BUILD_ENABLED_DEFAULT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import ua.in.smartdev.incrementalbuild.managers.IncrementalBuildManager;
import ua.in.smartdev.incrementalbuild.model.IncrementalBuildSpecification;
import ua.in.smartdev.incrementalbuild.model.IncrementalBuildStatistic;
import ua.in.smartdev.incrementalbuild.model.ProjectState;
import ua.in.smartdev.incrementalbuild.model.ProjectStateUpdateResult;
import ua.in.smartdev.incrementalbuild.services.StatisticService;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "incrementalbuild")
public class IncrementalBuildLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
	Logger logger;
	
	@Requirement
	IncrementalBuildManager incrementalBuildManager;
		
	@Requirement
	StatisticService statisticService;
	
	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		logger.info("Incremental Build");

		if (!isIncrementalBuildEnabled(session)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Incremental build is disabled");
			}
			return;
		}

		Plugin helperPlugin = new Plugin();
		helperPlugin.setArtifactId("incrementalbuild-helper-maven-plugin");
		helperPlugin.setGroupId("ua.in.smartdev.incrementalbuild");

		PluginExecution doNothingExecution = new PluginExecution();
		doNothingExecution.addGoal("doNothing");
		doNothingExecution.setId("UP TO DATE");
		helperPlugin.setExecutions(Arrays.asList(doNothingExecution));
		
		List<Observable<ProjectState>> allProjectStates = new ArrayList<Observable<ProjectState>>();
		for (MavenProject project : session.getAllProjects()) {
			IncrementalBuildSpecification specification = new IncrementalBuildSpecification();
			specification.setProjectId(project.getGroupId() + ":" + project.getArtifactId());
			
			allProjectStates.add(incrementalBuildManager.discoverProjectState(specification)
			                                            .doOnNext(attachCurrentStateToMavenProjectContext(project)));
			
			logger.info("[SMART_BUILD] project: " + project.getArtifactId() + " plugins: " + project.getBuildPlugins());
			//project.getModel().getBuild().setPlugins(Arrays.asList(helperPlugin));
		}
		List<ProjectState> allStates = 
				Observable.concat(allProjectStates)
		                  .toList()
		                  .toBlocking()
		                  .last();
		logger.info(allStates.size() + " projects will be rebuilt");
	}
	
	@Override
	public void afterSessionEnd( MavenSession session ) throws MavenExecutionException {
		if (!isIncrementalBuildEnabled(session)) {
			return;
		}
		logger.info("Remember project state");
		
		final int projectsCount = session.getAllProjects().size();
		List<Observable<ProjectStateUpdateResult>> updateResults = 
				new ArrayList<Observable<ProjectStateUpdateResult>>(session.getAllProjects().size());
		for(MavenProject project : session.getAllProjects()) {
			ProjectState projectState = 
					(ProjectState) project.getContextValue(CURRENT_PROJECT_STATE_CONTEXT_ATTRIBUTE); 
			updateResults.add(
					incrementalBuildManager.updateProjectState(projectState));
		}
	
		Observable.concat(updateResults)
		          .toList()
		          .flatMap(buildTotalStatistic())
		          .subscribe(printStatistic());
		
		logger.info("Incremental Build Done");
    }
	
	private boolean isIncrementalBuildEnabled(MavenSession session) {
		
		List<Properties> allProperties = new ArrayList<Properties>(3);
		allProperties.add(session.getUserProperties());
		allProperties.add(session.getSystemProperties());
				
		if (session.getCurrentProject() != null) {
			allProperties.add(session.getCurrentProject().getModel().getProperties());
		}
		for(Properties properties : allProperties) {
			Boolean isEnabled = getIncrementalBuildEnabled(properties);
			if (isEnabled == null) {
				continue;
			}
			return isEnabled;
		}
		return INCREMENTAL_BUILD_ENABLED_DEFAULT;
	}
	
	private Boolean getIncrementalBuildEnabled(Properties properties) {
		String value;
		if (properties.containsKey(INCREMENTAL_BUILD_ENABLED)) {
			value = properties.getProperty(INCREMENTAL_BUILD_ENABLED);
		} else if (properties.containsKey("incrementalbuild.enabled")) {
			value = properties.getProperty(INCREMENTAL_BUILD_ENABLED_ALIAS);
		} else {
			return null;
		}
		return Boolean.parseBoolean(value);
	}

	private <T extends  ProjectStateUpdateResult> Func1<List<T>, Observable<IncrementalBuildStatistic>> buildTotalStatistic() {
		return new Func1<List<T>, Observable<IncrementalBuildStatistic>>() {

			@Override
			public Observable<IncrementalBuildStatistic> call(List<T> allResults) {
				return statisticService.buildStatistic(allResults);
			}
			
		};
		
	}
	
	private <T extends IncrementalBuildStatistic> Action1<T> printStatistic() {
		return new Action1<T>() {

			@Override
			public void call(T statistic) {
				logger.info("Total Duration: " + statistic.getTotalBuildDuration());
			}
			
		};
	}
	
	private Action1<ProjectState> attachCurrentStateToMavenProjectContext(final MavenProject mavenProject) {
		return new Action1<ProjectState>() {

			@Override
			public void call(ProjectState projectState) {
				mavenProject.setContextValue(CURRENT_PROJECT_STATE_CONTEXT_ATTRIBUTE, projectState);
			}
		};
	}
}
