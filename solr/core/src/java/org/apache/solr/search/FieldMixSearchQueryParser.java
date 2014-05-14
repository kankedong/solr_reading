package org.apache.solr.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.index.Term;

/**
 * 类说明:混合检索的
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-5-7 下午5:02:29
 * 修改时间:2014-5-7 下午5:02:29
 */
public class FieldMixSearchQueryParser  extends SolrQueryParser {
//    private Map<String,Float> queryFields;
    private List<FieldMixSearchQParser.MixField> mixFieldList;
    private static final String FIELD_TAIL="_single";
 
    public FieldMixSearchQueryParser(QParser parser, String defaultField) {
        super(parser, defaultField);
    }
 
    public FieldMixSearchQueryParser(QParser parser, String defaultField,Map<String,Float> queryFields,List<FieldMixSearchQParser.MixField> mixFieldList) {
        super(parser, defaultField);
//        this.queryFields = queryFields;
        this.mixFieldList = mixFieldList;
    }
    
    /**
     * 重载了SolrQueryParserBase中的getFieldQuery方法
     * @see org.apache.solr.parser.SolrQueryParserBase#getFieldQuery(java.lang.String, java.lang.String, boolean)
     * 实现者:lixiaodong@cnir.com
     * 修改者:
     * 实现时间:2014-5-8
     * 修改时间:2014-5-8
     */
    protected Query getFieldQuery(String field, String queryText, boolean quoted) throws SyntaxError {
        
        if (field.charAt(0) == '_' && parser != null) {
        	return super.getFieldQuery(field, queryText, quoted);
        }
        SchemaField sf = schema.getFieldOrNull(field);
        if (sf != null) {
          FieldType ft = sf.getType();
          // delegate to type for everything except tokenized fields
          if (ft.isTokenized() && sf.indexed()) {
        	  
        	  if( !isMixSearchField(field) ){
        		  //不是混合检索字段
        		  return newFieldQuery(getAnalyzer(), field, queryText, quoted || (ft instanceof TextField && ((TextField)ft).getAutoGeneratePhraseQueries()));
        	  }else{
        		  //
        		  BooleanQuery bq = new BooleanQuery();
        		  Query query = newFieldQuery(ft.getQueryAnalyzer(), field, queryText, quoted || (ft instanceof TextField && ((TextField)ft).getAutoGeneratePhraseQueries()));
//        		  Query query = newFieldQuery(getAnalyzer(), field, queryText, quoted || (ft instanceof TextField && ((TextField)ft).getAutoGeneratePhraseQueries()));
        		  if(query!=null) 
        			  bq.add(query, BooleanClause.Occur.SHOULD);
        		  Query extendQuery = getExtendedFieldQuery(this.getExtendedFieldName(field), queryText);
        		  if( extendQuery!=null )
        			  bq.add(extendQuery, BooleanClause.Occur.SHOULD);
        		  return bq;
        	  }
        	  
          } else {
            return sf.getType().getFieldQuery(parser, sf, queryText);
          }
        }

        // default to a normal field query
        return newFieldQuery(getAnalyzer(), field, queryText, quoted);
    }
    
    /**
     * 判断是否为混合检索字段
     * @param fieldName
     * @return
     * 创建者:lixiaodong@cnir.com
     * 修改者:
     * 创建时间:2014-5-8 下午4:52:39
     * 修改时间:2014-5-8 下午4:52:39
     */
    private boolean isMixSearchField( String fieldName ){
    	for( FieldMixSearchQParser.MixField mixField: mixFieldList ){
    		if( mixField.getFieldName().equals(fieldName) )
    			return true;
    	}
    	return false;
    } 
    
    /**
     * 通过一个字段名称或者其单字切分的字段名称
     * @param fieldName
     * 创建者:lixiaodong@cnir.com
     * 修改者:
     * 创建时间:2014-5-8 下午4:55:30
     * 修改时间:2014-5-8 下午4:55:30
     */
    private String getExtendedFieldName( String fieldName ){
    	return fieldName+FIELD_TAIL;
    }
    
    /**
     * 获取扩展检索字段的Query对象
     * @param field
     * @param queryText
     * @return
     * 创建者:lixiaodong@cnir.com
     * 修改者:
     * 创建时间:2014-5-8 下午5:55:54
     * 修改时间:2014-5-8 下午5:55:54
     * @throws SyntaxError 
     */
    private Query getExtendedFieldQuery( String field, String queryText ) throws SyntaxError{
    	
    	String myField = field == null ? this.getDefaultField() : field;
//        Float boots = queryFields.get(field);
 
        if (myField != null){
            FieldType ft = this.schema.getField(myField).getType();
           
            if ((ft instanceof TextField)){
                try{
//                    Analyzer analyzer = ft.getQueryAnalyzer() == null ? ft.getAnalyzer() : ft.getQueryAnalyzer();
                	
                	//注意扩展字段检索的Analyzer是分词的
                	FieldType ftSpecial =  this.schema.getFieldTypeByName("text_cnpir_seg");
                	Analyzer analyzer = ftSpecial.getQueryAnalyzer() == null ? ftSpecial.getAnalyzer() : ftSpecial.getQueryAnalyzer();
                  	
                    if (analyzer != null) {
                        BooleanQuery bq = new BooleanQuery();
                        TokenStream ts = analyzer.tokenStream(field, new StringReader(queryText));
                        ts.reset();//为什么不加就会出错。。。。
                        
                        List<Term> termList = new ArrayList<Term>();
//                        int endOffset = 0;
                        while (ts.incrementToken()) {
                        	
                        	termList.clear();
                        	
                            CharTermAttribute ta = (CharTermAttribute)ts.getAttribute(CharTermAttribute.class);
//                            OffsetAttribute oa = (OffsetAttribute)ts.getAttribute(OffsetAttribute.class);
                            
                            //只有长度大于2的才进行单字的短语检索
                            if( ta.length()>=2 ){
                            	PhraseQuery pq= new PhraseQuery();
                            	for( int i=0 ;i<ta.length();i++ ){
                            		pq.add(new Term(myField,String.valueOf(ta.charAt(i))));
                            		bq.add(new TermQuery(new Term(myField,String.valueOf(ta.charAt(i)))),BooleanClause.Occur.SHOULD);
                            	}
                            	pq.setSlop(0);
                            	bq.add(pq, BooleanClause.Occur.SHOULD);
                            }else if( ta.length()==1 ) {
                            	bq.add(new TermQuery(new Term(myField,String.valueOf(ta))),BooleanClause.Occur.SHOULD);
                            }
                            	
                        }
                        
                        IOUtils.closeWhileHandlingException(ts);//不关闭会出错
                        return bq;
                    }

                }catch (Exception e){
                    throw new SyntaxError(e.getMessage());
                }
            }
        }   
        return null;
    }
}