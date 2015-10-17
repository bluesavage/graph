package textrank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements a node value in a TextRank graph.
 *
 * @author paco@sharethis.com
 */

public class
    NodeValue
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(NodeValue.class.getName());


    /**
     * Public members.
     */

    public String text = null;


    /**
     * Create a description text for this value.
     */

    public String
	getDescription ()
    {
	return "GENERIC" + '\t' + getCollocation();
    }


    /**
     * Create a collocation out of the text for lookup in WordNet.
     */

    public String
	getCollocation ()
    {
	return text.replace(' ', '_');
    }
}
