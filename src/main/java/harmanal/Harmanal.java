package harmanal;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

// TODO: Complete Harmony complexity (Complexity of a single tone and single tone + added tone)
// TODO: Complete Transition complexity (Complexity of non-diatonic modulations)
// TODO: Create AudioAnalyser - class for analysis of audio using Vamp plugins + Chordanal + Harmanal (Harmanal plugins)

/**
 * Class to handle all functional complexity and transition complexity
 * 
 * version 1.0
 */

public class Harmanal {
	
	/* Roots finding */
	
	/**
	 * Gets function roots table for a harmony
	 */
	
	public static DatabaseTable getRoots(Harmony harmony) {
		
		Key key;
		DatabaseTable result = new DatabaseTable();
		
		// try all Major keys
		for (int i = 0; i < 12; i++) {
			key = new Key(i,Chordanal.MAJOR);
			result.addAll(getRoots(harmony,key));
		}
		
		// try all Minor keys
		for (int i = 0; i < 12; i++) {
			key = new Key(i,Chordanal.MINOR);
			result.addAll(getRoots(harmony,key));
		}
		
		return result;
	}
	
	/**
	 * Gets function roots table for a harmony for specific key
	 */
	
	public static DatabaseTable getRoots(Harmony harmony, Key key) {
		
		DatabaseTable result = new DatabaseTable();
		DatabaseTable rows;
		
		if ((rows = getRoots(harmony,key.getTonic(),Chordanal.TONIC,key)) != null) {
			result.addAll(rows);
		}
		if ((rows = getRoots(harmony,key.getSubdominant(),Chordanal.SUBDOMINANT,key)) != null) {
			result.addAll(rows);
		}
		if ((rows = getRoots(harmony,key.getDominant(),Chordanal.DOMINANT,key)) != null) {
			result.addAll(rows);
		}
		
		return result;
	}
	
	/**
	 * Gets function roots table for a harmony for specific key and function
	 */
	
	public static DatabaseTable getRoots(Harmony harmony, Harmony function, int functionSign, Key key) {
		DatabaseTable result = new DatabaseTable();
		Harmony common;
		
		common = harmony.getCommonTones(function);
		
		if (common.tones.size() == 3) {
			result.add(Chordanal.getKeyName(key) + "," + Chordanal.functionNameTable.getFirstInValue(Chordanal.functionTable.getFirstInValue(Integer.toString(functionSign))) + "," + common.getToneNamesMapped() + ";" + getHarmonyComplexity(harmony, Chordanal.createHarmonyFromRelativeTones(common.getToneNamesMapped()),key));
		} else if (common.tones.size() == 2) {
			if ((common.tones.get(0).getNumberMapped() == Chordanal.getRootTone(function).getNumberMapped()) || (common.tones.get(1).getNumberMapped() == Chordanal.getRootTone(function).getNumberMapped())) {
				result.add(Chordanal.getKeyName(key) + "," + Chordanal.functionNameTable.getFirstInValue(Chordanal.functionTable.getFirstInValue(Integer.toString(functionSign))) + "," + common.getToneNamesMapped() + ";" + getHarmonyComplexity(harmony, Chordanal.createHarmonyFromRelativeTones(common.getToneNamesMapped()),key));
			}
		}
		return result;
		
	}
	
	/**
	 * Gets function roots table for a harmony, sorted by distance and formatted
	 */
	
	public static List<String> getRootsFormatted(Harmony harmony) {
		List<String> result = new ArrayList<String>();
		
		DatabaseTable roots = getRoots(harmony).sortByValueByFirstNumeric();
		
		if (!roots.isEmpty()) {
			for (List<String> key : roots.getAllKeys()) {
				for (String value : roots.getValues(key.get(0), key.get(1))) {
					result.add(key.get(0) + " (" + key.get(1) + ")\nroot: " + key.get(2) + " steps: " + value);
				}	
			}
		}
		
		return result;
	}
	
