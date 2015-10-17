package textrank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 
public class
    Context
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(Context.class.getName());


    /**
     * Public members.
     */

    public Sentence s = null;
    public int start = 0;


    /**
     * Constructor.
     */

    public
	Context (final Sentence s, final int start)
    {
	this.s = s;
	this.start = start;
    }
}
