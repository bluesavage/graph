package textrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.didion.jwnl.data.POS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

/**
 * Java implementation of the TextRank algorithm by Rada Mihalcea, et al.
 * http://lit.csci.unt.edu/index.php/Graph-based_NLP
 * 
 * @author paco@sharethis.com
 */

public class TextRank implements Callable<Collection<MetricVector>> {
	// logging

	private final static Log LOG = LogFactory.getLog(TextRank.class.getName());

	/**
	 * Public definitions.
	 */

	public final static String NLP_RESOURCES = "nlp.resources";
	public final static double MIN_NORMALIZED_RANK = 0.1D;
	public final static int MAX_NGRAM_LENGTH = 5;
	public final static long MAX_WORDNET_TEXT = 2000L;
	public final static long MAX_WORDNET_GRAPH = 600L;

	/**
	 * Protected members.
	 */

	protected LanguageModel lang = null;

	protected String text = null;
	protected boolean use_wordnet = false;
	protected ArrayList<Sentence> s_list = null;

	protected Graph graph = null;
	protected Graph ngram_subgraph = null;
	protected Map<NGram, MetricVector> metric_space = null;

	protected long start_time = 0L;
	protected long elapsed_time = 0L;

	/**
	 * Constructor.
	 */

	public TextRank() throws Exception {
		lang = LanguageModel.buildLanguage();
	}

	/**
	 * Prepare to call algorithm with a new text to analyze.
	 */

	public Collection<Sentence> prepCall(final String _text) throws Exception {
		final String[] _para = new String[1];
		_para[0] = _text;
		return prepCall(_para);
	}

	/**
	 * Prepare to call algorithm with a new text to analyze.
	 */

	public Collection<Sentence> prepCall(final String[] _para) throws Exception {
		final StringBuilder sb = new StringBuilder();

		graph = new Graph();
		ngram_subgraph = null;
		metric_space = new HashMap<NGram, MetricVector>();

		this.s_list = new ArrayList<Sentence>();

		// ////////////////////////////////////////////////
		// PASS 1: construct a graph from PoS tags

		initTime();

		// scan sentences to construct a graph of relevent morphemes

		s_list = new ArrayList<Sentence>();

		for (String para_text : _para) {
			if (para_text.trim().length() > 0) {
				for (String sent_text : lang.splitParagraph(para_text)) {
					final Sentence s = new Sentence(sent_text.trim());
					s.tokenize(lang);
					s.mapTokens(lang, graph);
					s_list.add(s);

					if (LOG.isDebugEnabled()) {
						LOG.debug("s: " + s.text);
						LOG.debug(s.md5_hash);
					}
				}
			}
		}

		this.text = sb.toString();
		markTime("construct_graph");
		return s_list;
	}

	/**
	 * Run the TextRank algorithm on the given semi-structured text (e.g.,
	 * results of parsed HTML from crawled web content) to build a graph of
	 * weighted key phrases.
	 */

