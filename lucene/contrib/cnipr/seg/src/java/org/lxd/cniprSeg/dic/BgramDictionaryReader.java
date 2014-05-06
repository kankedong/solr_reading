package org.lxd.cniprSeg.dic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.lxd.cniprSeg.BgramDictionary;


public class BgramDictionaryReader {
	
	static final Logger log = Logger.getLogger(BgramDictionaryReader.class.getName());
	private static String UTF8 = "UTF-8";
	private static String fileName = "bigramInfoDictionary.txt";
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public BgramDictionary getDictionaryByFileName() throws IOException
	{
		BgramDictionary dic = new BgramDictionary();
		
//		File dicFilePath = DictionaryPathFactory.getDicFilePath(fileName);
//		
//		if( dicFilePath==null ){
//			return null;
//		}
//					
//		BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(dicFilePath) ,UTF8) );
		
		InputStream is = DictionaryPathFactory.getDicFileInputStream(fileName);
		if( is==null ){
			return null;
		}
		BufferedReader br = new BufferedReader( new InputStreamReader(is ,UTF8) );
		
		String line = null;
		int num = 0;
		while( (line=br.readLine())!=null )
		{
			line = line.trim();
			int firstWordStart = 0;
			int firstWordEnd   = line.indexOf("@");
			
			int secondWordStart = firstWordEnd+1;
			int secondWordEnd   = line.indexOf(":");
			int frequenceStart  = secondWordEnd+1;

			String firstWord  = line.substring(firstWordStart,firstWordEnd) ;
			String secondWord = line.substring(secondWordStart,secondWordEnd);
			int frequence =  Integer.valueOf(line.substring(frequenceStart));
					
			dic.createDictionaryNode(firstWord,secondWord,frequence);
			num++;
		}
		
//		System.out.println(num);
		
		return dic;
	}
	
	
	
}
