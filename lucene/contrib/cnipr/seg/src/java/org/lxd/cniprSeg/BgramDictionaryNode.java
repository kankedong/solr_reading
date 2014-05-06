package org.lxd.cniprSeg;

import java.util.Map;

public class BgramDictionaryNode extends DictionaryNode{
	
	private int frequence;
	private boolean isEndWord=false;
	private Character charOfWord;
	private BgramDictionaryNode[] nextLevelNodes =null ;
	private Map<String,Integer> bgramFrequenceHashMap=null;
	
	
	public BgramDictionaryNode( char c )
	{
		charOfWord = c;
		nextLevelNodes = new BgramDictionaryNode[3];
	}
	
	public int getFrequence() {
		return frequence;
	}
	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}
	public boolean isEndWord() {
		return isEndWord;
	}
	public void setEndWord(boolean isEndWord) {
		this.isEndWord = isEndWord;
	}
	public Character getCharOfWord() {
		return charOfWord;
	}
	
	public void setCharOfWord(Character charOfWord) {
		this.charOfWord = charOfWord;
	}
	
	public BgramDictionaryNode[] getNextLevelNodes() {
		return nextLevelNodes;
	}

	public void setNextLevelNodes(BgramDictionaryNode[] nextLevelNodes) {
		this.nextLevelNodes = nextLevelNodes;
	}

	public Map<String, Integer> getBgramFrequenceHashMap() {
		return bgramFrequenceHashMap;
	}
	public void setBgramFrequenceHashMap(Map<String, Integer> bgramFrequenceHashMap) {
		this.bgramFrequenceHashMap = bgramFrequenceHashMap;
	}
		                                                                  
}
