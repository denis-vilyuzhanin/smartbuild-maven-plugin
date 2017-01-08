package ua.in.smartdev.incrementalbuild;

import java.util.Arrays;
import java.util.List;
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

	@Parameter(name = "defaultMode", defaultValue = "INCREMENTAL")
	private BuildMode defaultMode = BuildMode.INCREMENTAL;

	@Parameter(name = "mode", property = "incrementalbuild")
	private BuildMode mode;
	
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
		MavenProject currentRootProject = session.getCurrentProject();
		currentRootProject.getContextValue(INCREMENTAL_BUILD_ENABLED);
		for (MavenProject project : session.getAllProjects()) {
			logger.info("[SMART_BUILD] project: " + project.getArtifactId() + " plugins: " + project.getBuildPlugins());
			project.getModel().getBuild().setPlugins(Arrays.asList(helperPlugin));
		}
	}
	
	private boolean isIncrementalBuildEnabled(MavenSession session) {
		List<Properties> allProperties = Arrays.asList(
				session.getUserProperties(),
				session.getSystemProperties(),
				session.getCurrentProject().getModel().getProperties());
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
