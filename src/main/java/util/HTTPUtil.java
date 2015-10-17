package util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class HTTPUtil
{
	static Logger logger = Logger.getLogger(HTTPUtil.class.getName());

	public HTTPUtil()
	{}

	private static final String HTTP_VERSION = "1.1";
	private static final String HTTP_POST = "POST";
	private static final String HEADER_HOST = "Host";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_CONTENT_LENGTH = "Content-Length";
	private static final int HTTP_DEFAULT_PORT = 80;
	public static final String lineSeparator = System.getProperty("line.separator");

	public static class Response
	{
		StringBuffer contentBuffer = new StringBuffer();
		boolean contentReaded = false;
		int statusCode;
		String statusString;
		HashMap<String, String> headers;
		int contentLength;
		String contentType;
		BufferedReader content;

		Response(int statusCode, String statusString, HashMap<String, String> headers, int contentLength, String contentType, BufferedReader content)
		{
			this.statusCode = statusCode;
			this.statusString = statusString;
			this.headers = headers;
			this.contentLength = contentLength;
			this.contentType = contentType;
			this.content = content;
		}

		public int getStatusCode()
		{
			return statusCode;
		}

		public String getStatusString()
		{
			return statusString;
		}

		private String getContentBuffer(boolean newline, boolean lastNewline)
		{
			StringBuffer sb = new StringBuffer();
			String lineS = lineSeparator;
			char lineSCh1 = lineS.charAt(0);
			char lineSCh2 = (lineS.length() == 2 ? lineS.charAt(1) : 0);
			int i = 0;
			char ch1 = 0, ch2;

			while (true)
			{
				try
				{
					ch1 = contentBuffer.charAt(i);

					if (lineSCh2 != 0)
					{
						ch2 = contentBuffer.charAt(i + 1);

						if (ch1 == lineSCh1 && ch2 == lineSCh2)
						{
							if (newline)
							{
								sb.append(ch1).append(ch2);
								++i;
							}
						}
						else
						{
							sb.append(ch1);
						}
					}
					else
					{
						if (ch1 == lineSCh1)
						{
							if (newline)
							{
								sb.append(ch1);
							}
						}
						else
						{
							sb.append(ch1);
						}
					}

					++i;

				}
				catch (IndexOutOfBoundsException e)
				{
					if (i < contentBuffer.length())
					{
						sb.append(ch1);
					}
					break;
				}
			}

			if (lastNewline)
			{
				sb.append(lineS);
			}

			return sb.toString();
		}

		public String getContent(boolean newline, boolean lastNewline, boolean useCache) throws IOException
		{
			if (!useCache && !contentReaded)
			{
				String line;
				StringBuffer buffer = new StringBuffer();

				// Blank 라인 skip
				line = content.readLine();

				line = content.readLine();

				while (line != null)
				{
					buffer.append(line);
					line = content.readLine();

					if (line != null && newline)
					{
						buffer.append(lineSeparator);
					}
				}

				if (lastNewline)
				{
					buffer.append(lineSeparator);
				}

				return buffer.toString();
			}

			if (!contentReaded)
			{
				String line;
				line = content.readLine();

				while (line != null)
				{
					contentBuffer.append(line);
					line = content.readLine();

					if (line != null)
					{
						contentBuffer.append(lineSeparator);
					}
				}

				contentReaded = true;
			}

			return getContentBuffer(newline, lastNewline).toString();
		}

		public String getContent() throws IOException
		{
			return getContent(true, false, false);
		}
	}

	public static Response Post(URL url, HashMap<String, String> headers, HashMap<String, String> params, String contentType, String encoding, String timeout, String userName, String password) throws InterruptedIOException, Exception
	{
		HttpURLConnection uc = null;
		PrintWriter writer = null;
		BufferedReader reader = null;
		String query = "";

		if (params != null)
		{
			Iterator<String> iter = params.keySet().iterator();
			String name, value;
			while (iter.hasNext())
			{
				name = iter.next();
				value = params.get(name);
				query += URLEncoder.encode(name, encoding) + "=";
				query += URLEncoder.encode(value, encoding) + "&";
				// query += URLEncoder.encode(name) + "=";
				// query += URLEncoder.encode(value) + "&";
			}

			if (query.charAt(query.length() - 1) == '&')
			{
				query = query.substring(0, query.length() - 1);
			}
		}

		uc = (HttpURLConnection) url.openConnection();
		uc.setRequestMethod("POST");
		uc.setDoInput(true);
		uc.setDoOutput(true);
		uc.setUseCaches(false);
		uc.setDefaultUseCaches(false);

		if (contentType != null && !contentType.equals(""))
		{
			uc.setRequestProperty(HEADER_CONTENT_TYPE, contentType);
		}
		else
		{
			uc.setRequestProperty(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded");
		}

		if (userName != null && !userName.equals(""))
		{
			if (password == null) password = "";

			String userPassword = userName + ":" + password;
			String encodedPass = userPassword;

			uc.setRequestProperty("Authorization", "Basic " + encodedPass);
		}

		if (headers != null)
		{
			Iterator<String> iter = headers.keySet().iterator();

			while (iter.hasNext())
			{
				String key = iter.next();
				uc.setRequestProperty(key, headers.get(key));
			}
		}
		else
		{
			uc.setRequestProperty("SOAPAction", "\"\"");
		}

//		writer = new PrintWriter(new OutputStreamWriter(TimedURLConnection.getOutputStream(uc, Integer.parseInt(timeout))));

		if (query != null)
		{
			writer.print(query);
		}

		writer.flush();
		writer.close();

//		reader = new BufferedReader(new InputStreamReader(TimedURLConnection.getInputStream(uc, Integer.parseInt(timeout)), encoding));

		Response response = null;
		HashMap<String, String> retHeaders = new HashMap<String, String>();
		String hKey = null;
		String hValue = null;

		int i = 0;

		while (true)
		{
			hKey = uc.getHeaderFieldKey(i);
			hValue = uc.getHeaderField(i);

			try
			{
				if (i == 0 && hKey == null) hKey = "";
				retHeaders.put(hKey, hValue);
			}
			catch (Exception e)
			{
				break;
			}

			i++;
		}

		int retContentLength = uc.getContentLength();
		String retContentType = uc.getContentType();
		String retCodeStatus = (String) retHeaders.get("");

		int idx1 = retCodeStatus.indexOf(" ") + 1;
		int idx2 = retCodeStatus.indexOf(" ", idx1);
		int retStatusCode = 200;
		String retStatusString = "OK";

		if (idx2 == -1)
		{
			retStatusCode = Integer.parseInt(retCodeStatus.substring(idx1, idx1 + 3));
			retStatusString = " ";
		}
		else
		{
			retStatusCode = Integer.parseInt(retCodeStatus.substring(idx1, idx2));
			retStatusString = retCodeStatus.substring(idx2 + 1);
		}

		response = new Response(retStatusCode, retStatusString, retHeaders, retContentLength, retContentType, reader);

		return response;

	}

	public static String HTTPPost(String urlStr, HashMap<String, String> params, String encoding, String timeout, String userName, String password) throws InterruptedIOException, Exception
	{
		if (timeout == null || ("").equals(timeout) || ("0").equals(timeout))
		{
			timeout = "30000"; // default 30 sec.
		}

		URL url = new URL(urlStr);

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("SOAPAction", "\"\"");

		Response response = Post(url, headers, params, "application/x-www-form-urlencoded", encoding, timeout, userName, password);

		if (200 == response.getStatusCode())
		{
			return response.getContent();
		}
		else
		{
			return null;
		}

	}

	public static String getSimplePOSTResponse(String url, HashMap<String, String> param, String encoding) throws MalformedURLException, IOException, ProtocolException
	{
		BufferedReader br = null;
		String response = null; 
		String line = null;
		String query = "";

		if (param != null)
		{
			Iterator<String> iter = param.keySet().iterator();
			String name, value;
			while (iter.hasNext())
			{
				name = iter.next();
				value = param.get(name);
				// query += URLEncoder.encode(name, "UTF-8") + "=";
				// query += URLEncoder.encode(value, "UTF-8") + "&";
				query += URLEncoder.encode(name) + "=";
				query += URLEncoder.encode(value) + "&";
			}

			if (query.charAt(query.length() - 1) == '&')
			{
				query = query.substring(0, query.length() - 1);
			}
		}

		URL encodedURL = new URL(url);
		HttpURLConnection httpCon = (HttpURLConnection) encodedURL.openConnection();
		httpCon.setDoInput(true);
		httpCon.setDoOutput(true);
		httpCon.setUseCaches(false);
		httpCon.setDefaultUseCaches(false);
		httpCon.setRequestMethod("POST");
		httpCon.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

		OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(httpCon.getOutputStream()));
		out.write(query);
		out.flush();
		out.close();

		br = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), encoding));
		response = "";

		while ((line = br.readLine()) != null)
		{
			response += line + lineSeparator;
		}

		return response;
	}


	/**
	 * 특정 URL의 내용을 가져온다. HTTP의 GET과 똑같은 역할을 한다.
	 * 
	 * @param location : URL 주소
	 * @return : location에 대한 GET 호출 결과
	 */
	public static String getURLContent(String location)
	{
		BufferedReader input = null;
		StringBuffer buffer = new StringBuffer();
		URL url = null;
		String line = "";

		try
		{
			url = new URL(location);
			input = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

			while ((line = input.readLine()) != null)
			{
				buffer.append(line).append('\n');
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (Exception e)
				{}
			}
		}

		return buffer.toString();
	}

	public static void main(String args[]) throws Exception
	{
		//		Hashtable hash = new Hashtable();
		//		hash.put("inXML", "<?xml version='1.0' encoding='euc-kr'?><test>한글value=2</test>");
		//		System.out.println(hash.toString());
		//		String ret = HTTPUtil.HTTPPost("http://218.236.240.44:9001/koreg/AppReceive.jsp", hash, "euc-kr", "10000", "", "");
		//		System.out.println("ret[" + ret + "]");
		//		String ret = HTTPUtil.getSimplePOSTResponse("http://218.236.240.44:9001/koreg/AppReceive.jsp", hash, "euc-kr");
		//		System.out.println("ret[" + ret + "]");
		//		String ret = HTTPUtil.getBinary("http://195.132.143.28/P38API/RunTopo");
	}
}
