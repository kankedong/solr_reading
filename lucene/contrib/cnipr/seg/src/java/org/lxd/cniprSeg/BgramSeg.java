package org.lxd.cniprSeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.lxd.cniprSeg.lucene.CniprSegTokenizer;


/**
 * 
 * @author Administrator
 *
 */
public class BgramSeg {
	
	static final Logger log = Logger.getLogger(CniprSegTokenizer.class.getName());
	
	private MainDictionary mainDic;
	private BgramDictionary bgramDic;
	private TagDictionary tagDic;
	private boolean isOutputPunctuation; //是否输出标点符号
	private Queue<WordByWordSegNode> segResult;//切分结果 
	private StringBuilder sentences = new StringBuilder(1024*10);//待切分语句
	
	/**
	 * 2-gram分词器构造函数
	 * @throws IOException
	 */
	public BgramSeg() 
	{

		try {
			//单例获取各个词典
			mainDic = MainDictionaryFactory.getMainDictionary();
			bgramDic = BgramDictionaryFactory.getBgramDictionary();
			tagDic = TagDictionaryFactory.getTagDictionary();
		} catch (IOException e) {
			log.info("create dic error:"+e);
		} 
		
		isOutputPunctuation = false;
		segResult = new LinkedList<WordByWordSegNode>();
		sentences.setLength(0);
	}
	
	/**
	 * 2-gram分词器构造函数
	 * @throws IOException
	 */
	public BgramSeg( String sentence ) 
	{

		try {
			//单例获取各个词典
			mainDic = MainDictionaryFactory.getMainDictionary();
			bgramDic = BgramDictionaryFactory.getBgramDictionary();
			tagDic = TagDictionaryFactory.getTagDictionary();
		} catch (IOException e) {
			log.info("create dic error:"+e);
		} 
		
		isOutputPunctuation = false;
		segResult = new LinkedList<WordByWordSegNode>();
		sentences.setLength(0);
		sentences.append(sentence);
//		getSegResult();
	}
	
	/**
	 * 2-gram分词器构造函数
	 * @throws IOException
	 */
	public BgramSeg( Reader input ) 
	{
		try {
			//单例获取各个词典
			mainDic = MainDictionaryFactory.getMainDictionary();
			bgramDic = BgramDictionaryFactory.getBgramDictionary();
			tagDic = TagDictionaryFactory.getTagDictionary();
		} catch (IOException e) {
			log.info("create dic error:"+e);
		} 
		
		isOutputPunctuation = false;
		segResult = new LinkedList<WordByWordSegNode>();
		sentences.setLength(0);
		readDataFromInput(input);
//		getSegResult();
	}
	
	/**
	 * 清空缓存
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-1 上午9:51:37
	 * 修改时间:2014-3-1 上午9:51:37
	 */
	private void clearBuffer()
	{
		sentences.setLength(0);
	}
	
	
	/**
	 * 从字符流中读取数据到缓存中
	 * @param input
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-1 上午9:50:17
	 * 修改时间:2014-3-1 上午9:50:17
	 */
	private void readDataFromInput( Reader input ){
		
		BufferedReader br = new BufferedReader( input );
		String line = null;
		try {
			
			while( (line=br.readLine())!=null ){
				sentences.append(line);
			}
			
		} catch (IOException e) {
			log.info("exception when reading input to seg,e is "+e);
		}finally{
//			try {
//				br.close();//不能关闭吗？不应该在此处关闭
//			} catch (IOException e) {
//				log.info("exception when close input,e is "+e);
//			}
		}
	}
	
	/**
	 * 清空
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午5:15:35
	 * 修改时间:2014-2-26 下午5:15:35
	 */
	public void reset( Reader input )
	{
		segResult.clear();
		clearBuffer();
		readDataFromInput(input);
		getSegResult();
	}
	
	/**
	 * 读取下一个分词结果
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-1 上午10:07:10
	 * 修改时间:2014-3-1 上午10:07:10
	 */
	public WordByWordSegNode next()
	{
		return segResult.poll();
	}
	
