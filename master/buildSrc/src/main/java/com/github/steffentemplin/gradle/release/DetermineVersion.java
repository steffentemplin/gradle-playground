package com.github.steffentemplin.gradle.release;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetermineVersion extends DefaultTask {
	
	private static final Logger LOG = LoggerFactory.getLogger(DetermineVersion.class);

	private static final String DEV_QUALIFIER = "DEV";
	
	private static final String RELEASE_QUALIFIER = "REL";

	private static final Version DEFAULT_VERSION = new Version(1, 0, 0, DEV_QUALIFIER);
	
	@TaskAction
	public void perform() throws IOException, GitAPIException {
		Project project = getProject();
		File rootDir = project.getRootDir();
		Git git = Git.open(rootDir.getParentFile());
		String currentBranch = git.getRepository().getBranch();
		
		Version currentVersion;
		if (currentBranch.equals("master")) {
			// TODO: abort?
			currentVersion = handleMaster(git);
		} else if (currentBranch.equals("develop")) {
			currentVersion = handleDevelop(git);
		} else {
			Matcher releaseBranch = getReleaseBranchPattern().matcher(currentBranch);
			if (releaseBranch.matches()) {
				currentVersion = handleRelease(git, Version.parse(releaseBranch.group(1)));
			} else {
				Matcher hotfixBranch = getHotfixBranchPattern().matcher(currentBranch);
				if (hotfixBranch.matches()) {
					currentVersion = handleHotfix(git, Version.parse(hotfixBranch.group(1)));
				} else {
					// behavior is the same for develop, feature branches, etc.
					currentVersion = handleDevelop(git);
				}
			}
		}

		project.setVersion(currentVersion);
		LOG.info("Version was set to " + currentVersion + " for project " + project.getName());
	}
	
	private Version handleMaster(Git git) throws GitAPIException {
		Version lastRelease = getLastRelease(git);
		if (lastRelease == null) {
			return DEFAULT_VERSION;
		}
		
		return incrementDev(lastRelease);
	}
	
	private Version handleDevelop(Git git) throws GitAPIException {
		Version lastRelease = getLastRelease(git);
		Version nextRelease = getNextRelease(git);
		if (lastRelease == null) {
			if (nextRelease == null) {
				return DEFAULT_VERSION;
			} else {
				return incrementDev(nextRelease);
			}
		} else {
			if (nextRelease == null) {
				return incrementDev(lastRelease);
			} else {
				if (lastRelease.compareTo(nextRelease) < 0) {
					return incrementDev(nextRelease);
				} else {
					return incrementDev(lastRelease);
				}
			}
		}
	}
	
	private Version handleHotfix(Git git, Version version) throws IOException {
		if (isReleaseBuild()) {
			version.setQualifier(RELEASE_QUALIFIER);
		} else {
			version.setQualifier(DEV_QUALIFIER);
		}
		
		return version;
	}
	
	private Version handleRelease(Git git, Version version) throws IOException {
		if (isReleaseBuild()) {
			version.setQualifier(RELEASE_QUALIFIER);
		} else {
			version.setQualifier(DEV_QUALIFIER);
		}
		
		return version;
	}
	
	private boolean isReleaseBuild() {
		return getProject().hasProperty("release");
	}
	
	private Version getLastRelease(Git git) throws GitAPIException {
		Version lastRelease = null;
		Pattern releaseTag = getReleaseTagPattern();
		List<Ref> tags = git.tagList().call();
		for (Ref tag : tags) {
			Matcher matcher = releaseTag.matcher(tag.getName());
			if (matcher.matches()) {
				Version version = Version.parse(matcher.group(1));
				if (lastRelease == null || version.compareTo(lastRelease) > 0) {
					lastRelease = version;
				}
			}
		}
		
		return lastRelease;
	}
	
	private Version getNextRelease(Git git) throws GitAPIException {
		Version nextRelease = null;
		Pattern releaseBranch = getReleaseBranchPattern();
		List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call(); // TODO: list remote branches only
		for (Ref branch : branches) {
			String branchName = branch.getName();
			int idx = branchName.lastIndexOf('/');
			if (idx >= 0) {
				try {
					branchName = branchName.substring(idx + 1);
				} catch (IndexOutOfBoundsException e) {
					LOG.warn("Error while parsing branch name", e);
					continue;
				}
			}
			
			Matcher matcher = releaseBranch.matcher(branchName);
			if (matcher.matches()) {
				Version version = Version.parse(matcher.group(1));
				if (nextRelease == null || version.compareTo(nextRelease) > 0) {
					nextRelease = version;
				}
			}
		}
		
		return nextRelease;
	}
	
	private static Version incrementDev(Version version) {
		Version newVersion = version.clone();
		newVersion.incMinor();
		newVersion.setQualifier(DEV_QUALIFIER);
		return newVersion;
	}
	
	private Pattern getReleaseBranchPattern() {
		return Pattern.compile(getProject().getName() + "-release-(" + Version.VERSION_PATTERN + ')');
	}
	
	private Pattern getHotfixBranchPattern() {
		return Pattern.compile(getProject().getName() + "-hotfix-(" + Version.VERSION_PATTERN + ')');
	}
	
	private Pattern getReleaseTagPattern() {
		return Pattern.compile(getProject().getName() + "-(" + Version.VERSION_PATTERN + ')');
	}

}
