package org.lxd.cniprSeg;

import java.util.HashMap;
import java.util.Map;

public class MainDictionary {
	
	public static long totalFrequence = 0L;
	
	private Map<Character,MainDictionaryNode>  dicMap = null;
	
	public Map<Character, MainDictionaryNode> getDicMap() {
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
	public void createDictionaryNode( String word,PosEnum posEnum,int frequece,String pos,int posFrequece )
	{		
		if( word==null || word.trim().equals("") )
			return;

		
		if( dicMap==null )
		{
			dicMap = new HashMap<Character,MainDictionaryNode>(2048);
		}
		
		Character firstChar = word.charAt(0);
		MainDictionaryNode firstNode = (MainDictionaryNode)dicMap.get(firstChar);
		getOrCreateNode(dicMap,word,firstNode, posEnum, frequece );
	}
	

	/**
	 * 
	 * @param word
	 * @return
	 */
	public MainDictionaryNode getNode(String word) {

		if( null==word || word.trim().equals(""))
			return null;
		
		int charIndex = 1;
		
		MainDictionaryNode currentNode = dicMap.get(word.charAt(0));

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

	
	private MainDictionaryNode getOrCreateNode(Map<Character,MainDictionaryNode> dicMap,String word,MainDictionaryNode firstNode,PosEnum posEnum,int frequence) throws NullPointerException,
			IllegalArgumentException {
		if (word == null) {
			throw new NullPointerException("空指");
		}
		int charIndex = 1;//
		
		if (firstNode == null) {
			firstNode = new MainDictionaryNode(word.charAt(0));
			dicMap.put(word.charAt(0), firstNode);
		}
		
		MainDictionaryNode currentNode = firstNode;
		
		while (true) {
		
			if (charIndex >= word.length()) {
				currentNode.setEndWord(true);
				currentNode.setFrequence(currentNode.getFrequence()+frequence);
				totalFrequence+=frequence;
				if(null==currentNode.getPosFrequenceHashMap())
				{
					Map<PosEnum, Integer> map = new HashMap<PosEnum, Integer>();
					map.put(posEnum, frequence);
					currentNode.setPosFrequenceHashMap(map);
				}
				else
				{
					currentNode.getPosFrequenceHashMap().put(posEnum, frequence);
				}
				
				return currentNode;
			}
			
			int compa = (word.charAt(charIndex) - currentNode.getCharOfWord());
			if (compa == 0) {
				if (currentNode.getNextLevelNodes()[currentNode.MIDDLE] == null) {
					currentNode.getNextLevelNodes()[currentNode.MIDDLE] = new MainDictionaryNode(word.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.MIDDLE];
				charIndex++;
			} else if (compa < 0) {
				if (currentNode.getNextLevelNodes()[currentNode.LEFT] == null) {
					currentNode.getNextLevelNodes()[currentNode.LEFT] = new MainDictionaryNode(word.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.LEFT];
			} else {
				if (currentNode.getNextLevelNodes()[currentNode.RIGHT] == null) {
					currentNode.getNextLevelNodes()[currentNode.RIGHT] = new MainDictionaryNode(word.charAt(charIndex));
				}
				currentNode = currentNode.getNextLevelNodes()[currentNode.RIGHT];
			}
		}
	}

}
