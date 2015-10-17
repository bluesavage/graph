package textrank;

import java.io.IOException;
import java.io.StringReader;

import org.tartarus.snowball.ext.EnglishStemmer;

public class PorterStemmer {
	public static String stem(String string) {
		EnglishStemmer english = new EnglishStemmer();
		english.setCurrent(string);
		english.stem();
		return english.getCurrent();
	}
	
	public static void main(String args[]) {
		try{
			PorterStemmer.stem("computing");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}