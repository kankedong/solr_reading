package org.lxd.cniprSeg.solr;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.lxd.cniprSeg.lucene.CniprSegTokenizer;

/**
 * 词典构建的是单例模式的
 * @author Administrator
 *
 */
public class CniprSegTokenizerFactory  extends TokenizerFactory implements ResourceLoaderAware{


	static final Logger log = Logger.getLogger(CniprSegTokenizerFactory.class.getName());
	/* 线程内共享 */
	private ThreadLocal<CniprSegTokenizer> tokenizerLocal = new ThreadLocal<CniprSegTokenizer>();

	
	public CniprSegTokenizerFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public Tokenizer create(AttributeFactory factory, Reader input) {
		
		CniprSegTokenizer tokenizer = tokenizerLocal.get();
		if(tokenizer == null) {
			tokenizer = newTokenizer(input);
		} else {
			try {
				tokenizer.setReader(input);
			} catch (IOException e) {
				tokenizer = newTokenizer(input);
				log.info("CniprSegTokenizer.reset i/o error by:"+e.getMessage());
			}
		}

		return tokenizer;
	}
	

	
	
	
	private CniprSegTokenizer newTokenizer(Reader input) {
		
		CniprSegTokenizer tokenizer = new CniprSegTokenizer(input);
		tokenizerLocal.set(tokenizer);
	    return tokenizer;
   	}


	public void inform(ResourceLoader loader) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
