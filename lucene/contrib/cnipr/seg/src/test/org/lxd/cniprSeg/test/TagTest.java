package org.lxd.cniprSeg.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.lxd.cniprSeg.BgramDictionary;
import org.lxd.cniprSeg.BgramDictionaryNode;
import org.lxd.cniprSeg.MainDictionary;
import org.lxd.cniprSeg.MainDictionaryNode;
import org.lxd.cniprSeg.TagDictionary;
import org.lxd.cniprSeg.WordByWordSegNode;
import org.lxd.cniprSeg.WordByWordSegResult;
import org.lxd.cniprSeg.dic.BgramDictionaryReader;
import org.lxd.cniprSeg.dic.MainDictionaryReader;
import org.lxd.cniprSeg.dic.TagDictionaryReader;


public class TagTest {
	
	@Test
	public void testReader() throws IOException
	{
		TagDictionaryReader reader = new TagDictionaryReader();
		TagDictionary dic = reader.getDictionaryByFileName();
		System.out.println( dic.getTotalFrequence() );
	}
	
	@Test
	public void testTagging() throws IOException
	{
		TagDictionaryReader reader = new TagDictionaryReader();
		reader.getDictionaryByFileName();
		
		
		MainDictionaryReader mainDicReader = new MainDictionaryReader();
		MainDictionary mainDic = mainDicReader.getDictionaryByFileName(); 
		
		BgramDictionaryReader bgramDicReader = new BgramDictionaryReader();
		BgramDictionary bgramDic = bgramDicReader.getDictionaryByFileName();
		
		System.out.println( "mainDic.totalFrequence" + mainDic.totalFrequence );
		
		String sentence = "中国人喜欢吃米饭";
		
		WordByWordSegResult wbwSegResult = new WordByWordSegResult();
		
		int start = 0;
		while( start<(sentence.length()) )
		{
			int end=start+1;
			while( end<=sentence.length() )
			{
				String word = sentence.substring(start,end);
				MainDictionaryNode mainDicnode = mainDic.getNode(word);
				if( mainDicnode==null || !mainDicnode.isEndWord())//待优�?
				{
					end++;
					continue;
				}
				else
				{
					BgramDictionaryNode bgramDicNode =  bgramDic.getNode(word);
					WordByWordSegNode result = new WordByWordSegNode( word,start,end,mainDicnode,bgramDicNode );
					wbwSegResult.add(result);
					end++;
				}
				
			}
			start++;
		}
		

		
		List<WordByWordSegNode> resultList = wbwSegResult.getBestSeg(mainDic.totalFrequence);
		
		for( int i=resultList.size()-1;i>=0;i-- )
		{
			System.out.println( "[word:"+resultList.get(i).getWord()+"|start:"+resultList.get(i).getStart()+"|end:"+resultList.get(i).getEnd()+"|accumulationProbability:"+resultList.get(i).getAccumulationProbability() );
		}
		
		wbwSegResult.getBestSegTagging(resultList);
		

	}
		
}
