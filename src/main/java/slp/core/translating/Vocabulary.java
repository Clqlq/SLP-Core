package slp.core.translating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Translation (to integers) is the second step (after Lexing) before any modeling takes place.
 * The vocabulary is global (static) and is open by default; it can be initialized through
 * the {@link VocabularyRunner} class or simply left open to be filled by the modeling code
 * (as has been shown to be more appropriate for modeling source code).
 * <br />
 * <em>Note:</em> the counts in this class are for informative purposes only:
 * these are not (to be) used by any model nor updated with training.
 * 
 * @author Vincent Hellendoorn
 *
 */
public class Vocabulary {

	public static final String UNK = "<UNK>";
	public static final String BOS = "<s>";
	public static final String EOS = "</s>";
	
	static Map<String, Integer> wordIndices;
	static List<String> words;
	static List<Integer> counts;
	static int size;
	
	private static boolean closed;
	
	static { reset(); }
	
	private static void addUnk() {
		wordIndices.put(UNK, 0);
		words.add(UNK);
		counts.add(0);
		size++;
	}
	
	public static void reset() {
		wordIndices = new HashMap<>();
		words = new ArrayList<>();
		counts = new ArrayList<>();
		closed = false;
		size = 0;
		addUnk();
	}
	
	public static Map<String, Integer> wordIndices() {
		return wordIndices;
	}
	
	public static List<String> words() {
		return words;
	}
	
	public static List<Integer> counts() {
		return counts;
	}
	
	public static int size() {
		return size;
	}
	
	/**
	 * To be determined if this is a good solution to false vocabulary inflation
	 * @param offset
	 */
	public static void adjustSize(int offset) {
		size += offset;
	}
	
	public static void close() {
		closed = true;
	}

	public static void open() {
		closed = false;
	}
	
	private static int checkPoint;
	public static void setCheckpoint() {
		checkPoint = words.size();
	}
	
	public static void restoreCheckpoint() {
		for (int i = words.size(); i > checkPoint; i--) {
			counts.remove(counts.size() - 1);
			String word = words.remove(words.size() - 1);
			wordIndices.remove(word);
			size--;
		}
	}
	
	static void store(String token, int count) {
		Integer index = wordIndices.get(token);
		if (index == null) {
			index = wordIndices.size();
			wordIndices.put(token, index);
			words.add(token);
			counts.add(count);
			size++;
		}
		else {
			counts.set(index, count);
		}
	}
	
	public static Stream<Integer> toIndices(Stream<String> tokens) {
		return tokens.map(Vocabulary::toIndex);
	}

	public static List<Integer> toIndices(List<String> tokens) {
		return tokens.stream().map(Vocabulary::toIndex).collect(Collectors.toList());
	}

	public static Integer toIndex(String token) {
		Integer index = wordIndices.get(token);
		if (index == null) {
			if (closed) {
				return wordIndices.get(UNK);
			}
			else {
				index = wordIndices.size();
				wordIndices.put(token, index);
				words.add(token);
				size++;
				counts.add(1);
			}
		}
		return index;
	}
	
	public static Integer getCount(String token) {
		Integer index = wordIndices.get(token);
		if (index != null) {
			return getCount(index);
		}
		return 0;
	}

	private static Integer getCount(Integer index) {
		return counts.get(index);
	}

	public static Stream<String> toWords(Stream<Integer> indices) {
		return indices.map(Vocabulary::toWord);
	}

	public static List<String> toWords(List<Integer> indices) {
		return indices.stream().map(Vocabulary::toWord).collect(Collectors.toList());
	}
		
	public static String toWord(Integer index) {
		return words.get(index);
	}
}
