package com.github.steffentemplin.gradle.release;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version>, Cloneable {
	
	static final String SNAPSHOT_QUALIFIER = "DEV";
	
	static final String RELEASE_QUALIFIER = "REL";
	
	static final Version DEFAULT_VERSION = new Version(1, 0, 0, SNAPSHOT_QUALIFIER);
	
	static final String VERSION_PATTERN = "(([1-9][0-9]*|0))\\.(([1-9][0-9]*|0))\\.(([1-9][0-9]*|0))(\\.([0-9a-zA-Z-_\\.]+))?";
	
	private static final Pattern VERSION = Pattern.compile(VERSION_PATTERN);

	private int major;

	private int minor;

	private int micro;

	private String qualifier;
	
	public Version(int major, int minor, int micro, String qualifier) {
		super();
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.qualifier = qualifier;
	}
	
	public static Version parse(String versionString) {
		Matcher matcher = VERSION.matcher(versionString);
		if (matcher.matches()) {
			try {
				int major = Integer.parseInt(matcher.group(1));
				int minor = Integer.parseInt(matcher.group(3));
				int micro = Integer.parseInt(matcher.group(5));
				String qualifier = matcher.group(8);
				return new Version(major, minor, micro, qualifier);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid version string: " + versionString, e);
			}
		}
		
		throw new IllegalArgumentException("Invalid version string: " + versionString);
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public int getMicro() {
		return micro;
	}
	
	public String getQualifier() {
		return qualifier;
	}
	
	public void incrementMajor() {
		major++;
	}
	
	public void incrementMinor() {
		minor++;
	}
	
	public void incrementMicro() {
		micro++;
	}
	
	public void resetMajor() {
		major = 0;
	}
	
	public void resetMinor() {
		minor = 0;
	}
	
	public void resetMicro() {
		micro = 0;
	}
	
	public void removeQualifier() {
		qualifier = null;
	}
	
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * Compares two versions based on the algorithm described in the
	 * OSGi core specification, section 3.6.3.
	 */
	public int compareTo(Version other) {
		if (other == null) {
			return 1;
		}
		
		if (major == other.major) {
			if (minor == other.minor) {
				if (micro == other.micro) {
					if (qualifier == null) {
						if (other.qualifier != null) {
							return 1;
						}
						
						return 0;
					} else {
						if (other.qualifier == null) {
							return -1;
						}
						
						return qualifier.compareTo(other.qualifier);
					}
				}
				
				return micro - other.micro;
			}
			
			return minor - other.minor;
		}
		
		return major - other.major;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Version) {
			Version other = (Version) obj;
			return compareTo(other) == 0;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		String baseVersion = String.format("%d.%d.%d", major, minor, micro);
		if (qualifier == null) {
			return baseVersion;
		}
		
		return baseVersion + '.' + qualifier;
	}
	
	@Override
	protected Version clone() {
		return new Version(major, minor, micro, qualifier);
	}

}
