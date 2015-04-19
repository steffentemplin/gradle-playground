package com.github.steffentemplin.gradle.release;

import static com.github.steffentemplin.gradle.release.Version.DEFAULT_VERSION;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitVersionProvider implements VersionProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(GitVersionProvider.class);

	private final Project project;

	public GitVersionProvider(Project project) {
		super();
		this.project = project;
	}

	@Override
	public Version getBuildVersion() throws GradleException {
		try {
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
					currentVersion = Version.parse(releaseBranch.group(1));
				} else {
					Matcher hotfixBranch = getHotfixBranchPattern().matcher(currentBranch);
					if (hotfixBranch.matches()) {
						currentVersion = Version.parse(hotfixBranch.group(1));
					} else {
						// behavior is the same for develop, feature branches, etc.
						currentVersion = handleDevelop(git);
					}
				}
			}
			
			return currentVersion;
		} catch (IOException e) {
			throw new GradleException("Could not determine version for project " + project.getName(), e);
		} catch (GitAPIException e) {
			throw new GradleException("Could not determine version for project " + project.getName(), e);
		}
	}
	
	private Version handleMaster(Git git) throws GitAPIException {
		Version lastRelease = getLastRelease(git);
		if (lastRelease == null) {
			return DEFAULT_VERSION;
		}
		
		return increment(lastRelease);
	}
	
	private Version handleDevelop(Git git) throws GitAPIException {
		Version lastRelease = getLastRelease(git);
		Version nextRelease = getNextRelease(git);
		if (lastRelease == null) {
			if (nextRelease == null) {
				return DEFAULT_VERSION;
			} else {
				return increment(nextRelease);
			}
		} else {
			if (nextRelease == null) {
				return increment(lastRelease);
			} else {
				if (lastRelease.compareTo(nextRelease) < 0) {
					return increment(nextRelease);
				} else {
					return increment(lastRelease);
				}
			}
		}
	}
	
	private Version getLastRelease(Git git) throws GitAPIException {
		Version lastRelease = null;
		Pattern releaseTag = getReleaseTagPattern();
		List<Ref> tags = git.tagList().call();
		for (Ref tag : tags) {
			String tagName = tag.getName();
			int idx = tagName.lastIndexOf('/');
			if (idx >= 0) {
				try {
					tagName = tagName.substring(idx + 1);
				} catch (IndexOutOfBoundsException e) {
					LOG.warn("Error while parsing tag name", e);
					continue;
				}
			}
			
			Matcher matcher = releaseTag.matcher(tagName);
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
	
	private Pattern getReleaseBranchPattern() {
		return Pattern.compile(project.getName() + "-release-(" + Version.VERSION_PATTERN + ')');
	}
	
	private Pattern getHotfixBranchPattern() {
		return Pattern.compile(project.getName() + "-hotfix-(" + Version.VERSION_PATTERN + ')');
	}
	
	private Pattern getReleaseTagPattern() {
		return Pattern.compile(project.getName() + "-(" + Version.VERSION_PATTERN + ')');
	}
	
	private static Version increment(Version version) {
		Version newVersion = version.clone();
		newVersion.incrementMinor();
		newVersion.resetMicro();
		return newVersion;
	}

}
