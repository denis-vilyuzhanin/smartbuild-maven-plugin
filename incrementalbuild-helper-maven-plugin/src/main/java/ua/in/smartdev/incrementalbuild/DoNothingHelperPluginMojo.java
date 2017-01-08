package ua.in.smartdev.incrementalbuild;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "doNothing", defaultPhase=LifecyclePhase.INITIALIZE)
public class DoNothingHelperPluginMojo extends AbstractMojo {
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info( "Everything up-to-date" );
	}

}
