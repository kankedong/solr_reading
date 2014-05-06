package org.lxd.cniprSeg.lucene.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.junit.Test;
import org.lxd.cniprSeg.solr.CniprSegTokenizerFactory;

/**
 * 类说明:
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-3-11 下午5:54:40
 * 修改时间:2014-3-11 下午5:54:40
 */
public class CniprSegTokenizerFactoryTest {
	
	
	@Test
	public void test() throws IOException{
		
		Map<String, String> maps = new HashMap<String, String>();
		CniprSegTokenizerFactory factory = new CniprSegTokenizerFactory(maps);
		String txt = "我是中国人民子弟兵";
		
		
		Tokenizer tokenizer = factory.create(null, new StringReader(txt));
		
	
		
	}
	
}
