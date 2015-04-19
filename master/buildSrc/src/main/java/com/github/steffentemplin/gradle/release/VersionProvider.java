package com.github.steffentemplin.gradle.release;

import org.gradle.api.GradleException;

public interface VersionProvider {
	
	/**
	 * Gets the version number for the build of this project. The qualifier
	 * will be overridden based on if this is a release or snapshot build.
	 *
	 * @return The version
	 * @throws GradleException
	 */
	Version getBuildVersion() throws GradleException;

}