	public Collection<MetricVector> call() throws Exception {
		// ////////////////////////////////////////////////
		// PASS 2: run TextRank to determine keywords

		initTime();

		final int max_results = (int) Math.round((double) graph.size()
				* Graph.KEYWORD_REDUCTION_FACTOR);

		graph.runTextRank();
		graph.sortResults(max_results);

		ngram_subgraph = NGram.collectNGrams(lang, s_list,
				graph.getRankThreshold());

		markTime("basic_textrank");

		if (LOG.isInfoEnabled()) {
			LOG.info("TEXT_BYTES:\t" + text.length());
			LOG.info("GRAPH_SIZE:\t" + graph.size());
		}

		// ////////////////////////////////////////////////
		// PASS 3: lemmatize selected keywords and phrases

		initTime();

		// Graph synset_subgraph = new Graph();

		// filter for edge cases

		// if (use_wordnet &&
		// (text.length() < MAX_WORDNET_TEXT) &&
		// (graph.size() < MAX_WORDNET_GRAPH)
		// ) {
		// // test the lexical value of nouns and adjectives in WordNet
		//
		// for (Node n: graph.values()) {
		// final KeyWord kw = (KeyWord) n.value;
		//
		// if (lang.isNoun(kw.pos)) {
		// SynsetLink.addKeyWord(synset_subgraph, n, kw.text, POS.NOUN);
		// }
		// else if (lang.isAdjective(kw.pos)) {
		// SynsetLink.addKeyWord(synset_subgraph, n, kw.text, POS.ADJECTIVE);
		// }
		// }

		// test the collocations in WordNet
		//
		// for (Node n : ngram_subgraph.values()) {
		// final NGram gram = (NGram) n.value;
		//
		// if (gram.nodes.size() > 1) {
		// SynsetLink.addKeyWord(synset_subgraph, n, gram.getCollocation(),
		// POS.NOUN);
		// }
		// }
		//
		// synset_subgraph =
		// SynsetLink.pruneGraph(synset_subgraph, graph);
		// }

		// augment the graph with n-grams added as nodes

		for (Node n : ngram_subgraph.values()) {
			final NGram gram = (NGram) n.value;

			if (gram.length < MAX_NGRAM_LENGTH) {
				graph.put(n.key, n);

				for (Node keyword_node : gram.nodes) {
					n.connect(keyword_node);
				}
			}
		}

		markTime("augment_graph");

		// ////////////////////////////////////////////////
		// PASS 4: re-run TextRank on the augmented graph

		initTime();

		graph.runTextRank();
		// graph.sortResults(graph.size() / 2);

		// collect stats for metrics

		final int ngram_max_count = NGram.calcStats(ngram_subgraph);

		// if (use_wordnet) {
		// SynsetLink.calcStats(synset_subgraph);
		// }

		markTime("ngram_textrank");

		if (LOG.isInfoEnabled()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("RANK: " + ngram_subgraph.dist_stats);

				for (Node n : new TreeSet<Node>(ngram_subgraph.values())) {
					final NGram gram = (NGram) n.value;
					LOG.debug(gram.getCount() + " " + n.rank + " " + gram.text /*
																				 * +
																				 * " @ "
																				 * +
																				 * gram
																				 * .
																				 * renderContexts
																				 * (
																				 * )
																				 */);
				}
			}

			// if (LOG.isDebugEnabled()) {
			// LOG.debug("RANK: " + synset_subgraph.dist_stats);
			//
			// for (Node n : new TreeSet<Node>(synset_subgraph.values())) {
			// final SynsetLink s = (SynsetLink) n.value;
			// LOG.info("emit: " + s.synset + " " + n.rank + " " + s.relation);
			// }
			// }
		}

		// ////////////////////////////////////////////////
		// PASS 5: construct a metric space for overall ranking

		initTime();

		final double link_min = ngram_subgraph.dist_stats.getMin();
		final double link_coeff = ngram_subgraph.dist_stats.getMax()
				- ngram_subgraph.dist_stats.getMin();

		final double count_min = 1;
		final double count_coeff = (double) ngram_max_count - 1;

		// final double synset_min = synset_subgraph.dist_stats.getMin();
		// final double synset_coeff = synset_subgraph.dist_stats.getMax() -
		// synset_subgraph.dist_stats.getMin();

		for (Node n : ngram_subgraph.values()) {
			final NGram gram = (NGram) n.value;

			if (gram.length < MAX_NGRAM_LENGTH) {
				final double link_rank = (n.rank - link_min) / link_coeff;
				final double count_rank = (gram.getCount() - count_min)
						/ count_coeff;
				// final double synset_rank = use_wordnet ?
				// n.maxNeighbor(synset_min, synset_coeff) : 0.0D;

				// final MetricVector mv = new MetricVector(gram, link_rank,
				// count_rank, synset_rank);
				final MetricVector mv = new MetricVector(gram, link_rank,
						count_rank, 0);

				metric_space.put(gram, mv);
			}
		}

		markTime("normalize_ranks");

		// return results

