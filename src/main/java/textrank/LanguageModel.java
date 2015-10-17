package textrank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class LanguageModel {
	// logging

	private final static Log LOG = LogFactory.getLog(LanguageModel.class
			.getName());

	/**
	 * Public definitions.
	 */

	public final static int TOKEN_LENGTH_LIMIT = 50;

	/**
	 * Factory method, loads libraries for OpenNLP based on the given language
	 * code.
	 */

	public static LanguageModel buildLanguage() throws Exception {
		LanguageModel lang = null;
		lang = new LanguageEnglish();
		return lang;
	}

	/**
	 * Load libraries for OpenNLP for this specific language.
	 */

	public abstract void loadResources() throws Exception;

	/**
	 * Split sentences within the paragraph text.
	 */

	public abstract String[] splitParagraph(final String text);

	/**
	 * Tokenize the sentence text into an array of tokens.
	 */

	public abstract String[] tokenizeSentence(final String text);

	/**
	 * Run a part-of-speech tagger on the sentence token list.
	 */

	public abstract String[] tagTokens(final String[] token_list);

	/**
	 * Prepare a stable key for a graph node (stemmed, lemmatized) from a token.
	 */

	public abstract String getNodeKey(final String text, final String pos)
			throws Exception;

	/**
	 * Determine whether the given PoS tag is relevant to add to the graph.
	 */

	public boolean isRelevant(final String pos) {
		return isNoun(pos) || isAdjective(pos);
	}

	/**
	 * Determine whether the given PoS tag is a noun.
	 */

	public abstract boolean isNoun(final String pos);

	/**
	 * Determine whether the given PoS tag is an adjective.
	 */

	public abstract boolean isAdjective(final String pos);

	/**
	 * Perform stemming on the given token.
	 */

	public abstract String stemToken(final String token);

	/**
	 * Clean the text for a token.
	 *
	 * @param token_text
	 *            input text for a token.
	 * @return clean text
	 * @throws Exception
	 *             in case of any problem.
	 */

	public String scrubToken(final String token_text) throws Exception {
		String scrubbed = token_text;

		if (scrubbed.length() > TOKEN_LENGTH_LIMIT) {
			scrubbed = scrubbed.substring(0, TOKEN_LENGTH_LIMIT);
		}

		return scrubbed;
	}
}
