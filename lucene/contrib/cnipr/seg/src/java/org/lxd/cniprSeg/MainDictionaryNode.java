package org.lxd.cniprSeg;

import java.util.Map;

public class MainDictionaryNode extends DictionaryNode{
	
	private int frequence=0;
	private boolean isEndWord=false;
	private Character charOfWord;
	private MainDictionaryNode[] nextLevelNodes =null ;
	private Map<PosEnum,Integer> posFrequenceHashMap=null;
	
	public MainDictionaryNode( char c )
	{
		charOfWord = c;
		nextLevelNodes = new MainDictionaryNode[3];
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
	
	public MainDictionaryNode[] getNextLevelNodes() {
		return nextLevelNodes;
	}
	public void setNextLevelNodes(MainDictionaryNode[] nextLevelNodes) {
		this.nextLevelNodes = nextLevelNodes;
	}
	public Map<PosEnum, Integer> getPosFrequenceHashMap() {
		return posFrequenceHashMap;
	}
	public void setPosFrequenceHashMap(Map<PosEnum, Integer> posFrequenceHashMap) {
		this.posFrequenceHashMap = posFrequenceHashMap;
	}

}