		return metric_space.values();
	}

	// ////////////////////////////////////////////////////////////////////
	// access and utility methods
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Re-initialize the timer.
	 */

	public void initTime() {
		start_time = System.currentTimeMillis();
	}

	/**
	 * Report the elapsed time with a label.
	 */

	public void markTime(final String label) {
		elapsed_time = System.currentTimeMillis() - start_time;

		if (LOG.isInfoEnabled()) {
			LOG.info("ELAPSED_TIME:\t" + elapsed_time + "\t" + label);
		}
	}

	/**
	 * Accessor for the graph.
	 */

	public Graph getGraph() {
		return graph;
	}

	/**
	 * Serialize the graph to a file which can be rendered.
	 */

	public void serializeGraph(final String graph_file) throws Exception {
		for (Node n : graph.values()) {
			n.marked = false;
		}

		final TreeSet<String> entries = new TreeSet<String>();

		for (Node n : ngram_subgraph.values()) {
			final NGram gram = (NGram) n.value;
			final MetricVector mv = metric_space.get(gram);

			if (mv != null) {
				final StringBuilder sb = new StringBuilder();

				sb.append("rank").append('\t');
				sb.append(n.getId()).append('\t');
				sb.append(mv.render());
				entries.add(sb.toString());

				n.serializeGraph(entries);
			}
		}

		final OutputStreamWriter fw = new OutputStreamWriter(
				new FileOutputStream(graph_file), "UTF-8");

		try {
			for (String entry : entries) {
				fw.write(entry, 0, entry.length());
				fw.write('\n');
			}
		} finally {
			fw.close();
		}
	}

	/**
	 * Serialize resulting graph to a string.
	 */

	public String toString() {
		final TreeSet<MetricVector> key_phrase_list = new TreeSet<MetricVector>(
				metric_space.values());
		final StringBuilder sb = new StringBuilder();

		for (MetricVector mv : key_phrase_list) {
			if (mv.metric >= MIN_NORMALIZED_RANK) {
				sb.append(mv.render()).append("\t").append(mv.value.text)
						.append("\n");
			}
		}

		return sb.toString();
	}

	private List<String> getKeywords() {
		List<String> result = new ArrayList<String>();
		final TreeSet<MetricVector> key_phrase_list = new TreeSet<MetricVector>(
				metric_space.values());
		final StringBuilder sb = new StringBuilder();

		for (MetricVector mv : key_phrase_list) {
			if (mv.metric >= MIN_NORMALIZED_RANK) {
				result.add(mv.value.text);
			}
		}
		return result;
	}
	
	

	public static List<String> getKeywords(String content) {
		List<String> keywords = null;
		final String text = content;
		if(null == content) return new ArrayList<String>(); 
		try{
			final TextRank tr = new TextRank();
			final Collection<Sentence> s_list = tr.prepCall(text.split("\n"));
			final FutureTask<Collection<MetricVector>> task = new FutureTask<Collection<MetricVector>>(tr);
			Collection<MetricVector> answer = null;
	
			final Thread thread = new Thread(task);
			thread.run();
			answer = task.get(15000L, TimeUnit.MILLISECONDS); // timeout in N ms
			keywords = tr.getKeywords();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return keywords;
	}
	
	public static void main(final String[] args) throws Exception {
		String paperAbstract = "BACKGROUND Fructose acutely raises serum uric acid in normal subjects, but the effect in subjects with metabolic syndrome or subjects with chronic kidney disease is unknown. The aim of the study was to evaluate changes in serum uric acid during the fructose tolerance test in patients with chronic kidney disease, metabolic syndrome with comparison to healthy controls."
			+ "Studies were performed in 36 subjects with obesity (body mass index >30) and metabolic syndrome, 14 patients with stage 3 chronic kidney disease, and 25 healthy volunteers. The fructose tolerance test was performed in each patient. The change in serum uric acid during the fructose challenge was correlated with baseline ambulatory blood pressure, serum uric acid, metabolic, and inflammatory markers, and target organ injury including carotid intima media thickness and renal resistive index (determined by Doppler)."
			+ "Absolute serum uric acid values were highest in the chronic kidney disease group, followed by the metabolic syndrome and then healthy controls. Similar increases in serum uric acid in response to the fructose tolerance test was observed in all three groups, but the greatest percent rise was observed in healthy controls compared to the other two groups. No significant association was shown between the relative rise in uric acid and clinical or inflammatory parameters associated with kidney disease (albuminuria, eGFR) or metabolic syndrome."
			+ "Subjects with chronic kidney disease and metabolic syndrome have higher absolute uric acid values following a fructose tolerance test, but show a relatively smaller percent increase in serum uric acid. Changes in serum uric acid during the fructose tolerance test did not correlate with changes in metabolic parameters, inflammatory mediators or with target organ injury. These studies suggest that acute changes in serum uric acid in response to fructose do not predict the metabolic phenotype or presence of inflammatory mediators in subjects with obesity, metabolic syndrome or chronic kidney disease.";
		
		List<String> keywords = TextRank.getKeywords(paperAbstract);
		System.out.println(keywords);
	}
}
