package org.lxd.cniprSeg;

import java.util.HashMap;
import java.util.Map;

public class BgramDictionary {
	
	long totalFrequence = 0L;
	
	private Map<Character,BgramDictionaryNode>  dicMap = null;
	
	public Map<Character, BgramDictionaryNode> getDicMap() {
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
	public void createDictionaryNode( String firstWord,String secondWord,int frequence )
	{		
		if( firstWord==null || firstWord.trim().equals("") || secondWord==null || secondWord.equals("") )
			return;

		
		if( dicMap==null )
		{
			dicMap = new HashMap<Character,BgramDictionaryNode>(2048);
		}
		
		Character firstChar = firstWord.charAt(0);
		BgramDictionaryNode firstNode = (BgramDictionaryNode)dicMap.get(firstChar);
		getOrCreateNode(dicMap,firstNode,firstWord,secondWord, frequence);
	}
	
	/**
	 * ��ѯһ����
	 * @param word
	 * @return
	 */
	public BgramDictionaryNode getNode(String word) {

		if( null==word || word.trim().equals(""))
			return null;
		
		int charIndex = 1;
		
		BgramDictionaryNode currentNode = dicMap.get(word.charAt(0));

		while (true) {
			
//			charIndex++;
			if (charIndex >= word.length()) {
				return currentNode;
			}
			
			if (currentNode == null)
				return null;
			
			int compa = (word.charAt(charIndex) - currentNode.getCharOfWord());
			if (compa == 0) {
				currentNode = currentNode.getNextLevelNodes()[currentNode.MIDDLE];
				charIndex++;
			} else if (compa < 0) {
				currentNode = currentNode.getNextLevelNodes()[currentNode.LEFT];
			} else {
				currentNode = currentNode.getNextLevelNodes()[currentNode.RIGHT];
			}
		}
	}

	
	private BgramDictionaryNode getOrCreateNode(Map<Character,BgramDictionaryNode> dicMap,BgramDictionaryNode firstNode,String firstWord,String secondWord, int frequence) throws NullPointerException,
			IllegalArgumentException {
		
		if (firstWord == null) {
			throw new NullPointerException("空指");
		}
		int charIndex = 1;//
		
		if (firstNode == null) {
			firstNode = new BgramDictionaryNode(firstWord.charAt(0));
			dicMap.put(firstWord.charAt(0), firstNode);
		}
		
		BgramDictionaryNode currentNode = firstNode;
		
		while (true) {
			
//			charIndex++;
			if (charIndex >= firstWord.length()) {
				currentNode.setEndWord(true);
				currentNode.setFrequence(frequence);
				Map<String, Integer> map = currentNode.getBgramFrequenceHashMap();
				
				if( null==map )
				{
					map = new HashMap<String, Integer>();
					map.put(secondWord, frequence);
					currentNode.setBgramFrequenceHashMap(map);
				}
				else
				{
					currentNode.getBgramFrequenceHashMap().put(secondWord, frequence);
				}
				
				return currentNode;
			}
			
			int compa = (firstWord.charAt(charIndex) - currentNode.getCharOfWord());
			if (compa == 0) {
				if (currentNode.getNextLevelNodes()[currentNode.MIDDLE] == null) {
					currentNode.getNextLevelNodes()[currentNode.MIDDLE] = new BgramDictionaryNode(firstWord.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.MIDDLE];
				charIndex++;
			} else if (compa < 0) {
				if (currentNode.getNextLevelNodes()[currentNode.LEFT] == null) {
					currentNode.getNextLevelNodes()[currentNode.LEFT] = new BgramDictionaryNode(firstWord.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.LEFT];
			} else {
				if (currentNode.getNextLevelNodes()[currentNode.RIGHT] == null) {
					currentNode.getNextLevelNodes()[currentNode.RIGHT] = new BgramDictionaryNode(firstWord.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.RIGHT];
			}
		}
	}

}
