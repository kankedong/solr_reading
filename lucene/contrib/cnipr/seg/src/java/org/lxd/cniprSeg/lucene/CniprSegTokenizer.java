package org.lxd.cniprSeg.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.lxd.cniprSeg.BgramSeg;
import org.lxd.cniprSeg.WordByWordSegNode;


public class CniprSegTokenizer extends Tokenizer{
	
	static final Logger log = Logger.getLogger(CniprSegTokenizer.class.getName());
	
	private BgramSeg seg;
	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	
	public CniprSegTokenizer(Reader input)  {
		super(input);
		termAtt = addAttribute(CharTermAttribute.class);
		offsetAtt = addAttribute(OffsetAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
//		seg = new BgramSeg(input);//会造成第一次切分无结果
//		seg.getSegResult();//会造成第一次切分无结果
		seg = new BgramSeg();
	}
	
	public void reset() throws IOException {
		//lucene 4.0
		//org.apache.lucene.analysis.Tokenizer.setReader(Reader)
		//setReader 自动被调用, input 自动被设置。
		super.reset();
		seg.reset(input);
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		WordByWordSegNode word = seg.next();
		if(word != null) {
			termAtt.copyBuffer(word.getWord().toCharArray(), 0, word.getWord().length());
			offsetAtt.setOffset(word.getStart(), word.getEnd());
			typeAtt.setType("word");
			return true;
		} else {
			end();
			return false;
		}
	}

}