	/**
	 * 获取切分结果的
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-3-10 下午4:06:46
	 * 修改时间:2014-3-10 下午4:06:46
	 */
	public void getSegResult() 
	{
		List<List<WordByWordSegNode>> resultList= new ArrayList<List<WordByWordSegNode>>();
		
		String sentence = sentences.toString();
		
		if( sentence==null || sentence.trim().equals("") )
			return ;
		
		WordByWordSegResult wbwSegResult = new WordByWordSegResult();
		
		int start = 0;
		boolean isSegFinished = false;
		while( start<(sentence.length()) && !isSegFinished )
		{
			int end=start+1;
			while( end<=sentence.length() )
			{
				String word = sentence.substring(start,end);
				
				if( isAllCharacterDigit(word) ){
					List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
					if( list!=null ){	
						resultList.add(list);
						wbwSegResult.clear();
					}	
					
					end = nextFirstNotDigitCharPosition(sentence,start);
					list = new ArrayList<WordByWordSegNode>();
					word = sentence.substring(start, end);
					WordByWordSegNode segNode = new WordByWordSegNode( word,start,end,null,null );
					list.add(segNode);
					resultList.add(list);
					start = end-1;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					break;
				}else if( isAllCharacterLetter(word)  ){
					List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
					if( list!=null ){	
						resultList.add(list);
						wbwSegResult.clear();
					}	
					
					wbwSegResult.clear();
					end = nextFirstNotLetterCharPosition(sentence,start);
					list = new ArrayList<WordByWordSegNode>();
					word = sentence.substring(start, end);
					WordByWordSegNode segNode = new WordByWordSegNode( word,start,end,null,null );
					list.add(segNode);
					resultList.add(list);
					start = end-1;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					break;
				}else if( isEndOfSentence(word) ){
					List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
					if( list!=null ){	
						resultList.add(list);
						wbwSegResult.clear();
					}	
					
					wbwSegResult.clear();
					end = nextFirstNotEndCharPosition(sentence,start);
//					list = new ArrayList<WordByWordSegNode>();
//					word = sentence.substring(start, end);
//					WordByWordSegNode segNode = new WordByWordSegNode( word,start,end,null,null );
//					list.add(segNode);
//					resultList.add(list);
					start = end;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					break;
				}
				
				
				MainDictionaryNode mainDicnode = mainDic.getNode(word);
				if( mainDicnode==null ){
					break;
				}
				else if( !mainDicnode.isEndWord()){//待优化
					end++;
					if( start==sentence.length()-1 ){
						isSegFinished=true;
					}else{
						//不存在单一单字，存在以这个单字开头的词的情况，需要进一步判断待切分语句的以这个单字开头的另一个词是否在词典中存在，如果不存在，要把这个字
						//切分出来，如“秸清”的“秸”字
						if( end <=sentence.length()-1 ){
							String nextWord = sentence.substring(start,end);
							
							if( mainDic.getNode(nextWord)==null ){
								//单词的下一个词也不存在，那么把这个单字加入到分词结果中，或者作为分词边界
								WordByWordSegNode result = new WordByWordSegNode( word,start,end-1,mainDicnode,null );
								wbwSegResult.add(result);
							}
						}
						
						
					} 
					continue;
				}
				else{
					if( word.length()==1 && (mainDicnode.getPosFrequenceHashMap().get(PosEnum.w)!=null) ){
						List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
						if( list!=null ){
							resultList.add(list);
							wbwSegResult.clear();
						}
						
						if( isOutputPunctuation ){
							list = new ArrayList<WordByWordSegNode>();
							WordByWordSegNode node = new WordByWordSegNode(word,start,end,null,null);
							list.add(node);
							resultList.add(list);
						}
						
						start = end-1;
						if( start==sentence.length()-1 ) 
							isSegFinished=true;
						break;
					}else {
						BgramDictionaryNode bgramDicNode =  bgramDic.getNode(word);
						WordByWordSegNode result = new WordByWordSegNode( word,start,end,mainDicnode,bgramDicNode );
						wbwSegResult.add(result);
						end++;
						if( start==sentence.length()-1 ) 
							isSegFinished=true;
					}
					
				}
				
			}
			start++;
		}

		List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);		
		if( list!=null ){
			resultList.add(list);
		}
		
