package textrank;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class Graph extends TreeMap<String, Node> {
	// logging

	private final static Log LOG = LogFactory.getLog(Graph.class.getName());

	/**
	 * Public definitions.
	 */

	public final static double INCLUSIVE_COEFF = 0.25D;
	public final static double KEYWORD_REDUCTION_FACTOR = 0.8D;
	public final static double TEXTRANK_DAMPING_FACTOR = 0.85D;
	public final static double STANDARD_ERROR_THRESHOLD = 0.005D;

	/**
	 * Public members.
	 */

	public SummaryStatistics dist_stats = new SummaryStatistics();

	/**
	 * Protected members.
	 */

	protected Node[] node_list = null;

	/**
	 * Run through N iterations of the TreeRank algorithm, or until the standard
	 * error converges below a given threshold.
	 */

	public void runTextRank() {
		final int max_iterations = this.size();
		node_list = new Node[this.size()];

		// load the node list

		int j = 0;

		for (Node n1 : this.values()) {
			node_list[j++] = n1;
		}

		// iterate, then sort and mark the top results

		iterateGraph(max_iterations);
	}

	/**
	 * Iterate through the graph, calculating rank.
	 */

	protected void iterateGraph(final int max_iterations) {
		final double[] rank_list = new double[node_list.length];

		// either run through N iterations, or until the standard
		// error converges below a threshold

		for (int k = 0; k < max_iterations; k++) {
			dist_stats.clear();

			// calculate the next rank for each node

			for (int i = 0; i < node_list.length; i++) {
				final Node n1 = node_list[i];
				double rank = 0.0D;

				for (Node n2 : n1.edges) {
					rank += n2.rank / (double) n2.edges.size();
				}

				rank *= TEXTRANK_DAMPING_FACTOR;
				rank += 1.0D - TEXTRANK_DAMPING_FACTOR;

				rank_list[i] = rank;
				dist_stats.addValue(Math.abs(n1.rank - rank));
			}

			final double standard_error = dist_stats.getStandardDeviation()
					/ Math.sqrt((double) dist_stats.getN());

			if (LOG.isInfoEnabled()) {
				LOG.info("iteration: " + k + " error: " + standard_error);
			}

			// swap in new rank values

			for (int i = 0; i < node_list.length; i++) {
				node_list[i].rank = rank_list[i];
			}

			if (standard_error < STANDARD_ERROR_THRESHOLD) {
				break;
			}
		}
	}

	/**
	 * Sort results to identify potential keywords.
	 */

	public void sortResults(final long max_results) {
		Arrays.sort(node_list, new Comparator<Node>() {
			public int compare(Node n1, Node n2) {
				if (n1.rank > n2.rank) {
					return -1;
				} else if (n1.rank < n2.rank) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		// mark the top-ranked nodes

		dist_stats.clear();

		for (int i = 0; i < node_list.length; i++) {
			final Node n1 = node_list[i];

			if (i <= max_results) {
				n1.marked = true;
				dist_stats.addValue(n1.rank);
			}

			LOG.info("n: " + n1.value.text + " " + n1.rank + " " + n1.marked);

			if (LOG.isDebugEnabled()) {
				LOG.debug("n: " + n1.key + " " + n1.rank + " " + n1.marked);

				for (Node n2 : n1.edges) {
					LOG.debug(" - " + n2.key);
				}
			}
		}
	}

	/**
	 * Calculate a threshold for the ranked results.
	 */

	public double getRankThreshold() {
		return dist_stats.getMean()
				+ (dist_stats.getStandardDeviation() * INCLUSIVE_COEFF);
	}
}
