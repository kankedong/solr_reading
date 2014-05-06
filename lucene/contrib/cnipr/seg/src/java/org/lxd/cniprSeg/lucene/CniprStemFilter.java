package org.lxd.cniprSeg.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.en.PorterStemmer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * 类说明:
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-4-24 上午11:11:32
 * 修改时间:2014-4-24 上午11:11:32
 */
public final class CniprStemFilter extends TokenFilter {
//	  private final PorterStemmer stemmer = new PorterStemmer();
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);

	  public CniprStemFilter(TokenStream in) {
	    super(in);
	  }

	  @Override
	  public final boolean incrementToken() throws IOException {
//	    if (!input.incrementToken())
//	      return false;
//
//	    if ((!keywordAttr.isKeyword()) && stemmer.stem(termAtt.buffer(), 0, termAtt.length()))
//	      termAtt.copyBuffer(stemmer.getResultBuffer(), 0, stemmer.getResultLength());
//	    return true;
		return false;
	  }
	}