	/* Harmony derivation and complexity */
	
	/**
	 * Gets the derivation from the root to the harmony in a given key
	 */
	
	public static List<String> getHarmonyDerivation(Harmony harmony, Harmony root, Key key) {
		
		return getHarmonyDerivation(harmony, root, harmony.subtractTones(root), key);
	}
	
	/**
	 * Gets the derivation from the root to the harmony in a given key, specifying the order of adding tones
	 */
	
	public static List<String> getHarmonyDerivation(Harmony harmony, Harmony root, List<Tone> added, Key key) {

		List<String> result = new ArrayList<String>();
		Harmony phraseForm = Chordanal.createHarmonyFromRelativeTones(root.getToneNamesMapped());
		
		result.add(root.getToneNamesMapped());
		
		boolean special = false;
		for (Tone tone : added) {
			if (key.getScaleHarmony().containsMapped(tone)) {
				// ADD operator step
				
				special = false;
				if (getRootCompletionTone(root, key) != null) {
					if ((root.tones.size() == 2) && (tone.getNumberMapped() == getRootCompletionTone(root, key).getNumberMapped())) {
						special = true;
					}
				}
				if (special) {
					// special situation - adding tone which belongs to root - for free

					phraseForm.tones.add(tone);
				} else {
				
					phraseForm.tones.add(tone);
					result.add(phraseForm.getToneNamesMapped());
				}
			} else {
				// ADD + CHROMATIZE operators steps
				
				// ADD
				Tone diatonicTone = new Tone(tone.getNumber()-1);
				
				special = false;
				for (Tone rootTone : root.tones) {
					if (diatonicTone.getNumberMapped() == rootTone.getNumberMapped()) {
						special = true;
					}
				}
				if (getRootCompletionTone(root, key) != null) {
					if (diatonicTone.getNumberMapped() == getRootCompletionTone(root, key).getNumberMapped()) {
						special = true;
					}
				}
				if (special) {
					// special situation - one semitone down is one of the tones of root - need to chromatize from up to down
					
					diatonicTone.chromatizeUp();
					diatonicTone.chromatizeUp();
				}
				
				phraseForm.tones.add(diatonicTone);
				result.add(phraseForm.getToneNamesMapped());
				
				// CHROMATIZE
				
				if (special) {
					phraseForm.tones.get(phraseForm.tones.size()-1).chromatizeDown();
				} else {
					phraseForm.tones.get(phraseForm.tones.size()-1).chromatizeUp();
				}
				result.add(phraseForm.getToneNamesMapped());
				
			}
					
		}
		
		return result;
	}
	
	/**
	 * Gets list of all possible derivations from the root to the harmony in a given key
	 */
	
	public static List<List<String>> getHarmonyDerivations(Harmony harmony, Harmony root, Key key) {
		List<Tone> blank = new ArrayList<Tone>();
		List<Tone> added = harmony.subtractTones(root);

		List<List<Tone>> permutations = new ArrayList<List<Tone>>();
		
		permutateListOfTones(blank, added, permutations);
		
		List<List<String>> result = new ArrayList<List<String>>();
		
		for (List<Tone> list : permutations) {
			
			result.add(getHarmonyDerivation(harmony, root, list, key));
		}
		
		return result;
	}
	
	/**
	 * Gets the harmony complexity of the harmony from the root in a given key
	 */
	
	public static int getHarmonyComplexity(Harmony harmony, Harmony root, Key key) {
		
		return getHarmonyDerivation(harmony,root,key).size()-1;
	}
	
	/**
	 * Gets the harmony complexity for the harmony or -1 if no roots were found
	 */
	
	public static int getHarmonyComplexity(Harmony harmony) {
		DatabaseTable roots = getRoots(harmony).sortByValueByFirstNumeric();
		if (roots.isEmpty()) {
			return -1;
		} else {
			return Integer.parseInt(roots.getAllValues().get(0).get(0));
		}
	}
	
