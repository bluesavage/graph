package textrank;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.math.util.MathUtils;


/**
 * Implements a node in the TextRank graph, denoting some noun or
 * adjective morpheme.
 *
 * @author paco@sharethis.com
 */

public class
    Node
    implements Comparable<Node>
{
    // logging

    private final static Log log_ =
        LogFactory.getLog(Node.class.getName());


    /**
     * Public members.
     */

    public HashSet<Node> edges = new HashSet<Node>();
    public double rank = 0.0D;
    public String key = null;
    public boolean marked = false;
    public NodeValue value = null;


    /**
     * Private constructor.
     */

    private
	Node (final String key, final NodeValue value)
    {
	this.rank = 1.0D;
	this.key = key;
	this.value = value;
    }


    /**
     * Compare method for sort ordering.
     */

    public int
	compareTo (final Node that)
    {
        if (this.rank > that.rank) {
	    return -1;
	}
	else if (this.rank < that.rank) {
	    return 1;
	}
	else {
	    return this.value.text.compareTo(that.value.text);
	}
    }


    /**
     * Connect two nodes with a bi-directional arc in the graph.
     */

    public void
	connect (final Node that)
    {
	this.edges.add(that);
	that.edges.add(this);
    }


    /**
     * Disconnect two nodes removing their bi-directional arc in the
     * graph.
     */

    public void
	disconnect (final Node that)
    {
	this.edges.remove(that);
	that.edges.remove(this);
    }


    /**
     * Create a unique identifier for this node, returned as a hex
     * string.
     */

    public String
	getId ()
    {
	return Integer.toString(hashCode(), 16);
    }


    /**
     * Factory method.
     */

    public static Node
	buildNode (final Graph graph, final String key, final NodeValue value)
	throws Exception
    {
	Node n = graph.get(key);

	if (n == null) {
	    n = new Node(key, value);
	    graph.put(key, n);
	}

	if (log_.isDebugEnabled()) {
	    log_.debug(n.key);
	}

	return n;
    }


    /**
     * Search nearest neighbors in WordNet subgraph to find the
     * maximum rank of any adjacent SYNONYM synset.
     */

    public double
	maxNeighbor (final double min, final double coeff)
    {
	double adjusted_rank = 0.0D;

	if (log_.isDebugEnabled()) {
	    log_.debug("neighbor: " + value.text + " " + value);
	    log_.debug("  edges:");

	    for (Node n : edges) {
		log_.debug(n.value);
	    }
	}

	if (edges.size() > 1) {
	    // consider the immediately adjacent synsets

	    double max_rank = 0.0D;

	    for (Node n : edges) {
		if (n.value instanceof SynsetLink) {
		    max_rank = Math.max(max_rank, n.rank);
		}
	    }

	    if (max_rank > 0.0D) {
		// adjust it for scale [0.0, 1.0]
		adjusted_rank = (max_rank - min) / coeff;
	    }
	}
	else {
	    // consider the synsets of the one component keyword

	    for (Node n : edges) {
		if (n.value instanceof KeyWord) {
		    // there will only be one
		    adjusted_rank = n.maxNeighbor(min, coeff);
		}
	    }
	}

	if (log_.isDebugEnabled()) {
	    log_.debug(adjusted_rank);
	}

	return adjusted_rank;
    }


    /**
     * Traverse the graph, serializing out the nodes and edges.
     */

    public void
	serializeGraph (final Set<String> entries)
    {
	StringBuilder sb = new StringBuilder();

	// emit text and ranks vector

	marked = true;

	sb.append("node").append('\t');
	sb.append(getId()).append('\t');
	sb.append(value.getDescription()).append('\t');
	sb.append(MathUtils.round(rank, 3));
	entries.add(sb.toString());

	// emit edges

	for (Node n : edges) {
	    sb = new StringBuilder();
	    sb.append("edge").append('\t');
	    sb.append(getId()).append('\t');
	    sb.append(n.getId());
	    entries.add(sb.toString());

	    if (!n.marked) {
		// tail recursion on child
		n.serializeGraph(entries);
	    }
	}
    }
    
    public String toSring(){
//    	return new String(key + "(" + rank +")");
    	return new String(this.value.text);
    }
}
