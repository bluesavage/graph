package util;

import java.io.ByteArrayInputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MedlineCitationTools {
	/**
	 * metaInfoXML에서 Affiliation 문자열을 가져온다.
	 * @param pmid
	 * @param metaInfoXML
	 * @return
	 */
	public static String getAffiliation(String pmid, String metaInfoXML) {
		String resultString = getAffiliation(metaInfoXML);
		if (null == resultString) {
			System.out.println("[ERROR : getAffiliation] " + pmid);
		}
		return resultString;		
	}
	
	public static String getAffiliation(String metaInfoXML) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		StringBuilder result = new StringBuilder();
	
		if(null == metaInfoXML) return null;
		InputSource inputSource = new InputSource(new ByteArrayInputStream(metaInfoXML.getBytes()));
		
		try{
			NodeList affiliations = (NodeList)xPath.evaluate("/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/AuthorList/Author/AffiliationInfo/Affiliation", inputSource, XPathConstants.NODESET);
			for(int i=0;i<affiliations.getLength();i++) {
				Element element = (Element)affiliations.item(i);
				if(null != element) {
					result.append(getShortAffiliation(element.getTextContent())).append(" ");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		return result.toString().trim();
	}
	
	public static String getPubDate(String metaInfoXML) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		StringBuilder result = new StringBuilder();
	
		if(null == metaInfoXML) return null;
		InputSource inputSource = new InputSource(new ByteArrayInputStream(metaInfoXML.getBytes()));
		
		try{
			NodeList pubYear = (NodeList)xPath.evaluate("/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/Journal/JournalIssue/PubDate/Year", inputSource, XPathConstants.NODESET);
			for(int i=0;i<pubYear.getLength();i++) {
				Element element = (Element)pubYear.item(i);
				if(null != element) {
					result.append(element.getTextContent()).append(" ");
				}
			}
		}catch(Exception ex){
//			ex.printStackTrace();
			return "";
		}
		return result.toString().trim();
	}
	
	private static String getShortAffiliation(String fullString) {
		if (null == fullString) return null;
		String tokens[] = fullString.split("\\,");
		return tokens[0];
	}
	
	/**
	 * metaInfoXML에서 Journal Name을 가져온다.
	 * @param pmid
	 * @param metaInfoXML
	 * @return
	 */
	public static String getJournalName(String pmid, String metaInfoXML) {
		String resultString = getJournalName(metaInfoXML);
		if (null == resultString) {
			System.out.println("[ERROR : getJournalName] " + pmid);
		}
		return resultString;
	}
	
	public static String getJournalName(String metaInfoXML) {
		String result = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		
		if(null == metaInfoXML) return result;
		InputSource inputSource = new InputSource(new ByteArrayInputStream(metaInfoXML.getBytes()));
		
		try{
			Element title = (Element)(xPath.evaluate("/PubmedArticleSet/PubmedArticle/MedlineCitation//Title", inputSource, XPathConstants.NODE));
			if(null == title) return null;
			result = title.getTextContent();
		}catch(Exception ex){
			return null;
		}
		return result.toLowerCase().trim();
	}
	
	/**
	 * metaInfoXML에서 Article Title을 가져온다.
	 * @param pmid
	 * @param metaInfoXML
	 * @return
	 */
	public static String getArticleTitle(String pmid, String metaInfoXML) {
		String resultString = getArticleTitle(metaInfoXML);
		if (null == resultString) {
			System.out.println("[ERROR : getArticleTitle] " + pmid);
		}
		return resultString;
	}
	
	public static String getArticleTitle(String metaInfoXML) {
		String result = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath = factory.newXPath();
		
		if(null == metaInfoXML) return result;
		InputSource inputSource = new InputSource(new ByteArrayInputStream(metaInfoXML.getBytes()));
		
		try{
			Element title = (Element)(xPath.evaluate("/PubmedArticleSet/PubmedArticle/MedlineCitation/Article/ArticleTitle", inputSource, XPathConstants.NODE));
			if(null == title) return null;
			result = title.getTextContent();
		}catch(Exception ex){
			return null;
		}
		return result.toLowerCase().trim();
	}
}
