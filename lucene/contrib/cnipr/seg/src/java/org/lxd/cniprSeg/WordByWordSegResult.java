package org.lxd.cniprSeg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class WordByWordSegResult {
	
	private static int ListInitSize=16;
	
	private Map<Integer,List<WordByWordSegNode>> startWordListMap = null;
	
	private Map<Integer,List<WordByWordSegNode>> endWordListMap = null;
	
	private List<WordByWordSegNode> allWordNodeList = null;//new ArrayList<WordByWordSegResult>();
	
	private int lastEndIndex=-1;
	
	public WordByWordSegResult()
	{
		this.startWordListMap = new HashMap<Integer,List<WordByWordSegNode>>(64);
		this.endWordListMap   = new HashMap<Integer,List<WordByWordSegNode>>(64);
		this.allWordNodeList = new ArrayList<WordByWordSegNode>(128); 
	}
	
	public void clear()
	{
		this.startWordListMap = new HashMap<Integer,List<WordByWordSegNode>>(64);
		this.endWordListMap   = new HashMap<Integer,List<WordByWordSegNode>>(64);
		this.allWordNodeList = new ArrayList<WordByWordSegNode>(128); 
		this.lastEndIndex=-1;
	}
	
	public void add( WordByWordSegNode result )
	{
		if( result==null )
			return;
		
		List<WordByWordSegNode> startWordList = (List<WordByWordSegNode>)startWordListMap.get(result.getStart());
		List<WordByWordSegNode> endWordList = (List<WordByWordSegNode>)endWordListMap.get(result.getEnd());	
		allWordNodeList.add(result);
		
		if( startWordList==null )
		{
			startWordList = new ArrayList<WordByWordSegNode>(ListInitSize);
			startWordList.add(result);
			startWordListMap.put(result.getStart(), startWordList);
		}
		else
		{
			startWordList.add(result);
			startWordListMap.put(result.getStart(), startWordList);
		}
		
		if( endWordList==null )
		{
			endWordList = new ArrayList<WordByWordSegNode>(ListInitSize);
			endWordList.add(result);
			endWordListMap.put(result.getEnd(), endWordList);
		}
		else
		{
			endWordList.add(result);
			endWordListMap.put(result.getEnd(), endWordList);
		}
		
		if( lastEndIndex<result.getEnd() )
			lastEndIndex = result.getEnd();
			
	}
	
	public List<WordByWordSegNode> getStartWordList( Integer start )
	{
		List<WordByWordSegNode> list = (List<WordByWordSegNode>)startWordListMap.get(start);
		return list;
	}
	
	public List<WordByWordSegNode> getEndWordList( Integer end )
	{
		List<WordByWordSegNode> list = (List<WordByWordSegNode>)endWordListMap.get(end);
		return list;
	}
	
	public List<WordByWordSegNode> getAllWordNodeList()
	{
		return this.allWordNodeList;
	}
	
	public List<WordByWordSegNode> getPreNodeList( WordByWordSegNode currentNode )
	{
		int preNodeEnd = currentNode.getStart();
		return endWordListMap.get(preNodeEnd);
	}
	
	
	public List<WordByWordSegNode> getBestSegTagging( List<WordByWordSegNode> resultList )
	{
		
		
		for( int i=resultList.size()-1;i>=0;i-- )
		{
			//System.out.println();
			WordByWordSegNode segNode = resultList.get(i);
			//System.out.println( segNode.getWord() );
			Iterator<PosEnum> iter = segNode.getMainDicNode().getPosFrequenceHashMap().keySet().iterator();
			while( iter.hasNext() )
			{
				PosEnum posEnum = (PosEnum)iter.next();
				//System.out.print( "["+posEnum+":"+segNode.getMainDicNode().getPosFrequenceHashMap().get(posEnum)+"]" );
			}
			
		}
				
		return null;
	}
	public List<WordByWordSegNode> getBestSeg( long totalFrequence )
	{
		if( startWordListMap.size()==0 )
			return null;
		
		List<WordByWordSegNode> startList = startWordListMap.get(0);
//		recursion(startList,wbwSegResult,"");
		
		List<WordByWordSegNode> segNodeList = allWordNodeList;
		
		for( WordByWordSegNode currentNode:segNodeList )
		{
			//System.out.println("*******************************************************************************************************");
			//System.out.println( "[currentWord:" + currentNode.getWord() + " |start:" + currentNode.getStart() + "|end:" + currentNode.getEnd()+ "|frequence:"+currentNode.getMainDicNode().getFrequence()  + "]" );
			List<WordByWordSegNode> preNodeList = getPreNodeList(currentNode);
			
			WordByWordSegNode bestPreNode = null;
			double accumulationProbability = Double.NEGATIVE_INFINITY;
			
			if( preNodeList!=null )
			{
				for( WordByWordSegNode preNode: preNodeList )
				{
					
					
					//System.out.println( "[preWord:" + preNode.getWord() + "|start:" + preNode.getStart() + "|end:" + preNode.getEnd() + "|frequence:" + preNode.getMainDicNode().getFrequence() + "|transitionProbability:" + Utils.transitionProbability( preNode,currentNode,totalFrequence ) +"|preNode.getAccumulationProbability():"+preNode.getAccumulationProbability()+"]"  );
					double currentProbability = Math.log(Utils.transitionProbability(preNode, currentNode,totalFrequence))+ preNode.getAccumulationProbability();
//					double currentProbability = transitionProbability(preNode, currentNode,totalFrequence)+ preNode.getAccumulationProbability();
//					BigDecimal currentProbabilityBig = new BigDecimal(currentProbability);
//					BigDecimal accumulationProbabilityBig = new BigDecimal(accumulationProbability);
//					currentProbabilityBig.compareTo(accumulationProbabilityBig);
					
					if ( currentProbability>accumulationProbability ) {
						accumulationProbability = currentProbability;
						bestPreNode = preNode;
					}
					
				}
				
				currentNode.setAccumulationProbability(accumulationProbability);
				currentNode.setBestPreNode(bestPreNode);
				//System.out.println( "[bestPreNode:" + bestPreNode.getWord()+"|currentWord:"+currentNode.getWord()+"|accumulationProbability:"+accumulationProbability+"]"  );
			}

		}
		
		//System.out.println("*******************************************************************************************************");
		List<WordByWordSegNode> lastEndIndexList = this.endWordListMap.get( lastEndIndex );
		
		WordByWordSegNode bestLastSegNode = null;
		double accumulationProbability = Double.NEGATIVE_INFINITY;
		
		for( WordByWordSegNode segNode : lastEndIndexList)
		{
			if( accumulationProbability<segNode.getAccumulationProbability() )
			{
				bestLastSegNode = segNode;
				accumulationProbability = segNode.getAccumulationProbability();
			}
		}
		
		WordByWordSegNode bestCurrentSegNode = bestLastSegNode;
		
		
		List<WordByWordSegNode> resultList = new  ArrayList<WordByWordSegNode>();
		resultList.add(bestLastSegNode);
		
		
		
//		while( bestCurrentSegNode.getStart()>0 )
		while( bestCurrentSegNode!=null )
		{
			WordByWordSegNode preSegNode = bestCurrentSegNode.getBestPreNode();
			
			if( preSegNode!=null )
				resultList.add(preSegNode);
			
			bestCurrentSegNode = preSegNode;
		}
		
//		resultList.add(bestCurrentSegNode);
		
		return resultList;
	}
	
	
	 
	
	
}
