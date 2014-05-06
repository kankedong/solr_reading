package use.test;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocsCollector;

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

public class GroupCollector  extends TopDocsCollector {  
  
  private Collector collector;  
  private int docBase;  
  
  private String[] fc; // fieldCache  
  private GroupField gf = new GroupField();// 保存分组统计结果  
  
 GroupCollector(Collector topDocsCollector, String[] fieldCache)  
   throws IOException {  
  super(null);  
  collector = topDocsCollector;  
  this.fc = fieldCache;  
 }  
  
 @Override  
 public void collect(int doc) throws IOException {  
  collector.collect(doc);  
  // 因为doc是每个segment的文档编号，需要加上docBase才是总的文档编号  
  int docId = doc + docBase;  
  // 添加的GroupField中，由GroupField负责统计每个不同值的数目  
  gf.addValue(fc[docId]);  
 }  
  
// @Override  
// public void setNextReader(IndexReader reader, int docBase)  
//   throws IOException {  
//  collector.setNextReader(reader, docBase);  
//  this.docBase = docBase;  
// }  
//  
 @Override  
 public void setScorer(Scorer scorer) throws IOException {  
  collector.setScorer(scorer);  
 }  
  
 @Override  
 public boolean acceptsDocsOutOfOrder() {  
  return collector.acceptsDocsOutOfOrder();  
 }  
  
 public void setFc(String[] fc) {  
  this.fc = fc;  
 }  
  
 public GroupField getGroupField() {  
  return gf;  
 }

@Override
public void setNextReader(AtomicReaderContext context) throws IOException {}  
}  
