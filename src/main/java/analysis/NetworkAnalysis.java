package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import Jama.Matrix;

public class NetworkAnalysis {
	public static double THRESHHOLD = 4.0;

	public static void main(String args[]) {
		 Map<String,double[]> matrix = loadMatrix("data/matrix.txt", true);
		 String content = makePajekFile(matrix, THRESHHOLD);
		 netFileWriter("data/matrix.net", content);
	}
	
	public static void netFileWriter(String filename, String content) {
		try{
			FileUtils.writeStringToFile(new File(filename), content);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static Map<String,double[]> loadMatrix(String filename, boolean ignoreFirstLine) {
		Map<String,double[]> namedMatrix = new HashMap<String, double[]>();
		
		try{
			List<String> rows = FileUtils.readLines(new File(filename));
			for (int i=0;i<rows.size();i++) {
				if (ignoreFirstLine && i ==0) continue;
				String row = rows.get(i);
				String cells[] = row.split("\t");
				double value[] = new double[6];
				String name = null;
				for (int j=0;j<cells.length; j++) {
					if (j==0) name = cells[j];
					else {
						value[j-1] = Double.parseDouble(cells[j]);
					}
				}
				namedMatrix.put(name, value);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return namedMatrix;
	}
	
	private static SortedSet<String> getConnectedNodes(Map<String, double[]> matrix, double threshHold) {
		SortedSet<String> result = new TreeSet<String>();
		SortedSet<String> keys = getSortedKey(matrix);
		HashSet<String> edges = new HashSet<String>();

		for(String outerKey : keys) {
			for(String innerKey: keys) {
				String key1 = outerKey + "|"+ innerKey;
				String key2 = innerKey + "|" + outerKey;
				
				if(edges.contains(key1) || edges.contains(key2)) continue;
				if(outerKey.equals(innerKey)) continue;
				edges.add(key1);
				edges.add(key2);
				double similarity = WeightedJaccardSimilarity.calc(matrix.get(innerKey), matrix.get(outerKey));
				if (similarity > threshHold) {
					result.add(innerKey);
					result.add(outerKey);
				}
			}
		}
		
		return result;
	}
	public static String makePajekFile(Map<String, double[]> matrix, double threshHold) {
		if (matrix == null) return null; 

		int count=0;
		String pajekStr = null;
		StringBuilder sb = new StringBuilder();
		Map<String, Integer> itemNumber = new HashMap<String, Integer>();
		SortedSet<String> keys = getConnectedNodes(matrix, threshHold);
		for (String key: keys) itemNumber.put(key, ++count);
		
		sb.append("*vertices ").append(keys.size()).append("\n");
		
		// make vertices
		int number = 1;
		for (String key : keys) {
			sb.append(number++).append(" ").append("\"" + key + "\"").append("\n");
		}
		
		// make edges
		sb.append("*edges ").append("\n");

		for(String outerKey : keys) {
			for(String innerKey: keys) {
				if (outerKey.equals(innerKey)) continue;
				double similarity = WeightedJaccardSimilarity.calc(matrix.get(innerKey), matrix.get(outerKey));
				if (similarity > threshHold)
				sb.append(itemNumber.get(outerKey)).append(" ").append(itemNumber.get(innerKey)).append(" ").append(similarity).append("\n");
				
			}
		}
		pajekStr = sb.toString();
		System.out.println(pajekStr);
		return pajekStr;
	}
	
	public static SortedSet<String> getSortedKey(Map<String, double[]> matrix) {
		SortedSet<String> sortedSet=new TreeSet<String>();
		for (String item : matrix.keySet()) sortedSet.add(item);
		return sortedSet;
	}
}
