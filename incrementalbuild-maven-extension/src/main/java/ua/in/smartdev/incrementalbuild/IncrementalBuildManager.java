package ua.in.smartdev.incrementalbuild;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import static ua.in.smartdev.incrementalbuild.Constants.*;

@Component(role = IncrementalBuildManager.class)
public class IncrementalBuildManager {

	@Requirement
	Logger logger;
	
	public boolean checkIfProjectUpToDate(MavenProject project) {
		
		return false;
	}
}
