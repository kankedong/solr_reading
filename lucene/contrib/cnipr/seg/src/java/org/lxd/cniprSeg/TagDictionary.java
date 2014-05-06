package org.lxd.cniprSeg;

import java.util.HashMap;
import java.util.Map;

public class TagDictionary {
	
	private long totalFrequence = 0L;
	
	private Map<String,TagDictionaryNode>  dicMap = null;
	
	public Map<String, TagDictionaryNode> getDicMap() {
		return dicMap;
	}
	
	/**
	 * 
	 * @param word
	 * @param pinyin
	 * @param frequece
	 * @param pos
	 * @param posFrequece
	 */
	public void createDictionaryNode( String prePosStr,String tailPosStr,int transFrequence )
	{		
		if( prePosStr==null || prePosStr.trim().equals("") || tailPosStr==null || tailPosStr.equals("") )
			return;
	
		if( dicMap==null )
		{
			dicMap = new HashMap<String,TagDictionaryNode>(256);
		}
		
		TagDictionaryNode tagNode = (TagDictionaryNode)dicMap.get(prePosStr);
		getOrCreateNode(dicMap,tagNode,prePosStr,tailPosStr,transFrequence);
	}
	
	

	private TagDictionaryNode getOrCreateNode(Map<String,TagDictionaryNode> dicMap,TagDictionaryNode tagNode,String prePosStr,String tailPosStr, int transFrequence) throws NullPointerException,
			IllegalArgumentException {
		
		if (tagNode == null) {
			tagNode = new TagDictionaryNode(prePosStr);
			tagNode.getTagTransnHashMap().put(prePosStr, transFrequence);
			dicMap.put(prePosStr,tagNode);
		}
		else
		{
			tagNode.getTagTransnHashMap().put(tailPosStr, transFrequence);
		}
		
		this.totalFrequence += transFrequence;
		return tagNode;
	}
	
	public Map<String,Integer> getAllPartOfSpeechOfNextWord( String currentWordPartOfSpeech )
	{
		if( currentWordPartOfSpeech==null || currentWordPartOfSpeech.trim().equals("") )
		{
			return null;
		}
		else
		{
			TagDictionaryNode node = dicMap.get(currentWordPartOfSpeech);
			
			if( node!=null )
			{
				return node.getTagTransnHashMap();
			}
			else
			{
				return null;
			}
		}
		
	}

	public long getTotalFrequence() {
		return totalFrequence;
	}

	public void setTotalFrequence(long totalFrequence) {
		this.totalFrequence = totalFrequence;
	}

}
