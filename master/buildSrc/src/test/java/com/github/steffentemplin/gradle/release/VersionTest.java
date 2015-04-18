package com.github.steffentemplin.gradle.release;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VersionTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testQualifiedVersion() {
		String versionString = "1.2.3.SNAPSHOT";
		Version version = Version.parse(versionString);
		assertEquals(versionString, version.toString());
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertEquals("SNAPSHOT", version.getQualifier());
	}

	@Test
	public void testUnqualifiedVersion() {
		String versionString = "1.2.3";
		Version version = Version.parse(versionString);
		assertEquals(versionString, version.toString());
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getMicro());
		assertNull(version.getQualifier());
	}
	
	@Test
	public void testBigNumbers() {
		String versionString = "10.42.1337.RELEASE";
		Version version = Version.parse(versionString);
		assertEquals(versionString, version.toString());
		assertEquals(10, version.getMajor());
		assertEquals(42, version.getMinor());
		assertEquals(1337, version.getMicro());
		assertEquals("RELEASE", version.getQualifier());
	}

	@Test
	public void testInvalidVersion1() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1");
	}

	@Test
	public void testInvalidVersion2() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("01.2.3");
	}
	
	@Test
	public void testInvalidVersion3() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.02.3");
	}

	@Test
	public void testInvalidVersion4() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.2.03");
	}

	@Test
	public void testInvalidVersion5() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.2");
	}

	@Test
	public void testInvalidVersion6() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.2.3.");
	}

	@Test
	public void testInvalidVersion7() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("a.2.3");
	}

	@Test
	public void testInvalidVersion8() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.b.3");
	}

	@Test
	public void testInvalidVersion9() {
		thrown.expect(IllegalArgumentException.class);
		Version.parse("1.2.c");
	}

	@Test
	public void testMajorComparison() {
		assertTrue(Version.parse("1.0.0").compareTo(Version.parse("2.0.0")) < 0);
	}
	
	@Test
	public void testMinorComparison() {
		assertTrue(Version.parse("1.1.0").compareTo(Version.parse("1.2.0")) < 0);
	}
	
	@Test
	public void testMicroComparison() {
		assertTrue(Version.parse("2.3.4").compareTo(Version.parse("2.3.5")) < 0);
	}
	
	@Test
	public void testQualifierPrecedence() {
		assertTrue(Version.parse("1.6.9.REL").compareTo(Version.parse("1.6.9.DEV")) > 0);
	}

}
