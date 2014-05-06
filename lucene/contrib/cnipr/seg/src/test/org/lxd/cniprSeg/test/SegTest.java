package org.lxd.cniprSeg.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.lxd.cniprSeg.BgramDictionary;
import org.lxd.cniprSeg.BgramDictionaryNode;
import org.lxd.cniprSeg.MainDictionary;
import org.lxd.cniprSeg.MainDictionaryNode;
import org.lxd.cniprSeg.WordByWordSegNode;
import org.lxd.cniprSeg.WordByWordSegResult;
import org.lxd.cniprSeg.dic.BgramDictionaryReader;
import org.lxd.cniprSeg.dic.MainDictionaryReader;






public class SegTest {
	

	@Test
	public void test() throws IOException
	{
		MainDictionaryReader mainDicReader = new MainDictionaryReader();
		MainDictionary mainDic = mainDicReader.getDictionaryByFileName(); 
		
		BgramDictionaryReader bgramDicReader = new BgramDictionaryReader();
		BgramDictionary bgramDic = bgramDicReader.getDictionaryByFileName();
		
		System.out.println( "mainDic.totalFrequence" + mainDic.totalFrequence );
		
		String sentence = "中国人喜欢吃米饭";
//		String sentence = "刘毅：男，1968年6月出生，毕业于北京师范大学，本科。历任中国基建物资总公司会计、中国华通物产集团公司期货部风险控制部经理、中国电子信息产业发展研究院财务处副处长。";
		sentence = "刘毅：男，1968年6月出生，毕业于北京师范大学，本科。";
//		sentence = "鬃毛最今在市场上广受欢迎，供不应求。";
//		sentence = "appfog不再提供2g的内存了.只有512m了.所以新词发现和nlp方式的分词无法演示.谁有大内存的免费app引擎可以给我推荐下!";
		
		sentence = "appfog不再提供2g的内存了";//"。只有512m了。所以新词发现和nlp方式的分词无法演示。谁有大内存的免费app引擎可以给我推荐下!";
		
		
		WordByWordSegResult wbwSegResult = new WordByWordSegResult();
		
		int start = 0;
		while( start<(sentence.length()) )
		{
			int end=start+1;
			while( end<=sentence.length() )
			{
				String word = sentence.substring(start,end);//换成避免内存使用的方式
				MainDictionaryNode mainDicnode = mainDic.getNode(word);
				if( mainDicnode==null )
				{
					if( word.length()==1 )
					{
					}
					else
					{
						break;
					}
					
				}
				else if( !mainDicnode.isEndWord())//待优�?
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
		
//		for( WordByWordSegNode currentNode:segNodeList )
//		{
//			System.out.println("*******************************************************************************************************");
//			System.out.println( "[currentWord:" + currentNode.getWord() + "|start:" + currentNode.getStart() + "|end:" + currentNode.getEnd()+ "|frequence:"+currentNode.getMainDicNode().getFrequence() + "]" );
//			List<WordByWordSegNode> preNodeList = wbwSegResult.getPreNodeList(currentNode);
//			
//			WordByWordSegNode bestPreNode = null;
//			double accumulationProbability = 0;
//			
//			if( preNodeList!=null )
//			{
//				for( WordByWordSegNode preNode: preNodeList )
//				{
//					System.out.println( "[preWord:" + preNode.getWord() + "|start:" + preNode.getStart() + "|end:" + preNode.getEnd() + "|frequence:" + preNode.getMainDicNode().getFrequence() + "|transitionProbability:" + transitionProbability( preNode,currentNode,mainDic.totalFrequence ) +"]"  );
//					double currentProbability = transitionProbability(preNode, currentNode,mainDic.totalFrequence)+ preNode.getAccumulationProbability();
//					if (currentProbability > accumulationProbability) {
//						accumulationProbability = currentProbability;
//						bestPreNode = preNode;
//					}
//					
//				}
//				
//				currentNode.setAccumulationProbability(accumulationProbability);
//				currentNode.setBestPreNode(bestPreNode);
//			}
//			
//			
////			double wordProb = minValue; // 侯�?词概�?
////			CnToken minToken = null;
////			if (prevWordList == null)
////				continue;
////			for (CnToken prevWord : prevWordList) {
////				double currentProb = transProb(prevWord, currentWord)+ prevWord.nodeProb;
////				if (currentProb > wordProb) {
////					wordProb = currentProb;
////					minToken = prevWord;
////				}
////			}
////			currentWord.bestPrev = minToken; // 设置当前词的�?��前驱�?
////			currentWord.nodeProb = wordProb; // 设置当前词的词概�?
//		}
		
		
		
//		Iterator<WordByWordSegResult> iterList = startList.iterator();
//		while( iterList.hasNext() )
//		{
//			WordByWordSegResult result = (WordByWordSegResult)iterList.next();
//			System.out.println( result.getWord() );
//			
//			int end = result.getEnd();
//			List<WordByWordSegResult> tempList = wbwSegResultMap.getSegResultList(end);
//			while( tempList )
//		}
		
	}
	
	public static void recursion( List<WordByWordSegNode> startList, WordByWordSegResult wbwSegResultMap, String str )
	{
		if( null==startList )
		{
			System.out.println("");
			System.out.println(str);
			return;
		}
			

		Iterator<WordByWordSegNode> iterList = startList.iterator();
		while( iterList.hasNext() )
		{
			WordByWordSegNode result = (WordByWordSegNode)iterList.next();
			str += result.getWord()+"|";
//			System.out.print( result.getWord()+"|" );
			
			int end = result.getEnd();
			List<WordByWordSegNode> tempList = wbwSegResultMap.getStartWordList(end);
			recursion(tempList,wbwSegResultMap,str);
		}
	}
	
	
	
}
