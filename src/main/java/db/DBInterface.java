package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class DBInterface {
	
	public static synchronized void insertKeywordBaseRecord(int pmid, String query, String mterms, String uskeys, 
			String exkeys, String xmlDocString, String abstractString)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;

		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement("INSERT INTO BASE(PMID, QUERY, MTERMS, USKEYS, EXKEYS, XMLDOC, DOC) VALUES(?, ?, ?, ?, ?, ?, ?)");

			pstmt.setInt(1, pmid);
			pstmt.setString(2, query);
			pstmt.setString(3, mterms);
			pstmt.setString(4, uskeys);
			pstmt.setString(5, exkeys);
			pstmt.setString(6, xmlDocString);
			pstmt.setString(7, abstractString);

			pstmt.executeUpdate();
			pstmt.clearParameters();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (conn != null) conn.close();
			}
			catch (Exception e)
			{}
		}
	}
	
	public static synchronized void insertArticleBean(ArticleBean article)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;

		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement("INSERT INTO ARTICLES(PMID, XMLDOC, JOURNAL) VALUES(?, ?, ?)");

			pstmt.setString(1, article.getPmid());
			pstmt.setString(2, article.getXmlKeyword());
			pstmt.setString(3, article.getJournalName());

			pstmt.executeUpdate();
			pstmt.clearParameters();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (conn != null) conn.close();
			}
			catch (Exception e)
			{}
		}
	}
	
	public static synchronized void insertMESHFreqInfo(Connection conn, String meshTerm, String journal, String pubYear, int freq)
	{
//		Connection conn = null;
		PreparedStatement pstmt = null;

		try
		{
//			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement("INSERT INTO MESHFREQ(MESHTERM, JOURNAL, YEAR, FREQ) VALUES(?, ?, ?,?)");

			pstmt.setString(1, meshTerm);
			pstmt.setString(2, journal);
			pstmt.setString(3, pubYear);
			pstmt.setInt(4, freq);

			pstmt.executeUpdate();
			pstmt.clearParameters();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
//			try
//			{
//				if (conn != null) conn.close();
//			}
//			catch (Exception e)
//			{}
		}
	}
	
	public static synchronized void insertTRFreqInfo(Connection conn, String trKeyword, String keyword, String pubYear, int freq)
	{
//		Connection conn = null;
		PreparedStatement pstmt = null;

		try
		{
//			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement("INSERT INTO TRFREQ(TRKEYWORD, KEYWORD, YEAR, FREQ) VALUES(?, ?, ?,?)");

			pstmt.setString(1, trKeyword);
			pstmt.setString(2, keyword);
			pstmt.setString(3, pubYear);
			pstmt.setInt(4, freq);

			pstmt.executeUpdate();
			pstmt.clearParameters();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
//			try
//			{
//				if (conn != null) conn.close();
//			}
//			catch (Exception e)
//			{}
		}
	}

	
	public static synchronized void insertInvertRecord(Connection conn, Integer pmid, String source, String target)
	{
		PreparedStatement pstmt = null;

		try
		{
			pstmt = conn.prepareStatement("INSERT INTO INVERT(PMID, SOURCE, TARGET) VALUES(?, ?, ?)");

			pstmt.setInt(1, pmid);
			pstmt.setString(2, source);
			pstmt.setString(3, target);

			pstmt.executeUpdate();
			pstmt.clearParameters();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static synchronized void insertContingencyRecord(Connection conn, String source, String target, int freq)
	{
		PreparedStatement pstmt = null;
		try
		{
			pstmt = conn.prepareStatement("INSERT INTO COOC(SOURCE, TARGET, FREQ) VALUES(?, ?, ?)");

			pstmt.setString(1, source);
			pstmt.setString(2, target);
			pstmt.setInt(3, freq);

			pstmt.executeUpdate();
			pstmt.clearParameters();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static synchronized void insertClassifyResults(Connection conn, HashMap<String, String> maps)
	{
		PreparedStatement pstmt = null;
		try
		{
			Iterator<String> iter = maps.keySet().iterator();
			while(iter.hasNext()) {
				String pmid = iter.next();
				pstmt = conn.prepareStatement("INSERT INTO TCMARTICLE(PMID, FLAG) VALUES(?, ?)");
	
				pstmt.setString(1, pmid);
				pstmt.setString(2, maps.get(pmid));
	
				pstmt.executeUpdate();
				pstmt.clearParameters();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
//	public static synchronized BaseRecord getBaseRecord(Connection conn, String pmid){
//		BaseRecord record = null;
//		
//		try{
//			Statement sqlStatement = conn.createStatement();
//			String sqlQuery = "SELECT PMID, QUERY, MTERMS, USKEYS, XMLDOC, DOC FROM kiom.BASE WHERE PMID = '" + pmid + "'";
//			
//			ResultSet resultSet = sqlStatement.executeQuery(sqlQuery);
//			while(resultSet.next()) {
//				String query = resultSet.getString("QUERY");
//				String meshTerms = resultSet.getString("MTERMS");
//				String userKeywords = resultSet.getString("USKEYS");
//				String xmlString = resultSet.getString("XMLDOC");
//				String abstractString = resultSet.getString("DOC");
//				
//				record = new BaseRecord(pmid, query, meshTerms, userKeywords, xmlString, abstractString);
//			}
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//		
//		return record;
//	}
	
	@SuppressWarnings("finally")	
	public static HashSet<Integer> getUniquePMIDS(Connection conn) {
		Statement stmt = null;
		HashSet<Integer> result = new HashSet<Integer>();

		try {
			String sql = "SELECT DISTINCT PMID FROM kiom.BASE ORDER BY PMID DESC LIMIT 1000000 ";
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int pmid = rs.getInt("PMID");
				result.add(pmid);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionManager.closeStatement(stmt);
			return result;
		}
	}
	
	public static synchronized String getJournalName(Connection conn, String pmid){
		String journalName = null;

		try{
			Statement sqlStatement = conn.createStatement();
			String sqlQuery = "SELECT JOURNAL FROM JOURNAL WHERE PMID = '" + pmid + "'";
			
			ResultSet resultSet = sqlStatement.executeQuery(sqlQuery);
			while(resultSet.next()) {
				journalName = resultSet.getString("JOURNAL");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return journalName;
	}
	
	
	
	public static synchronized String getAffiliation(Connection conn, String pmid){
		String journalName = null;
		Statement sqlStatement = null;

		try{
			sqlStatement = conn.createStatement();
			String sqlQuery = "SELECT AFFILIATION FROM AFFILIATION WHERE PMID = '" + pmid + "'";
			
			ResultSet resultSet = sqlStatement.executeQuery(sqlQuery);
			while(resultSet.next()) {
				journalName = resultSet.getString("AFFILIATION");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
			ConnectionManager.closeStatement(sqlStatement);
		}
		
		return journalName;
	}
}
