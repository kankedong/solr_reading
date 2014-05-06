package org.lxd.cniprSeg;





/**
 * 切分结果节点
 * @author Administrator
 *
 */
public class WordByWordSegNode {
	
	private int start;
	private int end;
	private String word;
	private MainDictionaryNode mainDicNode;
	private BgramDictionaryNode bgramDicNode;
	private WordByWordSegNode bestPreNode=null;
	private double accumulationProbability=-100000.0;
	
//	private double accumulationProbability=-100000.0;
	
	public WordByWordSegNode( String word,int start,int end,MainDictionaryNode mainDicNode,BgramDictionaryNode bgramDicNode )
	{
		this.word  = word;
		this.start = start;
		this.end   = end;
		this.bgramDicNode = bgramDicNode;
		this.mainDicNode  = mainDicNode;
//		this.bestPreNode  = bestPreNode;
//		this.accumulationProbability = accumulationProbability;
	}
	
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public MainDictionaryNode getMainDicNode() {
		return mainDicNode;
	}
	public void setMainDicNode(MainDictionaryNode mainDicNode) {
		this.mainDicNode = mainDicNode;
	}
	public BgramDictionaryNode getBgramDicNode() {
		return bgramDicNode;
	}
	public void setBgramDicNode(BgramDictionaryNode bgramDicNode) {
		this.bgramDicNode = bgramDicNode;
	}

	public WordByWordSegNode getBestPreNode() {
		return bestPreNode;
	}

	public void setBestPreNode(WordByWordSegNode bestPreNode) {
		this.bestPreNode = bestPreNode;
	}

	public double getAccumulationProbability() {
		return accumulationProbability;
	}

	public void setAccumulationProbability(double accumulationProbability) {
		this.accumulationProbability = accumulationProbability;
	}
}
