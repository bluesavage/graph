package db;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import textrank.TextRank;
import util.MedlineCitationTools;
import util.PubmedUtils;
import util.XMLUtil;

public class ConstructDB {
	private static InputStream sentDectmodelIn = null;
	private static InputStream chunkParserModelIn = null;
	
	private static String JOURNALS[] = { "experiment2" };
	private static String KEYWORDS[] = { "experiment2" };

	public static void constructArticleDB(String journalName, List<String> pmids) {
		int total = pmids.size();
		int count = 0;

		for (String pmid : pmids) {
			System.out.println(pmid + "\tprocessing(" + ++count + "/" + total + ") : " + pmid);

			String xmlString = PubmedUtils.fetch(pmid.trim());
			// System.out.println(xmlString);
			Document xmlDocument = XMLUtil.getParsedDocument(xmlString, false);
			if (null == xmlDocument)
				continue;

			DBInterface.insertArticleBean(new ArticleBean(pmid, xmlString, journalName));
		}
	}

	@SuppressWarnings("finally")
	public static List<ArticleBean> getArticleBeans(String journalName) {
		Connection conn = null;
		Statement stmt = null;
		List<ArticleBean> result = new ArrayList<ArticleBean>();

		try {
			conn = ConnectionManager.getConnection();
			String sql = "SELECT PMID, XMLDOC, JOURNAL FROM jya.ARTICLES WHERE JOURNAL='" + journalName
					+ "' LIMIT 1000000";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String pmid = rs.getString("PMID");
				String xmlDocString = rs.getString("XMLDOC");
				String journal = rs.getString("JOURNAL");

				result.add(new ArticleBean(pmid, xmlDocString, journal));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.closeStatement(stmt);
			ConnectionManager.closeConnection(conn);
			return result;
		}
	}
	
	@SuppressWarnings("finally")
	public static List<ArticleBean> getArticleBeans(int offset, int hoppingSize) {
		Connection conn = null;
		Statement stmt = null;
		List<ArticleBean> result = new ArrayList<ArticleBean>();

		try {
			conn = ConnectionManager.getConnection();
			String sql = "SELECT PMID, XMLDOC, JOURNAL FROM jya.ARTICLES ORDER BY ID LIMIT " + offset + ", " + hoppingSize + ";";
			System.out.println(sql);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String pmid = rs.getString("PMID");
				String xmlDocString = rs.getString("XMLDOC");
				String journal = rs.getString("JOURNAL");

				result.add(new ArticleBean(pmid, xmlDocString, journal));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.closeStatement(stmt);
			ConnectionManager.closeConnection(conn);
			return result;
		}
	}