		for( List<WordByWordSegNode> sentenceSegList:resultList ){
//			for( WordByWordSegNode segNode:sentenceSegList ){
//				segResult.add(segNode);
//			}

			for( int i=sentenceSegList.size()-1;i>=0;i-- ){
				segResult.add(sentenceSegList.get(i));
			}
		}
		
//		return resultList;
	}
	
	/**
	 * 获取带词性标记的分词结果
	 * @param sentence
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:37:34
	 * 修改时间:2014-2-26 下午1:37:34
	 */
	public List<List<WordByWordSegNode>> getTagResult( String sentence )
	{
		List<List<WordByWordSegNode>> resultList= new ArrayList<List<WordByWordSegNode>>();
		
		if( sentence==null || sentence.trim().equals("") )
			return null;
		
		WordByWordSegResult wbwSegResult = new WordByWordSegResult();
		
		int start = 0;
		boolean isSegFinished = false;
		while( start<(sentence.length()) && !isSegFinished )
		{
			int end=start+1;
			while( end<=sentence.length() )
			{
				String word = sentence.substring(start,end);
				
				if( isAllCharacterDigit(word) )
				{
					List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
					if( list!=null )
					{	
						resultList.add(list);
						wbwSegResult.clear();
					}	
					
					end = nextFirstNotDigitCharPosition(sentence,start);
					list = new ArrayList<WordByWordSegNode>();
					word = sentence.substring(start, end);
					WordByWordSegNode segNode = new WordByWordSegNode( word,start,end,null,null );
					list.add(segNode);
					resultList.add(list);
					start = end-1;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					break;
				}
				else if( isAllCharacterLetter(word)  )
				{
					List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
					if( list!=null )
					{	
						resultList.add(list);
						wbwSegResult.clear();
					}	
					
					wbwSegResult.clear();
					end = nextFirstNotLetterCharPosition(sentence,start);
					list = new ArrayList<WordByWordSegNode>();
					word = sentence.substring(start, end);
					WordByWordSegNode segNode = new WordByWordSegNode( word,start,end,null,null );
					list.add(segNode);
					resultList.add(list);
					start = end-1;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					break;
				}
				
				
				MainDictionaryNode mainDicnode = mainDic.getNode(word);
				if( mainDicnode==null )
				{
					break;
				}
				else if( !mainDicnode.isEndWord())//待优�?
				{
					
					end++;
					if( start==sentence.length()-1 ) 
						isSegFinished=true;
					continue;
				}
				else
				{
					if( word.length()==1 && (mainDicnode.getPosFrequenceHashMap().get(PosEnum.w)!=null) )
					{
						List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);	
						if( list!=null )
						{
							resultList.add(list);
							wbwSegResult.clear();
						}
						
						if( isOutputPunctuation )
						{
							list = new ArrayList<WordByWordSegNode>();
							WordByWordSegNode node = new WordByWordSegNode(word,start,end,null,null);
							list.add(node);
							resultList.add(list);
							
						}
						
						start = end-1;
						if( start==sentence.length()-1 ) 
							isSegFinished=true;
						break;
					}
					else
					{
						BgramDictionaryNode bgramDicNode =  bgramDic.getNode(word);
						WordByWordSegNode result = new WordByWordSegNode( word,start,end,mainDicnode,bgramDicNode );
						wbwSegResult.add(result);
						end++;
						if( start==sentence.length()-1 ) 
							isSegFinished=true;
					}
					
				}
				
			}
			start++;
		}

		List<WordByWordSegNode> list = wbwSegResult.getBestSeg(mainDic.totalFrequence);		
		if( list!=null )
		{
			resultList.add(list);
		}
		
		
		
		return resultList;
	}
	
	
	public List<WordByWordSegNode>  getBestSegTagging( List<WordByWordSegNode> resultList )
	{
		
		
		for( int i=resultList.size()-1;i>=0;i-- )
		{
			//System.out.println();
			WordByWordSegNode segNode = resultList.get(i);
//			System.out.println( segNode.getWord() );
			
			if( segNode.getMainDicNode()==null )
			{
				if( this.isAllCharacterDigit(segNode.getWord()) )
				{
//					System.out.println( "["+segNode.getWord()+"://m]" );
				}
				else if( this.isAllCharacterLetter(segNode.getWord()) )
				{
//					System.out.println( "["+segNode.getWord()+"://char]" );
				}
				else
				{
//					System.out.println( "["+segNode.getWord()+"://unknown]" );
				}
				
					
				continue;
			}
			
			if( segNode.getMainDicNode().getPosFrequenceHashMap()==null )
			{
				System.out.println( "["+segNode.getWord()+"://unknown]" );
				continue;
			}
			
//			Iterator<PosEnum> iter = segNode.getMainDicNode().getPosFrequenceHashMap().keySet().iterator();
//			while( iter.hasNext() )
//			{
//				PosEnum posEnum = (PosEnum)iter.next();
//				Map<String,Integer> map = tagDic.getAllPartOfSpeechOfNextWord(posEnum.toString());
//				System.out.println( "["+segNode.getWord()+":"+posEnum+":"+segNode.getMainDicNode().getPosFrequenceHashMap().get(posEnum)+"]" );
//				
//				if( map!=null )
//				{
//					System.out.println("");
//					Iterator<String> keyIter = map.keySet().iterator();
//					while(keyIter.hasNext())
//					{
//						String key = keyIter.next();
//						Integer value = map.get(key);
//						System.out.print("["+key+":"+value+"]");
//					}
//				}
//			}
			
		}
				
		return null;
	}
	
	/**
	 * 获取具体sentence从start位置后第一个非数字的位置
	 * @param sentence
	 * @param start
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:38:39
	 * 修改时间:2014-2-26 下午1:38:39
	 */
	private int nextFirstNotDigitCharPosition( String sentence,int start)
	{
		int index = start;
		while( Character.isDigit(sentence.charAt(index)) )
		{
			if( index==sentence.length()-1 )
			{
				index = sentence.length();
				break;
			}
			else
			{
				index++;
			}
		}
		return index;
	}
	
	/**
	 * 获取具体sentence从start位置后第一个非字母的位置
	 * @param sentence
	 * @param start
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:40:23
	 * 修改时间:2014-2-26 下午1:40:23
	 */
	private int nextFirstNotLetterCharPosition( String sentence,int start)
	{
		int index = start;
		while( Character.isLowerCase(sentence.charAt(index))||Character.isUpperCase(sentence.charAt(index)) )
		{
			if( index==sentence.length()-1 )
			{
				index = sentence.length();
				break;
			}
			else
			{
				index++;
			}
		}
		return index;
	}
	
	
	/**
	 * 获取具体sentence从start位置后第一个非字母的位置
	 * @param sentence
	 * @param start
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:40:23
	 * 修改时间:2014-2-26 下午1:40:23
	 */
	private int nextFirstNotEndCharPosition( String sentence,int start)
	{
		int index = start;
		while( sentence.charAt(index)!=' ' )
		{
			if( index==sentence.length()-1 )
			{
				index = sentence.length();
				break;
			}
			else
			{
				index++;
			}
		}
		return index;
	}
	
	
	/**
	 * 判断str是否全部都是数字
	 * @param str
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:44:13
	 * 修改时间:2014-2-26 下午1:44:13
	 */
	private boolean isAllCharacterDigit(String str)
	{
		if( str==null || str.trim().equals("") )
		{
			return false;
		}
		else
		{
			for( Character c: str.toCharArray() )
			{
				if( !Character.isDigit(c) )
					return false;
			}
			return true;
		}
	}
	
	/**
	 * 判断str是否都是字母
	 * @param str
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:45:09
	 * 修改时间:2014-2-26 下午1:45:09
	 */
	private boolean isAllCharacterLetter(String str)
	{
		if( str==null || str.trim().equals("") ){
			return false;
		}else{
			for( Character c: str.toCharArray() ){
				if( !Character.isUpperCase(c)&&!Character.isLowerCase(c) )
					return false;
			}
			return true;
		}
	}
	
	
	/**
	 * 判断str是否全部都是数字
	 * @param str
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:44:13
	 * 修改时间:2014-2-26 下午1:44:13
	 */
	private boolean isEndOfSentence(String str)
	{
		if( str==null  ){
			return false;
		}else{
			if( str.equals(" ") ){
				return true;
			}else {
				return false;
			}
		}
	}

	/**
	 * 是否输出标点符号
	 * @return
	 * 创建者:lixiaodong@cnir.com
	 * 修改者:
	 * 创建时间:2014-2-26 下午1:46:27
	 * 修改时间:2014-2-26 下午1:46:27
	 */
	public boolean isOutputPunctuation() {
		return isOutputPunctuation;
	}

	
	public void setOutputPunctuation(boolean isOutputPunctuation) {
		this.isOutputPunctuation = isOutputPunctuation;
	}
	
	
}
