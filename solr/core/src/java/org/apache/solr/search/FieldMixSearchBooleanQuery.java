package org.apache.solr.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.solr.common.params.SolrParams;

import com.sun.corba.se.impl.orbutil.closure.Constant;

///**
// * 类说明:
// * 创建者:lixiaodong@cnir.com
// * 修改者:
// * 创建时间:2014-5-7 下午5:35:08
// * 修改时间:2014-5-7 下午5:35:08
// */
//public class FieldMixSearchBooleanQuery extends BooleanQuery {
//    
//	private SolrParams params;
// 
//    public FieldMixSearchBooleanQuery() {
//        super();
//    }
// 
//    public FieldMixSearchBooleanQuery(SolrParams params,boolean disableCoord) {
//        super(disableCoord);
//        this.params = params;
//    }
// 
//    public class MultiWeight extends BooleanWeight{
//        private SolrParams params;
//        private String[] factor;
// 
//        public MultiWeight(SolrParams params,IndexSearcher searcher,boolean disableCoord) throws IOException {
//            super(searcher, disableCoord);
//            this.params = params;
//        }
//        //最重要的一步，修改公式得分
//        @Override
//        public Scorer scorer(AtomicReaderContext context, boolean scoreDocsInOrder,boolean topScorer, Bits acceptDocs)
//                throws IOException {
//            String[] tempFactor = FieldCache.DEFAULT.gets.getStrings(reader, Constant.FIELD_FACTOR);
//            if(tempFactor != null && tempFactor.length != 0){
//                factor = tempFactor.clone();
//            }
//            List<Scorer> required = new ArrayList<Scorer>();
//            List<Scorer> prohibited = new ArrayList<Scorer>();
//            List<Scorer> optional = new ArrayList<Scorer>();
//            Iterator<BooleanClause> cIter = clauses().iterator();
//            for (Weight w  : weights) {
//                BooleanClause c =  cIter.next();
//                Scorer subScorer = w.scorer(reader, true, false);
//                if (subScorer == null) {
//                    if (c.isRequired()) {
//                        return null;
//                    }
//                } else if (c.isRequired()) {
//                    required.add(subScorer);
//                } else if (c.isProhibited()) {
//                    prohibited.add(subScorer);
//                } else {
//                    optional.add(subScorer);
//                }
//            }
// 
//            // Check if we can return a BooleanScorer
//            if (!scoreDocsInOrder && topScorer && required.size() == 0) {
//                return new BooleanScorer(this, isCoordDisabled(), similarity, minNrShouldMatch, optional, prohibited, maxCoord);
//            }
// 
//            if (required.size() == 0 && optional.size() == 0) {
//                // no required and optional clauses.
//                return null;
//            } else if (optional.size() < minNrShouldMatch) {
//                // either >1 req scorer, or there are 0 req scorers and at least 1
//                // optional scorer. Therefore if there are not enough optional scorers
//                // no documents will be matched by the query
//                return null;
//            }
// 
//            // Return a BooleanScorer2
//            return new BooleanScorer2(this, isCoordDisabled(), similarity, minNrShouldMatch, required, prohibited, optional, maxCoord);
//        }
// 
//        public String[] getFactor() {
//            return factor;
//        }
// 
//        public SolrParams getParams() {
//            return params;
//        }
//    }
// 
//    public Weight createWeight(IndexSearcher searcher) throws IOException {
//        return new MultiWeight(params,searcher, isCoordDisabled());
//    }
//}
