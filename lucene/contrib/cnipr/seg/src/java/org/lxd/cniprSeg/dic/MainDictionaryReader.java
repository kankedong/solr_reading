package org.lxd.cniprSeg.dic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.lxd.cniprSeg.MainDictionary;
import org.lxd.cniprSeg.PosEnum;
import org.lxd.cniprSeg.Utils;


public class MainDictionaryReader {

	private static String UTF8 = "UTF-8";
	private static String fileName = "mainInfoDictionary.txt";
	
	/**
	 * 通过文件名获取主词典文件
	 * @return
	 * @throws IOException
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午4:18:29
	 * 修改时间:2014-2-26 下午4:18:29
	 */
	public MainDictionary getDictionaryByFileName() throws IOException
	{
		MainDictionary dic = new MainDictionary();

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
		final String separator = "_SEPARATOR_";
		final int separatorLength = separator.length();
		while( (line=br.readLine())!=null )
		{
			
			
			int wordStart = 0;
			int wordEnd   = line.indexOf(separator);
			
			int posStart = wordEnd+separatorLength;
			int posEnd   = line.indexOf(separator,wordEnd+1);
			
			int frequenceStart = posEnd+separatorLength;
			int frequenceEnd = line.indexOf(separator,frequenceStart+1);
			
			String word = line.substring(wordStart,wordEnd);
			String pos  = line.substring(posStart,posEnd);
			int frequence = Integer.valueOf(line.substring(frequenceStart, frequenceEnd));
			PosEnum posEnum = Utils.getPosEnumValue(pos);
			
			
			dic.createDictionaryNode(word,posEnum,frequence,null,0);
			num++;
		}

		return dic;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String,Integer> getPosMapByFileName() throws IOException
	{
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		File dicFilePath = DictionaryPathFactory.getDicFilePath(fileName);
		
		if( dicFilePath==null ){
			return null;
		}
					
		BufferedReader br = new BufferedReader( new InputStreamReader(new FileInputStream(dicFilePath) ,UTF8) );
		
		String line = null;
		int num = 0;
		final String separator = "_SEPARATOR_";
		final int separatorLength = separator.length();
		while( (line=br.readLine())!=null )
		{
			int wordStart = 0;
			int wordEnd   = line.indexOf(separator);
			
			int posStart = wordEnd+separatorLength;
			int posEnd   = line.indexOf(separator,wordEnd+1);
			
			int frequenceStart = posEnd+separatorLength;
			int frequenceEnd = line.indexOf(separator,frequenceStart+1);
			
			String word = line.substring(wordStart,wordEnd);
			String pos  = line.substring(posStart,posEnd);
			int frequence = Integer.valueOf(line.substring(frequenceStart, frequenceEnd));
			PosEnum posEnum = Utils.getPosEnumValue(pos);
			
			
			Integer value = (Integer)map.get(pos);
			if( value==null )
			{
				map.put(pos, frequence);
			}
			else
			{
				value+=frequence;
				map.put(pos, value);
			}
			
			num++;
		}

		return map;
	}
	
	
	

	
}
