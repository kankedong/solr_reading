package org.lxd.cniprSeg.dic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lxd.cniprSeg.TagDictionary;


public class TagDictionaryReader {
	
	private static String UTF8 = "UTF-8";
	private static String fileName = "partOfSpeechTransferFrequence.txt";
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public TagDictionary getDictionaryByFileName() throws IOException
	{
		TagDictionary dic = new TagDictionary();
		
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
		BufferedReader br = new BufferedReader( new InputStreamReader(is ,UTF8) );String line = null;
		int num = 0;
		while( (line=br.readLine())!=null )
		{
			line = line.trim();
			int prePosStart = 0;
			int prePosEnd   = line.indexOf(":");
			
			int tailPosStart = prePosEnd+1;
			int tailPosEnd   = line.lastIndexOf(":");
			
			int frequenceStart  = tailPosEnd+1;

			String prePosStr  = line.substring(prePosStart,prePosEnd) ;
			String tailPosStr = line.substring(tailPosStart,tailPosEnd);
			int frequence     =  Integer.valueOf(line.substring(frequenceStart));
					
			dic.createDictionaryNode(prePosStr,tailPosStr,frequence);
			num++;
		}
		
		return dic;
	}
	
	
	
}
