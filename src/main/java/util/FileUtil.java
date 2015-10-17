package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileUtil
{
	private static Logger logger = Logger.getLogger(FileUtil.class.getName());

	public FileUtil()
	{}

	private static FileUtil instance = null;

	public static FileUtil getInstance()
	{
		if (instance == null)
		{
			instance = new FileUtil();
			return instance;
		}
		else return instance;
	}

	public synchronized static void writeFile(String filepath, String contents, boolean mode)
	{
		FileWriter fwriter = null;
		BufferedWriter bwriter = null;

		try
		{
			fwriter = new FileWriter(filepath, mode);
			// bwriter = new BufferedWriter(fwriter);
			bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath, mode), "UTF-8"));

			bwriter.write(contents);
			bwriter.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		finally
		{
			try
			{
				if (bwriter != null)
				{
					bwriter.close();
				}
			}
			catch (Exception e)
			{}
		}
	}

	public synchronized static String readFile(String filepath)
	{
		StringBuffer contents = new StringBuffer();
		FileReader freader = null;
		BufferedReader breader = null;

		try
		{
			freader = new FileReader(filepath);
			// breader = new BufferedReader(freader);
			breader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"));
			String str = null;

			while ((str = breader.readLine()) != null)
			{
				contents.append(str).append(System.getProperty("line.separator", "\n"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		finally
		{
			try
			{
				if (breader != null)
				{
					breader.close();
				}
			}
			catch (Exception ex)
			{}
		}

		return contents.toString();
	}
	
	public synchronized static void copyFile(File file1, File file2)
	{
		try
		{
			FileInputStream fin = new FileInputStream(file1);
			FileOutputStream fout = new FileOutputStream(file2);
			FileChannel in = fin.getChannel();
			FileChannel out = fout.getChannel();

			in.transferTo(0, in.size(), out);
			in.close();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
	
	public static void main(String args[])
	{
		StringBuffer contents = new StringBuffer();
		BufferedReader breader = null;
		
		try
		{
			breader = new BufferedReader(new InputStreamReader(new FileInputStream("C:/eclipse/data/download/CID-Synonym-filtered"), "UTF-8"));
			String str = null;
			int line = 0;
			
			while ((str = breader.readLine()) != null)
			{
				contents.append(str).append(System.getProperty("line.separator", "\n"));
				
				line++;
				logger.info(line);
				if((line % 10000000) == 0)
				{
					writeFile("C:/eclipse/data/download/CID-Synonym-filtered" + line, contents.toString(), false);
					contents = new StringBuffer();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		finally
		{
			try
			{
				if (breader != null)
				{
					breader.close();
				}
			}
			catch (Exception ex)
			{}
		}
	}
}
