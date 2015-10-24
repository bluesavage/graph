package analysis;

public class WeightedJaccardSimilarity {
    public static double calc(double[] docVector1, double[] docVector2) {
    	int loopLength = docVector1.length;
    	double similarity = 0;
    	for (int loopCount=0;loopCount<loopLength;loopCount++){
    		double denominator = Math.max(docVector1[loopCount], docVector2[loopCount]);
    		if (denominator > 0)
    			similarity += Math.min(docVector1[loopCount], docVector2[loopCount]) / denominator;
    	}
    	return similarity;
    }
}
