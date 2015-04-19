package com.github.steffentemplin.gradle.release;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetermineVersionAction implements Action<Project> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DetermineVersionAction.class);

	@Override
	public void execute(Project project) {
		Version buildVersion = getVersionProvider(project).getBuildVersion();
		if (isReleaseBuild(project)) {
			buildVersion.setQualifier(Version.RELEASE_QUALIFIER);
		} else {
			buildVersion.setQualifier(Version.SNAPSHOT_QUALIFIER);
		}
		
		project.setVersion(buildVersion);
		LOG.info("Version was set to " + buildVersion + " for project " + project.getName());
	}
	
	private VersionProvider getVersionProvider(Project project) {
		return new GitVersionProvider(project);
	}
	
	private boolean isReleaseBuild(Project project) {
		return project.hasProperty("release");
	}
	
}
