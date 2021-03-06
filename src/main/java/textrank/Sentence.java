package textrank;

import java.io.File;

import opennlp.tools.util.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author paco@sharethis.com
 */

public class Sentence {
	// logging

	private final static Log LOG = LogFactory.getLog(Sentence.class.getName());

	/**
	 * Public members.
	 */

	public String text = null;
	public String[] token_list = null;
	public Node[] node_list = null;
	public String md5_hash = null;

	/**
	 * Constructor.
	 */

	public Sentence(final String text) {
		this.text = text;
	}

	/**
	 * Return a byte array formatted as hexadecimal text.
	 */

	public static String hexFormat(final byte[] b) {
		final StringBuilder sb = new StringBuilder(b.length * 2);

		for (int i = 0; i < b.length; i++) {
			String h = Integer.toHexString(b[i]);

			if (h.length() == 1) {
				sb.append("0");
			} else if (h.length() == 8) {
				h = h.substring(6);
			}

			sb.append(h);
		}

		return sb.toString().toUpperCase();
	}

	/**
	 * Tokenize the sentence.
	 */

	public String[] tokenize(final LanguageModel lang) {
		token_list = lang.tokenizeSentence(text);
		return token_list;
	}

	/**
	 * Accessor for token list.
	 */

	public String[] getTokenList() {
		return token_list;
	}

	/**
	 * Main processing per sentence.
	 */

	public void mapTokens(final LanguageModel lang, final Graph graph)
			throws Exception {
		// scan each token to determine part-of-speech

		final String[] tag_list = lang.tagTokens(token_list);

		// create nodes for the graph

		Node last_node = null;
		node_list = new Node[token_list.length];

		for (int i = 0; i < token_list.length; i++) {
			final String pos = tag_list[i];

			if (LOG.isDebugEnabled()) {
				LOG.debug("token: " + token_list[i] + " pos tag: " + pos);
			}

			if (lang.isRelevant(pos)) {
				final String key = lang.getNodeKey(token_list[i], pos);
				final KeyWord value = new KeyWord(token_list[i], pos);
				final Node n = Node.buildNode(graph, key, value);

				// emit nodes to construct the graph

				if (last_node != null) {
					n.connect(last_node);
				}

				last_node = n;
				node_list[i] = n;
			}
		}
	}
}
