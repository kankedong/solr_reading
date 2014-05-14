package org.apache.solr.search;

import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.SolrPluginUtils;
import org.apache.solr.parser.QueryParser;;

/**
 * 类说明:
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-5-7 下午4:46:01
 * 修改时间:2014-5-7 下午4:46:01
 */
public class FieldMixSearchQParser  extends QParser{
    
	private SolrQueryParser lparser;
    private SolrParams solrParams;
    private Map<String,Float> queryFields;
    private List<FieldMixSearchQParser.MixField> mixFieldList;
    
    /**
     * 
     * @param qstr
     * @param localParams
     * @param params
     * @param req
     * @param mixFieldList
     * 创建者:lixiaodong@cnir.com
     * 修改者:
     * 创建时间:2014-5-8 下午4:04:02
     * 修改时间:2014-5-8 下午4:04:02
     */
    public FieldMixSearchQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req,List<FieldMixSearchQParser.MixField> mixFieldList){
        super(qstr, localParams, params, req);
        this.mixFieldList = mixFieldList;
    }
    
    /**
     * 解析并返回解析后的Query对象
     * @see org.apache.solr.search.QParser#parse()
     * 实现者:lixiaodong@cnir.com
     * 修改者:
     * 实现时间:2014-5-8
     * 修改时间:2014-5-8
     */
    public Query parse() throws SyntaxError {
        SolrParams localParams = getLocalParams();
        SolrParams params = getParams();
        solrParams = SolrParams.wrapDefaults(localParams, params);
        queryFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.QF));
        if (0 == queryFields.size()) {
            queryFields.put(req.getSchema().getDefaultSearchFieldName(), 1.0f);
        }
 
        String qstr = getString();
        String defaultField = getParam("df");
        if (defaultField == null) {
            defaultField = getReq().getSchema().getDefaultSearchFieldName();
        }
        this.lparser = new FieldMixSearchQueryParser(this, defaultField,queryFields,mixFieldList);//指定lparser
 
        String opParam = getParam("q.op");
        if (opParam != null) {
            this.lparser.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
        }else {
//            QueryParser.Operator operator = getReq().getSchema().getQueryParserDefaultOperator();
        	QueryParser.Operator operator = QueryParser.Operator.OR;//暂时将默认的运算符，设置为OR
            this.lparser.setDefaultOperator(operator == null ? QueryParser.Operator.OR : operator);
        }
 
        return this.lparser.parse(qstr);
    }
    
    /**
     * 参考LuceneQParser实现
     * @see org.apache.solr.search.QParser#getDefaultHighlightFields()
     * 实现者:lixiaodong@cnir.com
     * 修改者:
     * 实现时间:2014-5-8
     * 修改时间:2014-5-8
     */
    public String[] getDefaultHighlightFields(){
    	return lparser == null ? new String[]{} : new String[]{lparser.getDefaultField()};
    }
    
    /**
     * 未来可能扩展所以用一个类来封装需要混合检索的字段
     * @author Administrator
     *
     */
    public static class MixField{//注意在静态方法中，调用这个内部类，不加static会报错
    	
    	
    	public static final String MIX_FIELDS = "mixFields";
    	public static final String MIX_FIELD = "mixField";
    	
    	private String fieldName;
    	private int level;//暂时未使用，为扩展预留
    	
    	public MixField(){
    		}
    	
    	public MixField(String fieldName,int level){
    		this.fieldName = fieldName;
    		this.level = level;
    	}

		public String getFieldName() {
			return fieldName;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}
    }
}