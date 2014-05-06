package use.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class GroupField {  
  /** 
   * 字段名 
   */  
  private String name;  
   
  /** 
   * 所有可能的分组字段值，排序按每个字段值的文档个数大小排序 
   */  
  private List<String> values = new ArrayList<String>();  
   
  /** 
   * 保存字段值和文档个数的对应关系 
   */  
  private Map<String, Integer> countMap = new HashMap<String, Integer>();  
   
  public Map<String, Integer> getCountMap() {  
   return countMap;  
  }  
   
  public void setCountMap(Map<String, Integer> countMap) {  
   this.countMap = countMap;  
  }  
   
  public String getName() {  
   return name;  
  }  
   
  public void setName(String name) {  
   this.name = name;  
  }  
   
  public List<String> getValues() {  
   Collections.sort(values, new ValueComparator());  
   return values;  
  }  
   
  public void setValues(List<String> values) {  
   this.values = values;  
  }  
   
  public void addValue(String value) {  
   if (value == null || "".equals(value))  
    return;  
   // 对于多值的字段，支持按空格拆分  
   String[] temp = value.split(" ");  
   for (String str : temp) {  
    if (countMap.get(str) == null) {  
     countMap.put(str, 1);  
     values.add(str);  
    } else {  
     countMap.put(str, countMap.get(str) + 1);  
    }  
   }  
  }  
   
  class ValueComparator implements Comparator<String> {  
   
   public int compare(String value0, String value1) {  
    if (countMap.get(value0) > countMap.get(value1)) {  
     return -1;  
    } else if (countMap.get(value0) < countMap.get(value1)) {  
     return 1;  
    }  
    return 0;  
   }  
  }  
 } 