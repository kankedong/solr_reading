package org.lxd.cniprSeg;

import java.util.HashMap;
import java.util.Map;

public class TagDictionaryNode extends DictionaryNode{
	
	private int frequence;
	private String posStr;
	private PosEnum posEnum;
	private Map<String,Integer> tagFrequenceHashMap=null;
	
	
	public TagDictionaryNode( String posStr )
	{
		this.posStr = posStr;
		this.tagFrequenceHashMap = new HashMap<String,Integer>();
	}
	
	public int getFrequence() {
		return frequence;
	}
	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}
	

	public Map<String, Integer> getTagTransnHashMap() {
		return tagFrequenceHashMap;
	}
	public void setTagFrequenceHashMap(Map<String, Integer> tagFrequenceHashMap) {
		this.tagFrequenceHashMap = tagFrequenceHashMap;
	}
		                                                                  
}
