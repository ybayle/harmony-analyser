package org.harmony_analyser.chordanal;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for TonalPitchSpace class
 */

@SuppressWarnings("ConstantConditions")

public class TonalPitchSpaceTest {
	private Key key1, key2, key3;
	private Tone root1, root2;
	private Harmony harmony1, harmony2;

	@Before
	public void setUp() {
		key1 = new Key(0, Chordanal.MAJOR); // C major key
		harmony1 = Chordanal.createHarmonyFromTones("C4 E4 G4"); // C major chord
		root1 = Chordanal.getRootTone(harmony1); // C

		key2 = new Key(0, Chordanal.MAJOR); // C major key
		harmony2 = Chordanal.createHarmonyFromTones("G4 B4 D5 F5"); // G7 chord
		root2 = Chordanal.getRootTone(harmony1); // G

		key3 = new Key(7, Chordanal.MAJOR); // G major key
	}

	@Test
	public void shouldGetRootDistance() {
		assertEquals(1, TonalPitchSpace.getRootDistance(root1, root2));
	}

	@Test
	public void shouldGetKeyDistance() {
		assertEquals(1, TonalPitchSpace.getKeyDistance(key1, key3));
	}

	@Test
	public void shouldGetNonCommonPitchClassesDistance() {
		assertEquals(4.5f, TonalPitchSpace.getNonCommonPitchClassesDistance(harmony1, harmony2, key1), 0.01);
	}
}
