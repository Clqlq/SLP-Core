package slp.core.modeling.ngram;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import slp.core.modeling.AbstractModel;
import slp.core.modeling.ModelRunner;
import slp.core.sequencing.NGramSequencer;
import slp.core.util.Pair;

public class NGramCache extends AbstractModel {

	public static final int DEFAULT_CAPACITY = 5000;
	private final int capacity;
	
	private NGramModel model;
	private final Deque<List<Integer>> cache;
	
	public NGramCache() {
		this(DEFAULT_CAPACITY);
	}

	public NGramCache(int capacity) {
		this.model = NGramModel.standard();
		// A cache is dynamic by default and only acts statically in prediction tasks
		setDynamic(true);
		
		this.capacity = capacity;
		this.cache = new ArrayDeque<List<Integer>>(this.capacity);
	}
	
	@Override
	public void notify(File next) {
		this.model = NGramModel.standard();
		this.cache.clear();
	}

	@Override
	public void learnToken(List<Integer> input, int index) {
		if (this.capacity == 0) return;
		List<Integer> sequence = NGramSequencer.sequenceAt(input, index);
		if (sequence.size() == ModelRunner.getNGramOrder() || index == input.size() - 1) {
			this.cache.addLast(sequence);
			this.model.counter.addAggressive(sequence);
		}
		if (this.cache.size() > this.capacity) {
			List<Integer> removed = this.cache.removeFirst();
			this.model.counter.removeAggressive(removed);
		}
	}

	@Override
	public void forgetToken(List<Integer> input, int index) { }

	@Override
	public Pair<Double, Double> modelAtIndex(List<Integer> input, int index) {
		return this.model.modelToken(input, index);
	}

	@Override
	public Map<Integer, Pair<Double, Double>> predictAtIndex(List<Integer> input, int index) {
		return this.model.predictToken(input, index);
	}
}