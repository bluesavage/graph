package textrank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
//import opennlp.tools.lang.english.ParserTagger;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LanguageEnglish extends LanguageModel {
	// logging

	private final static Log LOG = LogFactory.getLog(LanguageEnglish.class.getName());

	/**
	 * Public definitions.
	 */

	public static SentenceDetectorME splitter = null;
	public static SentenceModel sentenceModel = null;
	public static Tokenizer tokenizer = null;
	public static POSTaggerME tagger = null;
	// public static englishStemmer stemmer_en = null;

	/**
	 * Constructor. Not quite a Singleton pattern but close enough given the
	 * resources required to be loaded ONCE.
	 */

	public LanguageEnglish() throws Exception {
		if (splitter == null) {
			loadResources();
		}
	}

	/**
	 * Load libraries for OpenNLP for this specific language.
	 */

	public void loadResources() throws Exception {
		InputStream sdModel = new FileInputStream("models/en-sent.bin");
		InputStream tnModel = new FileInputStream("models/en-token.bin");
		InputStream tgModel = new FileInputStream("models/en-pos-maxent.bin");
		
		try{
			sentenceModel = new SentenceModel(sdModel);
			splitter = new SentenceDetectorME(sentenceModel);
			
			TokenizerModel tokenizerModel = new TokenizerModel(tnModel);
			tokenizer = new TokenizerME(tokenizerModel);
			
			POSModel posModel = new POSModel(tgModel);
			tagger = new POSTaggerME(posModel);
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (sdModel != null) {
		    try {
		      sdModel.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}

	}

	/**
	 * Split sentences within the paragraph text.
	 */

	public String[] splitParagraph(final String text) {
		return splitter.sentDetect(text);
	}

	/**
	 * Tokenize the sentence text into an array of tokens.
	 */

	public String[] tokenizeSentence(final String text) {
		final String[] token_list = tokenizer.tokenize(text);

		for (int i = 0; i < token_list.length; i++) {
			token_list[i] = token_list[i].replace("\"", "").toLowerCase()
					.trim();
		}

		return token_list;
	}

	/**
	 * Run a part-of-speech tagger on the sentence token list.
	 */

	public String[] tagTokens(final String[] token_list) {
		final Sequence[] sequences = tagger.topKSequences(token_list);
		final String[] tag_list = new String[token_list.length];

		int i = 0;

		for (Object obj : sequences[0].getOutcomes()) {
			tag_list[i] = (String) obj;
			i++;
		}

		return tag_list;
	}

	/**
	 * Prepare a stable key for a graph node (stemmed, lemmatized) from a token.
	 */

	public String getNodeKey(final String text, final String pos)
			throws Exception {
		return pos.substring(0, 2) + stemToken(scrubToken(text)).toLowerCase();
	}

	/**
	 * Determine whether the given PoS tag is a noun.
	 */

	public boolean isNoun(final String pos) {
		return pos.startsWith("NN");
	}

	/**
	 * Determine whether the given PoS tag is an adjective.
	 */

	public boolean isAdjective(final String pos) {
		return pos.startsWith("JJ");
	}

	/**
	 * Perform stemming on the given token.
	 */

	public String stemToken(final String token) {
		return PorterStemmer.stem(token);
	}
	
	
}
