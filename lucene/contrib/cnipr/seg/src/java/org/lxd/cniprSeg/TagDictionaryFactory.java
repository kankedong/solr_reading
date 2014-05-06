package org.lxd.cniprSeg;

import java.io.IOException;

import org.lxd.cniprSeg.dic.TagDictionaryReader;

public class TagDictionaryFactory {
	
	private static TagDictionary TagDictionary = null ;
	private static TagDictionaryReader tagDictionaryReader = new TagDictionaryReader();
	
	/**
	 * 单例创建词典
	 * @return
	 * @throws IOException
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午4:36:27
	 * 修改时间:2014-2-26 下午4:36:27
	 */
	public synchronized static TagDictionary getTagDictionary() throws IOException
	{
		if( TagDictionary!=null )
		{
			return TagDictionary;
		}
		else
		{
			TagDictionary = tagDictionaryReader.getDictionaryByFileName();
			return TagDictionary;
		}
	}
}
