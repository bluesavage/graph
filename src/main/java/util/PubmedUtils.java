package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PubmedUtils {
	private final static String BASEURL_EUTILS = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private final static String BASEURL_ESEARCH = BASEURL_EUTILS + "esearch.fcgi";
	private final static String BASEURL_EFETCH = BASEURL_EUTILS + "efetch.fcgi";

	public static String fetch(String pmid) {
		String resultXML = null;
		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pmid + "&retmode=xml";
		InputStream in = null;
		try {
			in = new URL(url).openStream();
			resultXML = IOUtils.toString(in);
		}catch(Exception ex){
			ex.printStackTrace();
		} finally {
			try{
			in.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return resultXML;
	}
	
	
	public static String getPaperInfoXML(String pmid) {
		String resultXML = null;
		try{
				HashMap<String, String> paramMap = new HashMap<String, String>();
				paramMap.put("db", "PubMed");
				paramMap.put("id", String.valueOf(pmid));
				paramMap.put("retmode", "xml");
				paramMap.put("rettype", "medline");
	
				resultXML = HTTPUtil.getSimplePOSTResponse(BASEURL_EFETCH, paramMap, "UTF-8");
				
		}catch(Exception ex){
			ex.printStackTrace();
		}		
		return resultXML;
	}
	
	public static String getAbstractStr(Document doc){
		if(null == doc) return null; 
		NodeList elements = doc.getElementsByTagName("Abstract");
		if(null == elements) return null;
		Element abs = (Element)elements.item(0);
		if(null == abs) return null;
		return abs.getTextContent().trim();
	}
	
	public static List<String> keywords(Document doc){
		List<String> keywords = new ArrayList<String>();
		NodeList elements = doc.getElementsByTagName("Keyword");
		int rowcount = elements.getLength();
		for(int i=0;i<rowcount;i++)
			keywords.add(elements.item(i).getTextContent());
		return keywords;
	}
	
	public static HashMap<String, List<String>> meshterms(Document doc){
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		NodeList headingsList = doc.getElementsByTagName("MeshHeading");
		int rowcount = headingsList.getLength();
		
		for(int i=0;i<rowcount;i++) {
			Element heading = (Element)headingsList.item(i);
			NodeList meshHeading = heading.getChildNodes();
			int childcount = meshHeading.getLength();
			String meshHeadingString = null;
			List<String> meshQualifierList = new ArrayList();
			
			for(int j=0;j<childcount;j++){
				if(meshHeading.item(j).getNodeName().equals("DescriptorName")) meshHeadingString = meshHeading.item(j).getTextContent();
				else if(meshHeading.item(j).getNodeName().equals("QualifierName")) meshQualifierList.add(meshHeading.item(j).getTextContent());
			}
			
			result.put(meshHeadingString, meshQualifierList);
		}
		return result;
	}
	
	public static void main(String args[]) {
		String xmlString = PubmedUtils.fetch("16416429");
		Document document = XMLUtil.getParsedDocument(xmlString, false);
//		System.out.println(getAbstractStr(document));
		keywords(document);
		System.out.println(meshterms(document));
	}
}
