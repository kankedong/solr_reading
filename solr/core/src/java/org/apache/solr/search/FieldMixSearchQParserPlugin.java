package org.apache.solr.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.FieldMixSearchQParser.MixField;

/**
 * 类说明:混合字段检索解析器插件
 * 创建者:lixiaodong@cnir.com
 * 修改者:
 * 创建时间:2014-5-7 下午4:44:08
 * 修改时间:2014-5-7 下午4:44:08
 */
public class FieldMixSearchQParserPlugin extends QParserPlugin {
	
	public static final String NAME="fieldMixSearchQParser";
	private List<FieldMixSearchQParser.MixField> mixFieldList;
	
    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new FieldMixSearchQParser(qstr, localParams, params, req,mixFieldList);
    }
 
    public void init(NamedList args) {
    	NamedList list = (NamedList)args.get(MixField.MIX_FIELDS);
    	mixFieldList = convertTo(list);
    }
    
    private List<FieldMixSearchQParser.MixField> convertTo( NamedList list ){
    	
    	if( list!=null && list.size()>1 ){
    		List<FieldMixSearchQParser.MixField> resutl = new ArrayList<FieldMixSearchQParser.MixField>();
    		for( int i=0;i<list.size();i++ ){
    			
    			NamedList object = (NamedList)list.getVal(i);
    			
    			String fieldName =(String)(object.get("fieldName"));
    			Integer level =(Integer)(object.get("level"));
    			
//    			String fieldName =(String)((NamedList)object.get(MixField.MIX_FIELD)).get("fieldName");
//    			Integer level =(Integer)((NamedList)object.get(MixField.MIX_FIELD)).get("level");
    			
    			FieldMixSearchQParser.MixField field = new FieldMixSearchQParser.MixField(fieldName,level);
    			resutl.add(field);
    		}
    		return resutl;
    	}else{
    		return null;
    	}
    }
}