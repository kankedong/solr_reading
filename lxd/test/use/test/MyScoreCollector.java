package use.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

/** 
 * @author 三劫散仙 
 * @version 1.0 
 *  
 * 自定义收集器 
 * 实现评分收集 
 * **/  
public class MyScoreCollector extends Collector {  
    //private HashMap<String, String> documents=new HashMap<String, String>();  
    List<ScoreDoc> docs=new ArrayList<ScoreDoc>();  
    private Scorer scorer;//scorer类  
    private int docBase;//全局相对段基数  
       
  
    @Override  
    public boolean acceptsDocsOutOfOrder() {  
        // TODO Auto-generated method stub  
        //返回true是允许无次序的ID  
        //返回false必须是有次序的  
        return true;  
    }  
  
    @Override  
    public void collect(int arg0) throws IOException {  
        /** 
         * 匹配上一个文档 
         * 就记录其docid与打分情况 
         *  
         * */  
        docs.add(new ScoreDoc(arg0+docBase,scorer.score()));//  
    }  
//  BinaryDocValues names;//字符类型的内置存储  
//  BinaryDocValues bookNames;//字符类型的内置存储  
//  BinaryDocValues ids;//字符类型的内置存储  
//  BinaryDocValues prices;//字符类型的内置存储  
//  FieldCache.Doubles d ; //数值类型的内置存储  
//  FieldCache.Ints ints;//数值类型的内置存储  
    @Override  
    public void setNextReader(AtomicReaderContext arg0) throws IOException {  
        this.docBase=arg0.docBase;//记录每个索引段结构的相对位置  
    }  
  
    @Override  
    public void setScorer(Scorer arg0) throws IOException {  
        // TODO Auto-generated method stub  
        this.scorer=arg0;//记录改匹配的打分情况  
          
    }  
      
      
      
  
}  
