package use.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.junit.Test;

/** 
 * @author 三劫散仙 
 * @version 1.0 
 *  
 * 自定义收集器 
 * 实现评分收集 
 * **/  
public class MyScoreCollectorTest {  
  
  /**
   * 
   */
  @Test
  public void test(){
//  //自定义收集器  
//    MyScoreCollector  scoreCollector=new MyScoreCollector();  
//   searcher.search(new MatchAllDocsQuery(), scoreCollector);  
//   /** 
//    * 自定义的收集类，实现效果===>ScoreDocs类 
//    * **/  
//   List<ScoreDoc> s=scoreCollector.docs;  
//   for(ScoreDoc sc:s){  
//    System.out.println(sc.doc+"===="+sc.score);  
//   }  
  }
  
}  