	/* Transition complexity */
	
	/**
	 * Gets the common roots table for two harmonies
	 */
	
	public static DatabaseTable getCommonRoots(Harmony harmony1, Harmony harmony2) {
		
		DatabaseTable roots1 = getRoots(harmony1).sortByValueByFirstNumeric();
		DatabaseTable roots2 = getRoots(harmony2).sortByValueByFirstNumeric();

		DatabaseTable rootsTemporary = roots1.naturalJoinByFirstAndSecond(roots2);
		
		List<String> commonRootsRows = rootsTemporary.getAll();
		DatabaseTable result = new DatabaseTable();
		for (String row : commonRootsRows) {
			String[] keyValue = row.split(";");
			String[] key = keyValue[0].split(",");
			String[] value = keyValue[1].split(",");
			Harmony firstOption = Chordanal.createHarmonyFromRelativeTones(key[2]);
			Harmony secondOption = Chordanal.createHarmonyFromRelativeTones(key[3]);
			if (firstOption.containsMapped(secondOption)) {
				result.add(key[0] + "," + key[1] + "," + key[3] + ";" + value[0] + "," + value[1]);
			} else if (secondOption.containsMapped(firstOption)) {
				result.add(key[0] + "," + key[1] + "," + key[2] + ";" + value[0] + "," + value[1]);
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the common roots table for two harmonies grouped by the key 
	 */
	
	public static DatabaseTable getCommonRootsByKey(Harmony harmony1, Harmony harmony2) {
		
		DatabaseTable roots1 = getRoots(harmony1).sortByValueByFirstNumeric();
		DatabaseTable roots2 = getRoots(harmony2).sortByValueByFirstNumeric();
		
		DatabaseTable result = roots1.naturalJoinByFirst(roots2);
		
		return result;
	}
	
	/**
	 * Gets the common ancestors table for two harmonies
	 */
	
	public static DatabaseTable getCommonAncestors(Harmony harmony1, Harmony harmony2) {
		
		DatabaseTable commonRoots = getCommonRoots(harmony1, harmony2);
		
		List<String> commonRootsRows = commonRoots.getAll();
		
		DatabaseTable result = new DatabaseTable();
		
		for (String row : commonRootsRows) {
			String[] keyValue = row.split(";");
			String[] key = keyValue[0].split(",");
			
			Harmony root = Chordanal.createHarmonyFromRelativeTones(key[2]);
			Key commonKey = Chordanal.createKeyFromName(key[0]);
			
			List<String> rightDerivation1 = null, rightDerivation2 = null;
			int closestAncestor = 0;
			for (List<String> derivation1 : getHarmonyDerivations(harmony1, root, commonKey)) {
				for (List<String> derivation2 : getHarmonyDerivations(harmony2, root, commonKey)) {
					int commonMovesNumber;
					if (derivation1.size() < derivation2.size()) {
						commonMovesNumber = derivation1.size();
					} else {
						commonMovesNumber = derivation2.size();
					}
					int indexOfCommonAncestor = -1;
					for (int i = 0; i < commonMovesNumber; i++) {
						if (!derivation1.get(i).equals(derivation2.get(i))) {
							break;
						}
						indexOfCommonAncestor++;
					}
					if (indexOfCommonAncestor >= closestAncestor) {
						closestAncestor = indexOfCommonAncestor;
						rightDerivation1 = derivation1;
						rightDerivation2 = derivation2;
					}
				}
			}
			result.add(key[0] + "," + key[1] + "," + rightDerivation1.get(closestAncestor) + ";" + (rightDerivation1.size() - closestAncestor -1) + "," + (rightDerivation2.size() - closestAncestor -1));
		}
		
		return result;
	}

	/**
	 * Gets the transitions table for two harmonies
	 */
	
	public static DatabaseTable getTransitions(Harmony harmony1, Harmony harmony2) {
		
		DatabaseTable result = new DatabaseTable();
		
		// Transition between T/S/D
		DatabaseTable commonKeys = getCommonRootsByKey(harmony1, harmony2);
		List<List<String>> keys1 = commonKeys.getAllKeys();
		List<List<String>> values1 = commonKeys.getAllValues();
		for (int i = 0; i < keys1.size(); i++) {
			result.add(keys1.get(i).get(0) + "," + keys1.get(i).get(1) + "," + Integer.parseInt(values1.get(i).get(0)) + "," + keys1.get(i).get(2) + "," + Integer.parseInt(values1.get(i).get(1)) + ";" + (Integer.parseInt(values1.get(i).get(0)) + Integer.parseInt(values1.get(i).get(1))));
		}
		
		// Transition amongst T/S/D
		DatabaseTable commonAncestors = getCommonAncestors(harmony1, harmony2);
		List<List<String>> keys2 = commonAncestors.getAllKeys();
		List<List<String>> values2 = commonAncestors.getAllValues();
		for (int i = 0; i < keys2.size(); i++) {
			result.add(keys2.get(i).get(0) + "," + keys2.get(i).get(1) + "," + Integer.parseInt(values2.get(i).get(0)) + "," + keys2.get(i).get(1) + "," + Integer.parseInt(values2.get(i).get(1)) + ";" + (Integer.parseInt(values2.get(i).get(0)) + Integer.parseInt(values2.get(i).get(1))));
		}

		return result;
	}
	
	/**
	 * Gets the transitions table for two harmonies, sorted by distance and formatted
	 */
	
	public static List<String> getTransitionsFormatted(Harmony harmony1, Harmony harmony2) {
		List<String> result = new ArrayList<String>();
			
		DatabaseTable transitions = getTransitions(harmony1,harmony2).sortByValueByFirstNumeric();
		List<List<String>> keys = transitions.getAllKeys();
		List<List<String>> values = transitions.getAllValues();
		
		for (int i = 0; i < keys.size(); i++) {
			result.add(keys.get(i).get(0) + ": " + keys.get(i).get(1) + "->" + keys.get(i).get(3) + " steps: " + values.get(i).get(0));
		}
		
		return result;
	}
	
	/**
	 * Gets the transition complexity for two harmonies or -1 if no common keys were found
	 */
	
	public static int getTransitionComplexity(Harmony harmony1, Harmony harmony2) {
		DatabaseTable transitions = getTransitions(harmony1,harmony2).sortByValueByFirstNumeric();
		
		if (transitions.isEmpty()) {
			return -1;
		} else {
			return Integer.parseInt(transitions.getAllValues().get(0).get(0));
		}
	}
	
	/* Private methods */
	
	/**
	 * Fills all permuation from startList (empty list) to endList (list of Tones) into result
	 */
	
	private static void permutateListOfTones(List<Tone> startList, List<Tone> endList, List<List<Tone>> result) {
		if (endList.size() <= 1) {
			List<Tone> permResult = new ArrayList<Tone>();
			permResult.addAll(startList);
			permResult.addAll(endList);
			result.add(permResult);
		} else {
			for (int i = 0; i < endList.size(); i++) {
				List<Tone> newEndList = new ArrayList<Tone>();
				for ( int j = 0; j < i; j++ ) newEndList.add(endList.get(j));
				for ( int j = i+1; j < endList.size(); j++ ) newEndList.add(endList.get(j));

				List<Tone> newStartList = new ArrayList<Tone>();
				newStartList.addAll(startList);
				newStartList.add(endList.get(i));

				permutateListOfTones(newStartList, newEndList, result);
			}
		}
	}
	
	/**
	 * Gets the remaining tone of the function root if it was not complete
	 */
	
	private static Tone getRootCompletionTone(Harmony root, Key key) {
		if (root.tones.size() != 2) {
			return null;
		}
		Tone result = null;
		if (key.getTonic().containsMapped(root)) {
			for (Tone functionTone : key.getTonic().tones) {
				if (!root.containsMapped(functionTone)) {
					result = functionTone;
				}
			}
		}
		if (key.getSubdominant().containsMapped(root)) {
			for (Tone functionTone : key.getSubdominant().tones) {
				if (!root.containsMapped(functionTone)) {
					result = functionTone;
				}
			}
		}
		if (key.getDominant().containsMapped(root)) {
			for (Tone functionTone : key.getDominant().tones) {
				if (!root.containsMapped(functionTone)) {
					result = functionTone;
				}
			}
		}
		return result;
	}

	/* Audio Analysis */
	// TODO: Move to a service

	public static final float AUDIBLE_THRESHOLD = (float) 0.05;

	// gets timestamp from the first word in the line, before ':'
	private static float getTimestampFromLine(String line) {
		String stringTimestamp = line.substring(0, line.lastIndexOf(':'));
		return Float.parseFloat(stringTimestamp);
	}

	// averages multiple chromas from vector of their sum into one chroma
	private static float[] averageChroma(float[] chromaSums, int countChromas) {
		float[] resultChroma = new float[12];
		for (int i = 0; i < chromaSums.length; i++) {
			resultChroma[i] = chromaSums[i] / countChromas;
		}
		return resultChroma;
	}

	// filters chroma using AUDIBLE_THRESHOLD, setting values below the threshold to 0
	private static float[] filterChroma(float[] chroma) {
		float[] resultChroma = new float[12];
		for (int i = 0; i < chroma.length; i++) {
			if (chroma[i] < AUDIBLE_THRESHOLD) {
				resultChroma[i] = 0;
			} else {
				resultChroma[i] = chroma[i];
			}
		}
		return resultChroma;
	}

	/**
	 * Analyzes the song
	 *
	 * @param chromaFile [String] name of the file containing chroma information
	 * @param segmentationFile [String] name of the file containing segmentation information
	 * @param resultFile [String] name of the file to write a result
	 * @param reportFile [String] name of the file to write a report
	 * @param timestampsFile [String] name of the file to write a by-product of additional timestamps segmentation information
	 */

	public static void analyzeSong(String chromaFile, String segmentationFile, String resultFile, String reportFile, String timestampsFile) throws IOException {
		List<String> chromaLinesList = Files.readAllLines(new File(chromaFile).toPath(), Charset.defaultCharset());
		List<String> segmenationLinesList = Files.readAllLines(new File(segmentationFile).toPath(), Charset.defaultCharset());
		List<Float> segmenatationTimestampList = new ArrayList<Float>();

		// 1. Get timestamps from the segmentation file
		for (String line : segmenationLinesList) {
			segmenatationTimestampList.add(getTimestampFromLine(line));
		}

		String[] chromaString = new String[12];
		float chromaTimestamp;
		float[] chroma = new float[12];
		float[] chromaSums = new float[12];
		Arrays.fill(chromaSums, (float) 0);
		float[] chromaVector;
		int[] harmony = new int[12];
		Arrays.fill(harmony, 0);
		List<List<Integer>> harmonies = new ArrayList<List<Integer>>();
		List<Float> timestamps = new ArrayList<Float>();
		int countChromasForAveraging = 0;
		int segmentationIndex = 0;
		float segmenatationTimestamp;
		segmenatationTimestamp = segmenatationTimestampList.get(0);

		// Iterate over chromas
		for (String line : chromaLinesList) {
			chromaTimestamp = getTimestampFromLine(line);

			if (chromaTimestamp > segmenatationTimestamp) {
				// Go to the next segmentation timestamp
				segmentationIndex++;
				if (segmentationIndex > segmenatationTimestampList.size()-1) {
					break;
				}
				segmenatationTimestamp = segmenatationTimestampList.get(segmentationIndex);

				chromaVector = filterChroma(averageChroma(chromaSums, countChromasForAveraging));
				Arrays.fill(chromaSums, (float) 0);
				countChromasForAveraging = 0;

				// Create the chord
				float max = 0;
				int id = 0;
				for(int g=0;g<4;g++) {
					max=0;
					id=0;
					for (int i = 0; i < chromaVector.length; i++) {
						if (chromaVector[i] > max) {
							id = i;
							max = chromaVector[i];
						}
					}
					if (chromaVector[id] > 0) {
						harmony[id] = 1;
					}
					chromaVector[id] = 0;
				}
				// DEBUG
				//for (int i = 0; i < harmony.length; i++) {
					//System.out.print(harmony[i] + " ");
				//}	
				//System.out.println();

				List<Integer> harmonyList = new ArrayList<Integer>();
				for (int i = 0; i < harmony.length; i++) {
					harmonyList.add(harmony[i]);
				}

				harmonies.add(harmonyList);
				timestamps.add(segmenatationTimestamp);

				for (int i = 0; i < harmony.length; i++) {
					harmony[i] = 0;
				}
			}

			Scanner sc = new Scanner(line);
			sc.next();
			chromaString[0] = sc.next();
			chromaString[1] = sc.next();
			chromaString[2] = sc.next();
			chromaString[3] = sc.next();
			chromaString[4] = sc.next();
			chromaString[5] = sc.next();
			chromaString[6] = sc.next();
			chromaString[7] = sc.next();
			chromaString[8] = sc.next();
			chromaString[9] = sc.next();
			chromaString[10] = sc.next();
			chromaString[11] = sc.next();

			chroma[0] = Float.parseFloat(chromaString[0]);
			chroma[1] = Float.parseFloat(chromaString[1]);
			chroma[2] = Float.parseFloat(chromaString[2]);
			chroma[3] = Float.parseFloat(chromaString[3]);
			chroma[4] = Float.parseFloat(chromaString[4]);
			chroma[5] = Float.parseFloat(chromaString[5]);
			chroma[6] = Float.parseFloat(chromaString[6]);
			chroma[7] = Float.parseFloat(chromaString[7]);
			chroma[8] = Float.parseFloat(chromaString[8]);
			chroma[9] = Float.parseFloat(chromaString[9]);
			chroma[10] = Float.parseFloat(chromaString[10]);
			chroma[11] = Float.parseFloat(chromaString[11]);

			for (int i = 0; i < chromaSums.length; i++) {
				chromaSums[i] = chromaSums[i] + chroma[i];
				countChromasForAveraging++;
			}

			// DEBUG
			/*
			for (int i = 0; i < chromaWork.length; i++) {
				if (chroma[i] < 0.1) {
					chromaWork[i] = 0;
				} else {
					chromaWork[i] = chroma[i];
				}
			}*/
			//System.out.println("timestamp: " + timestamp);
			//for (int j = 0; j < chromaString.length; j++) {
				//System.out.print(chromaString[j]+ " ");
			//}
			//System.out.println();
			//for (int j = 0; j < chroma.length; j++) {
				//System.out.print(chroma[j]+ " ");
			//}
			//System.out.println();
			//for (int j = 0; j < chromaWork.length; j++) {
				//System.out.print(chromaWork[j]+ " ");
			//}
			//System.out.println();
		}
		
		int[] chord = new int[12];
		int[] previousChord = new int[12];
		for (int i = 0; i < previousChord.length; i++) {
			previousChord[i] = harmonies.get(0).get(i);
		}
		
		List<Integer> tcs = new ArrayList<Integer>();
		List<Integer> cs = new ArrayList<Integer>();
		int sumTS = 0;
		int sumCS = 0;
		int maxTS = 0;
		int maxCS = 0;
		int sumTones = 0;
		int counter = 0;
		
		FileWriter fstream2;
		try {
			fstream2 = new FileWriter(reportFile);
			BufferedWriter out2 = new BufferedWriter(fstream2);

			for (List<Integer> harmie : harmonies) {

				for (int i = 0; i < chord.length; i++) {
					chord[i] = harmie.get(i);
				}
				for (int i = 0; i < chord.length; i++) {
					out2.write(chord[i] + " "); // VERBOSE
				}
				out2.write("\n"); //VERBOSE

				// number of tones
				int numberTones = 0;
				for (int i = 0; i < chord.length; i++) {
					if (chord[i] == 1) {
						numberTones++;
					}
				}
				sumTones += numberTones;

				String chordTones = "";
				for (int i = 0; i < chord.length; i++) {
						if (chord[i] == 1) {
							chordTones += Chordanal.tonesNames.getFirstInValue(Integer.toString(i)) + " ";
						}
				}

				String previousChordTones = "";
				for (int i = 0; i < previousChord.length; i++) {
					if (previousChord[i] == 1) {
						previousChordTones += Chordanal.tonesNames.getFirstInValue(Integer.toString(i)) + " ";
					}
				}
				counter++;
				out2.write("previous: " + previousChordTones + "\n"); // VERBOSE
				out2.write(counter + ": " + chordTones + "\n"); // VERBOSE

				Harmony harmony1 = Chordanal.createHarmonyFromRelativeTones(previousChordTones);
				Harmony harmony2 = Chordanal.createHarmonyFromRelativeTones(chordTones);

				if ((harmony1 == null) || (harmony2 == null)) {
					out2.write("SKIP HARMONIES!\n"); // VERBOSE
				} else {
					int compl = getTransitionComplexity(harmony1, harmony2);
					if (compl == -1) {
						out2.write("NO COMMON ROOTS -> MAX COMPLEXITY = 7!\n"); // VERBOSE
						compl = 7;
					}
					tcs.add(compl);
					int compl2 = getHarmonyComplexity(harmony2);

					cs.add(compl2);
					sumTS += tcs.get(tcs.size()-1);
					sumCS += cs.get(cs.size()-1);
					if (tcs.get(tcs.size()-1) > maxTS) {
						maxTS = tcs.get(tcs.size()-1);
					}
					if (cs.get(cs.size()-1) > maxCS) {
						maxCS = cs.get(cs.size()-1);
					}
					out2.write("ts:" + tcs.get(tcs.size()-1) + "\n"); // VERBOSE

				}

				// set previous
				for (int i = 0; i < chord.length; i++) {
					previousChord[i] = chord[i];
				}
			}
			out2.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		FileWriter fstream3;
		try {
			fstream3 = new FileWriter(timestampsFile);
			
			BufferedWriter out3 = new BufferedWriter(fstream3);
			
			int counternew = 0;
			for (Float time:timestamps) {
				counternew++;
				out3.write(counternew + ": " + time + "\n");
			}
			
			out3.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		float atc = (float) sumTS  / (float) tcs.size();
		float ahc = (float) sumCS  / (float) cs.size();
		float rtc = (float) sumTS / (float) sumTones;

		FileWriter fstream;
		try {
			fstream = new FileWriter(resultFile);
			BufferedWriter out = new BufferedWriter(fstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileWriter fstream1;
		try {
			fstream1 = new FileWriter(resultFile);
			
			BufferedWriter out = new BufferedWriter(fstream1);
			// DEBUG
			//out.write("ATC: " + atc + " ");
			out.write(atc + " ");
			if (atc>3) {
				
				System.out.println("High ATC!: " + atc);
			}	
			if (atc<1.5) {
				
				System.out.println("Low ATC!: " + atc);
			}
			// DEBUG
			//out.write("AHC: " + ahc + " ");
			//out.write("MTC: " + maxTS+ " ");
			//out.write("MHC: " + maxCS+ " ");
			//out.write("RTC: " + rtc + " ");

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
