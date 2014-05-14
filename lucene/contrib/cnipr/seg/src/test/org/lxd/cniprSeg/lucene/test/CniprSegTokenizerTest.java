package org.lxd.cniprSeg.lucene.test;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Token;
import org.junit.Test;
import org.lxd.cniprSeg.lucene.CniprSegTokenizer;


/**
 * 类说明:
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-3-6 上午9:32:43
 * 修改时间:2014-3-6 上午9:32:43
 */
public class CniprSegTokenizerTest {
	
	@Test
	public void test() throws IOException
	{
		String txt = "我是中国人民子弟兵";
		txt="小麦根秸清除深埋机";
		CniprSegTokenizer tokenizer = new CniprSegTokenizer(  new StringReader(txt) );
		
		for(Token t= new Token(); (t=TokenUtils.nextToken(tokenizer, t)) !=null;) {
			
//			t.
			
			System.out.println(t+"|"+t.startOffset()+"|"+t.endOffset()+"|"+t.type()+"|"+t.length());
		}
		
	}
	
}
