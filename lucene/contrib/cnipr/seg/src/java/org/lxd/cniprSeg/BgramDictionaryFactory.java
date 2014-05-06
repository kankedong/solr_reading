package org.lxd.cniprSeg;

import java.io.IOException;

import org.lxd.cniprSeg.dic.BgramDictionaryReader;

public class BgramDictionaryFactory {
	
	private static BgramDictionary BgramDictionary = null ;
	private static BgramDictionaryReader bgramDictionaryReader = new BgramDictionaryReader();
	
	/**
	 * 单例创建二元词典
	 * @return
	 * @throws IOException
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午4:36:27
	 * 修改时间:2014-2-26 下午4:36:27
	 */
	public synchronized static BgramDictionary getBgramDictionary() throws IOException
	{
		if( BgramDictionary!=null )
		{
			return BgramDictionary;
		}
		else
		{
			BgramDictionary = bgramDictionaryReader.getDictionaryByFileName();
			return BgramDictionary;
		}
	}
}
