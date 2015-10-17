package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import common.Constants;


public class ConnectionManager
{
	public static Connection conn = null;
	static Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	
	public static synchronized Connection getConnection() throws Exception
	{
		if (conn == null || conn.isClosed()) {
			try
			{
				Class.forName(Constants.mysqlDriver);
	
				Properties props = new Properties();
				props.put("user", Constants.mysqlID);
				props.put("password", Constants.mysqlPasswd);
				props.put("encoding", Constants.encoding);
				props.put("charSet", Constants.charSet);
	
				conn = DriverManager.getConnection(Constants.mysqlURI, props);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw e;
			}
		}

		return conn;
	}
	
	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

}
