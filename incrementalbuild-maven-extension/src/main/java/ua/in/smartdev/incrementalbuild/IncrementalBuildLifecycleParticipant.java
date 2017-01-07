package ua.in.smartdev.incrementalbuild;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import org.codehaus.plexus.logging.Logger;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = "incrementalbuild" )
public class IncrementalBuildLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
    Logger logger;
    
	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		logger.info("[SMART_BUILD] afterProjectsRead");
		
		DefaultMavenExecutionRequest request = (DefaultMavenExecutionRequest) session.getRequest();
		//request.setSelectedProjects(Collections.emptyList());
		//request.setExcludedProjects(uptodateProjects);
		//session.setProjects(Collections.EMPTY_LIST);
		//request.setExcludedProjects(session.getAllProjects());
		Plugin helperPlugin = new Plugin();
		helperPlugin.setArtifactId("incrementalbuild-helper-maven-plugin");
		helperPlugin.setGroupId("ua.in.smartdev.incrementalbuild");
		
		PluginExecution doNothingExecution = new PluginExecution();
		doNothingExecution.addGoal("doNothing");
		doNothingExecution.setId("UP TO DATE");
		helperPlugin.setExecutions(Arrays.asList(doNothingExecution));
		
		for(MavenProject project : session.getAllProjects()) {
			project.getModel().getBuild().setPlugins(Arrays.asList(helperPlugin));
			logger.info("[SMART_BUILD] plugins: " + project.getBuildPlugins());
		}
		//request.setGoals(Arrays.asList("ua.in.smartdev.incrementalbuild:incrementalbuild-helper-maven-plugin:doNothing"));
	}

	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
		logger.info("[SMART_BUILD] afterSessionStart");
	}

	
	
	
}
