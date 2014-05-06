package org.apache.solr.handler;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.loader.ContentStreamLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Shares common code between various handlers that manipulate 
 * {@link org.apache.solr.common.util.ContentStream} objects.
 */
public abstract class ContentStreamHandlerBase extends RequestHandlerBase {
  public static Logger log = LoggerFactory.getLogger(ContentStreamHandlerBase.class);

  @Override
  public void init(NamedList args) {
    super.init(args);

    // Caching off by default
    httpCaching = false;
    if (args != null) {
      Object caching = args.get("httpCaching");
      if(caching!=null) {
        httpCaching = Boolean.parseBoolean(caching.toString());
      }
    }
  }
  
  /**
   * 该方法被ContentStreamHandlerBase所继承的父类RequestHandlerBase的
   * public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp)
   * 方法所调用
   */
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    
    SolrParams params = req.getParams();
    
    /** 
     * 根据update.chain 获取不同的处理链，定义于solrconfig.xml文件
     * langid -->  包含TikaLanguageIdentifierUpdateProcessorFactory 
     * script -->  包含StatelessScriptUpdateProcessorFactory 
     * dedupe -->  包含SignatureUpdateProcessorFactory 
     * ..... 
     */
    UpdateRequestProcessorChain processorChain =req.getCore().getUpdateProcessingChain(params.get(UpdateParams.UPDATE_CHAIN));
    
    
    /** 
     * 系统默认的更新处理链包含3个,按顺序执行 
     * LogUpdateProcessorFactory      ---> 记录更新日志，LogUpdateProcessor是LogUpdateProcessorFactory的内部类 
     * DistributedUpdateProcessorFactory  ---> 分布式更新,包含请求转发,版本控制等 
     * RunUpdateProcessorFactory      ---> 写入索引 
     */  
    UpdateRequestProcessor processor = processorChain.createProcessor(req, rsp);

    try {
      
      //stream的loader，用于从请求中获取要索引的document，这个documentLoader的实例对象就是updateHandler创建的对象，by lxd
      ContentStreamLoader documentLoader = newLoader(req, processor);

      Iterable<ContentStream> streams = req.getContentStreams();
      if (streams == null) {
        if (!RequestHandlerUtils.handleCommit(req, processor, params, false) && !RequestHandlerUtils.handleRollback(req, processor, params, false)) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "missing content stream");
        }
      } else {

        for (ContentStream stream : streams) {
          documentLoader.load(req, rsp, stream, processor);
        }

        // Perhaps commit from the parameters
        RequestHandlerUtils.handleCommit(req, processor, params, false);
        RequestHandlerUtils.handleRollback(req, processor, params, false);
      }
    } finally {
      // finish the request
      processor.finish();
    }
  }

  protected abstract ContentStreamLoader newLoader(SolrQueryRequest req, UpdateRequestProcessor processor);
}
