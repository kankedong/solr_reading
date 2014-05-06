package org.lxd.cniprSeg;

import java.io.IOException;

import org.lxd.cniprSeg.dic.MainDictionaryReader;

public class MainDictionaryFactory {
	
	private static MainDictionary mainDictionary = null ;
	private static MainDictionaryReader mainDicReader = new MainDictionaryReader();
	
	/**
	 * 单例创建主分词词典
	 * @return
	 * @throws IOException
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午4:36:27
	 * 修改时间:2014-2-26 下午4:36:27
	 */
	public synchronized static MainDictionary getMainDictionary() throws IOException
	{
		if( mainDictionary!=null )
		{
			return mainDictionary;
		}
		else
		{
			mainDictionary = mainDicReader.getDictionaryByFileName();
			return mainDictionary;
		}
	}
}
