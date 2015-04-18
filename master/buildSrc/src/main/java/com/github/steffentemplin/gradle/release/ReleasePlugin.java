package com.github.steffentemplin.gradle.release;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ReleasePlugin implements Plugin<Project> {

	public void apply(Project project) {
//		DetermineVersion determineVersion = project.getTasks().create("determineVersion", DetermineVersion.class);
//		Task build = project.getTasks().getByName("build");
//		build.mustRunAfter(determineVersion);

		project.beforeEvaluate(new DetermineVersionAction());
	}

}
