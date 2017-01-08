package ua.in.smartdev.incrementalbuild;

import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import static ua.in.smartdev.incrementalbuild.Constants.*;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "incrementalbuild")
public class IncrementalBuildLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
	Logger logger;

	private boolean enabled = true;

	@Parameter(name = "defaultMode", defaultValue = "INCREMENTAL")
	private BuildMode defaultMode = BuildMode.INCREMENTAL;

	@Parameter(name = "mode", property = "incrementalbuild")
	private BuildMode mode;
	
	private void readProperties(MavenSession session) {
		Properties sessionProps = session.getUserProperties();
		
		if (sessionProps.containsKey(INCREMENTAL_BUILD_ENABLED)) {
			enabled = Boolean.parseBoolean(
					sessionProps.getProperty(INCREMENTAL_BUILD_ENABLED, INCREMENTAL_BUILD_ENABLED_DEFAULT));
		} else if (sessionProps.containsKey("incrementalbuild.enabled")) {
			enabled = Boolean.parseBoolean(
					sessionProps.getProperty(INCREMENTAL_BUILD_ENABLED_ALIAS, INCREMENTAL_BUILD_ENABLED_DEFAULT));
		}
		
		
	}

	@Override
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		logger.info("Incremental Build");
		readProperties(session);
		
		if (!enabled) {
			if (logger.isDebugEnabled()) {
				logger.debug("Incremental build is disabled. incrementalbuild.enabled=" + enabled);
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

		for (MavenProject project : session.getAllProjects()) {
			logger.info("[SMART_BUILD] project: " + project.getArtifactId() + " plugins: " + project.getBuildPlugins());
			project.getModel().getBuild().setPlugins(Arrays.asList(helperPlugin));
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public BuildMode getDefaultMode() {
		return defaultMode;
	}

	public void setDefaultMode(BuildMode defaultMode) {
		this.defaultMode = defaultMode;
	}

	public BuildMode getMode() {
		return mode;
	}

	public void setMode(BuildMode mode) {
		this.mode = mode;
	}

}
