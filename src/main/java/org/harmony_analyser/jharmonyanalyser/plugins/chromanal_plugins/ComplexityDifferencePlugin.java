package org.harmony_analyser.jharmonyanalyser.plugins.chromanal_plugins;

import org.harmony_analyser.jharmonyanalyser.chroma_analyser.*;
import java.util.*;

/**
 * Plugin for high-level audio analysis using chroma input, based on Chromanal model
 */

/*
 * ComplexityDifferencePlugin
 *
 * - requires: chroma
 * - complexity of transition = sum of absolute value of changes
 */

@SuppressWarnings("SameParameterValue")

public class ComplexityDifferencePlugin extends ChromaAnalyserPlugin {
	public ComplexityDifferencePlugin() {
		key = "chroma_analyser:complexity_difference";
		name = "Complexity Difference";

		inputFileSuffixes = new ArrayList<>();
		inputFileSuffixes.add("-chromas");
		inputFileExtension = ".txt";

		outputFileSuffix = "-chroma-complexity-difference";

		parameters = new HashMap<>();

		setParameters();
	}

	public float getChromaComplexity(Chroma previousChroma, Chroma chroma) throws Chroma.WrongChromaSize {
		return Chromanal.getChromaComplexityTonal(previousChroma, chroma, false);
	}
}