	public static void makeJournalMESHOccurrenceDB() {
		HashMap<String, HashMap<String, Integer>> frequencies = new HashMap<String, HashMap<String, Integer>>();

		for (String journal : JOURNALS) {
			List<ArticleBean> articles = getArticleBeans(journal);
			int total = articles.size();
			int count = 1;
			for (ArticleBean bean : articles) {
				System.out.println(journal + "(" + count++ + "/" + total + ") : ");
				Document xmlDocument = XMLUtil.getParsedDocument(bean.getXmlKeyword(), false);
				String pubYear = MedlineCitationTools.getPubDate(bean.getXmlKeyword());
				HashMap<String, List<String>> meshTerms = PubmedUtils.meshterms(xmlDocument);

				Iterator<String> meshIterator = meshTerms.keySet().iterator();
				while (meshIterator.hasNext()) {
					String meshTerm = meshIterator.next();
					if (frequencies.containsKey(meshTerm)) {
						HashMap<String, Integer> frequency = frequencies.get(meshTerm);
						if (frequency.containsKey(pubYear)) {
							frequency.put(pubYear, (frequency.get(pubYear) + 1));
						} else {
							frequency.put(pubYear, 1);
						}
					} else {
						HashMap<String, Integer> frequency = new HashMap<String, Integer>();
						frequency.put(pubYear, 1);
						frequencies.put(meshTerm, frequency);
					}
				}
			}

			Connection conn = null;
			try {
				conn = ConnectionManager.getConnection();
				Iterator<String> meshIterator = frequencies.keySet().iterator();
				while (meshIterator.hasNext()) {
					String meshTerm = meshIterator.next();
					HashMap<String, Integer> freqs = frequencies.get(meshTerm);
					Iterator<String> pubYearIterator = freqs.keySet().iterator();

					while (pubYearIterator.hasNext()) {
						String pubYear = pubYearIterator.next();
						int freqVale = freqs.get(pubYear);
						DBInterface.insertMESHFreqInfo(conn, meshTerm, journal, pubYear, freqVale);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	public static void makeMESHOccurrenceDB() {
		HashMap<String, HashMap<String, Integer>> frequencies = new HashMap<String, HashMap<String, Integer>>();
		int offset = 0;
		int hoppingSize = 100;

		List<ArticleBean> articles = getArticleBeans(offset, hoppingSize);
		int count = 1;
		boolean continueFlag = true;

		while(continueFlag) {
			for (ArticleBean bean : articles) {
				System.out.println("(" + count++ + "/" + hoppingSize + ") : ");
				Document xmlDocument = XMLUtil.getParsedDocument(bean.getXmlKeyword(), false);
				String pubYear = MedlineCitationTools.getPubDate(bean.getXmlKeyword());
				HashMap<String, List<String>> meshTerms = PubmedUtils.meshterms(xmlDocument);

				Iterator<String> meshIterator = meshTerms.keySet().iterator();
				while (meshIterator.hasNext()) {
					String meshTerm = meshIterator.next();
					if (frequencies.containsKey(meshTerm)) {
						HashMap<String, Integer> frequency = frequencies.get(meshTerm);
						if (frequency.containsKey(pubYear)) {
							frequency.put(pubYear, (frequency.get(pubYear) + 1));
						} else {
							frequency.put(pubYear, 1);
						}
					} else {
						HashMap<String, Integer> frequency = new HashMap<String, Integer>();
						frequency.put(pubYear, 1);
						frequencies.put(meshTerm, frequency);
					}
				}
			}		
			articles.clear();
			offset = offset + hoppingSize;
			articles = getArticleBeans(offset, hoppingSize);
			if (articles.size() == 0) continueFlag = false;
			else continueFlag = true;
		}

		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();
			Iterator<String> meshIterator = frequencies.keySet().iterator();
			while (meshIterator.hasNext()) {
				String meshTerm = meshIterator.next();
				HashMap<String, Integer> freqs = frequencies.get(meshTerm);
				Iterator<String> pubYearIterator = freqs.keySet().iterator();

				while (pubYearIterator.hasNext()) {
					String pubYear = pubYearIterator.next();
					int freqVale = freqs.get(pubYear);
					DBInterface.insertMESHFreqInfo(conn, meshTerm, "", pubYear, freqVale);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
		}
	}

	public static void makeJournalTROccurrenceDB() {
		HashMap<String, HashMap<String, Integer>> frequencies = new HashMap<String, HashMap<String, Integer>>();

		for (String journal : JOURNALS) {

			List<ArticleBean> articles = getArticleBeans(journal);
			int total = articles.size();
			int count = 1;
			for (ArticleBean bean : articles) {
				System.out.println(journal + "(" + count++ + "/" + total + ") : ");
				Document xmlDocument = XMLUtil.getParsedDocument(bean.getXmlKeyword(), false);
				String pubYear = MedlineCitationTools.getPubDate(bean.getXmlKeyword());
				String abstractString = PubmedUtils.getAbstractStr(xmlDocument);
				if (null == abstractString)
					continue;
				List<String> keywordStrings = TextRank.getKeywords(abstractString);

				for (String trKeyword : keywordStrings) {
					if(trKeyword.length() < 2) continue;
					if (frequencies.containsKey(trKeyword)) {
						HashMap<String, Integer> frequency = frequencies.get(trKeyword);
						if (frequency.containsKey(pubYear)) {
							frequency.put(pubYear, (frequency.get(pubYear) + 1));
						} else {
							frequency.put(pubYear, 1);
						}
					} else {
						HashMap<String, Integer> frequency = new HashMap<String, Integer>();
						frequency.put(pubYear, 1);
						frequencies.put(trKeyword, frequency);
					}
				}
			}

			Connection conn = null;
			try {
				conn = ConnectionManager.getConnection();
				Iterator<String> meshIterator = frequencies.keySet().iterator();
				while (meshIterator.hasNext()) {
					String trKeyword = meshIterator.next();
					HashMap<String, Integer> freqs = frequencies.get(trKeyword);
					Iterator<String> pubYearIterator = freqs.keySet().iterator();

					while (pubYearIterator.hasNext()) {
						String pubYear = pubYearIterator.next();
						int freqVale = freqs.get(pubYear);
						DBInterface.insertTRFreqInfo(conn, trKeyword, journal, pubYear, freqVale);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static void makeKeywordMESHOccurrenceDB() {
		HashMap<String, HashMap<String, Integer>> frequencies = new HashMap<String, HashMap<String, Integer>>();

		for (String keyword : KEYWORDS) {

			List<ArticleBean> articles = getArticleBeans(keyword);
			int total = articles.size();
			int count = 1;
			for (ArticleBean bean : articles) {
				System.out.println(keyword + "(" + count++ + "/" + total + ") : ");
				Document xmlDocument = XMLUtil.getParsedDocument(bean.getXmlKeyword(), false);
				String pubYear = MedlineCitationTools.getPubDate(bean.getXmlKeyword());
				HashMap<String, List<String>> meshTerms = PubmedUtils.meshterms(xmlDocument);

				Iterator<String> meshIterator = meshTerms.keySet().iterator();
				while (meshIterator.hasNext()) {
					String meshTerm = meshIterator.next();
					if (frequencies.containsKey(meshTerm)) {
						HashMap<String, Integer> frequency = frequencies.get(meshTerm);
						if (frequency.containsKey(pubYear)) {
							frequency.put(pubYear, (frequency.get(pubYear) + 1));
						} else {
							frequency.put(pubYear, 1);
						}
					} else {
						HashMap<String, Integer> frequency = new HashMap<String, Integer>();
						frequency.put(pubYear, 1);
						frequencies.put(meshTerm, frequency);
					}
				}
			}

			Connection conn = null;
			try {
				conn = ConnectionManager.getConnection();
				Iterator<String> meshIterator = frequencies.keySet().iterator();
				while (meshIterator.hasNext()) {
					String meshTerm = meshIterator.next();
					HashMap<String, Integer> freqs = frequencies.get(meshTerm);
					Iterator<String> pubYearIterator = freqs.keySet().iterator();

					while (pubYearIterator.hasNext()) {
						String pubYear = pubYearIterator.next();
						int freqVale = freqs.get(pubYear);
						DBInterface.insertMESHFreqInfo(conn, meshTerm, keyword, pubYear, freqVale);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static void makeKeywordTROccurrenceDB() {
		HashMap<String, HashMap<String, Integer>> frequencies = new HashMap<String, HashMap<String, Integer>>();

		for (String keyword : KEYWORDS) {
			List<ArticleBean> articles = getArticleBeans(keyword);
			int total = articles.size();
			int count = 1;
			for (ArticleBean bean : articles) {
				System.out.println(keyword + "(" + count++ + "/" + total + ") : ");
				Document xmlDocument = XMLUtil.getParsedDocument(bean.getXmlKeyword(), false);
				String pubYear = MedlineCitationTools.getPubDate(bean.getXmlKeyword());
				String abstractString = PubmedUtils.getAbstractStr(xmlDocument);
				if (null == abstractString)
					continue;
				List<String> trKeywords = TextRank.getKeywords(abstractString);

				for (String trKeyword : trKeywords) {
					if(trKeyword.length() < 2) continue;
					if (frequencies.containsKey(trKeyword)) {
						HashMap<String, Integer> frequency = frequencies.get(trKeyword);
						if (frequency.containsKey(pubYear)) {
							frequency.put(pubYear, (frequency.get(pubYear) + 1));
						} else {
							frequency.put(pubYear, 1);
						}
					} else {
						HashMap<String, Integer> frequency = new HashMap<String, Integer>();
						frequency.put(pubYear, 1);
						frequencies.put(trKeyword, frequency);
					}
				}
			}

			Connection conn = null;
			try {
				conn = ConnectionManager.getConnection();
				Iterator<String> meshIterator = frequencies.keySet().iterator();
				while (meshIterator.hasNext()) {
					String trKeyword = meshIterator.next();
					HashMap<String, Integer> freqs = frequencies.get(trKeyword);
					Iterator<String> pubYearIterator = freqs.keySet().iterator();

					while (pubYearIterator.hasNext()) {
						String pubYear = pubYearIterator.next();
						int freqVale = freqs.get(pubYear);
						DBInterface.insertTRFreqInfo(conn, trKeyword, keyword, pubYear, freqVale);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (conn != null)
						conn.close();
				} catch (Exception e) {
				}
			}
		}
	}


	public static void collectArticles() {
		try {
			List<String> pmids = FileUtils.readLines(new File("src/main/resources/data/PMID.txt"));
			constructArticleDB("experiment2", pmids);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		// List<String> result = extractNounPhraseFromAbstract("things",
		// "There are some important things to note with the solutions given above");
		// System.out.println("result : " + result);
		// makeBaseDB();
//		collectArticles();
		makeMESHOccurrenceDB();
//		makeJournalMESHOccurrenceDB();
//		makeJournalTROccurrenceDB();
//		makeKeywordMESHOccurrenceDB();
//		makeKeywordTROccurrenceDB();
	}
}
