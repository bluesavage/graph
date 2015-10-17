package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XMLUtil
{
	private static Logger logger = Logger.getLogger(XMLUtil.class.getName());

	public static Document getParsedDocument(String xmlStr, boolean valid)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = null;

		try
		{
			if(!valid)
			{
				// validation 설정
				dbf.setValidating(false);
				dbf.setFeature("http://xml.org/sax/features/namespaces", false); 
				dbf.setFeature("http://xml.org/sax/features/validation", false); 
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); 
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
			}
			
			DocumentBuilder builder = dbf.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xmlStr));
	        return builder.parse(is);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.toString());
		}

		return doc;
	}
	
	public static Document getParsedDocument(File xmlFile, boolean valid)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = null;

		try
		{
			if(!valid)
			{
				// validation 설정
				dbf.setValidating(false); 
				dbf.setFeature("http://xml.org/sax/features/namespaces", false); 
				dbf.setFeature("http://xml.org/sax/features/validation", false); 
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); 
				dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
			}
			
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setByteStream(new FileInputStream(xmlFile));
			doc = builder.parse(is);
		}
		catch (Exception e)
		{
//			e.printStackTrace();
			logger.info(xmlFile.getAbsoluteFile());
//			logger.error(e.toString());
		}

		return doc;
	}
	
	public static void saveXML(Document doc, String encoding, String filePath)
	{
		try
		{
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

			DOMSource source = new DOMSource(doc);
			FileOutputStream fos = new FileOutputStream(filePath);
			StreamResult target = new StreamResult(fos);
			transformer.transform(source, target);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.toString());
		}
	}
}
