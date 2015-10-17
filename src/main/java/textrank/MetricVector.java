package textrank;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.math.util.MathUtils;


/**
 * Implements a point in the vector space representing the distance
 * metric.
 *
 * @author paco@sharethis.com
 */

public class
    MetricVector
    implements Comparable<MetricVector>
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(MetricVector.class.getName());


    /**
     * Public members.
     */

    public double metric = 0.0D;
    public NodeValue value = null;
    public double link_rank = 0.0D;
    public double count_rank = 0.0D;
    public double synset_rank = 0.0D;


    /**
     * Constructor.
     */

    public
	MetricVector (final NodeValue value, final double link_rank, final double count_rank, final double synset_rank)
    {
	this.value = value;

	this.metric = Math.sqrt(((1.0D * link_rank * link_rank) +
				 (0.5D * count_rank * count_rank) +
				 (1.5D * synset_rank * synset_rank)
				 ) / 3.0D
				);

	this.link_rank = MathUtils.round(link_rank, 2);
	this.count_rank = MathUtils.round(count_rank, 2);
	this.synset_rank = MathUtils.round(synset_rank, 2);

	if (LOG.isDebugEnabled()) {
	    LOG.debug("mv: " + metric + " " + link_rank + " " + count_rank + " " + synset_rank + " " + value.text);
	}
    }


    /**
     * Compare method for sort ordering.
     */

    public int
	compareTo (final MetricVector that)
    {
        if (this.metric > that.metric) {
	    return -1;
	}
	else if (this.metric < that.metric) {
	    return 1;
	}
	else {
	    return this.value.text.compareTo(that.value.text);
	}
    }


    /**
     * Serialize as text.
     */

    public String
	render ()
    {
	final StringBuilder sb = new StringBuilder();

	sb.append(MathUtils.round(metric, 1));
	sb.append(' ');
	sb.append(link_rank);
	sb.append(' ');
	sb.append(count_rank);
	sb.append(' ');
	sb.append(synset_rank);

	return sb.toString();
    }
}
