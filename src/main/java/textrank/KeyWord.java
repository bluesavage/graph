package textrank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements a node value in a TextRank graph denoting a noun or
 * adjective.
 *
 * @author paco@sharethis.com
 */

public class
    KeyWord
    extends NodeValue
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(KeyWord.class.getName());


    /**
     * Public members.
     */

    public String pos = null;


    /**
     * Constructor.
     */

    public
	KeyWord (final String text, final String pos)
    {
    	this.text = text;
    	this.pos = pos;
    }


    /**
     * Create a description text for this value.
     */

    public String
	getDescription ()
    {
	return "KEYWORD" + '\t' + pos + ' ' + text;
    }
}
