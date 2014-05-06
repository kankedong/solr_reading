package org.apache.solr.core;

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

import com.google.common.collect.Lists;
import org.apache.solr.common.SolrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;


class SolrCores {

  //modifyLock用于SolrCores的同步操作，各个core之间的共享锁，例如在写入一个core的映射表时，就进行同步的操作
  private static Object modifyLock = new Object(); // for locking around manipulating any of the core maps.
  
  //键值对，存储solr的相关对象
  private final Map<String, SolrCore> cores = new LinkedHashMap<String, SolrCore>(); // For "permanent" cores

  //WARNING! The _only_ place you put anything into the list of transient cores is with the putTransientCore method!
  //延迟加载的core？
  private Map<String, SolrCore> transientCores = new LinkedHashMap<String, SolrCore>(); // For "lazily loaded" cores

  //core的描述对象映射表，core的描述信息，，指的是core的配置信息的属性值
  private final Map<String, CoreDescriptor> dynamicDescriptors = new LinkedHashMap<String, CoreDescriptor>();

  //创建的core的core名和core对象的映射表
  private final Map<String, SolrCore> createdCores = new LinkedHashMap<String, SolrCore>();

  //core对象与core名称的映射表
  private Map<SolrCore, String> coreToOrigName = new ConcurrentHashMap<SolrCore, String>();

  //core的容器对象，不明白为啥CoreContainer与SolrCores的对象互相依赖？为啥互相依赖呢？
  private final CoreContainer container;

  //日志记录器
  private static final Logger logger = LoggerFactory.getLogger(SolrCores.class);

  // This map will hold objects that are being currently operated on. The core (value) may be null in the case of
  // initial load. The rule is, never to any operation on a core that is currently being operated upon.
  private static final Set<String> pendingCoreOps = new HashSet<String>();

  // Due to the fact that closes happen potentially whenever anything is _added_ to the transient core list, we need
  // to essentially queue them up to be handled via pendingCoreOps.
  private static final List<SolrCore> pendingCloses = new ArrayList<SolrCore>();

  SolrCores(CoreContainer container) {
    this.container = container;
  }

  // Trivial helper method for load, note it implements LRU(latest least used，最近最少使用) on transient cores. Also note, if
  // there is no setting for max size, nothing is done and all cores go in the regular "cores" list
  protected void allocateLazyCores(final int cacheSize, final SolrResourceLoader loader) {
    if (cacheSize != Integer.MAX_VALUE) {
      CoreContainer.log.info("Allocating transient cache for {} transient cores", cacheSize);
      transientCores = new LinkedHashMap<String, SolrCore>(cacheSize, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, SolrCore> eldest) {
          if (size() > cacheSize) {
            synchronized (modifyLock) {
              SolrCore coreToClose = eldest.getValue();
              logger.info("Closing transient core [{}]", coreToClose.getName());
              pendingCloses.add(coreToClose); // Essentially just queue this core up for closing.
              modifyLock.notifyAll(); // Wakes up closer thread too
            }
            return true;
          }
          return false;
        }
      };
    }
  }

  /**
   * 加入core的描述对象
   * @param rawName
   * @param p
   */
  protected void putDynamicDescriptor(String rawName, CoreDescriptor p) {
    synchronized (modifyLock) {
      dynamicDescriptors.put(rawName, p);
    }
  }

  // We are shutting down. You can't hold the lock on the various lists of cores while they shut down, so we need to
  // make a temporary copy of the names and shut them down outside the lock.
  protected void close() {
    Collection<SolrCore> coreList = new ArrayList<SolrCore>();

    // It might be possible for one of the cores to move from one list to another while we're closing them. So
    // loop through the lists until they're all empty. In particular, the core could have moved from the transient
    // list to the pendingCloses list.

    do {
      coreList.clear();
      synchronized (modifyLock) {
        // make a copy of the cores then clear the map so the core isn't handed out to a request again
        coreList.addAll(cores.values());
        cores.clear();

        coreList.addAll(transientCores.values());
        transientCores.clear();

        coreList.addAll(pendingCloses);
        pendingCloses.clear();
      }

      for (SolrCore core : coreList) {
        try {
          core.close();
        } catch (Throwable t) {
          SolrException.log(CoreContainer.log, "Error shutting down core", t);
        }
      }
    } while (coreList.size() > 0);
  }

  //WARNING! This should be the _only_ place you put anything into the list of transient cores!
  /**
   * 存放临时的core
   * @param cfg
   * @param name
   * @param core
   * @param loader
   * @return
   */
  protected SolrCore putTransientCore(ConfigSolr cfg, String name, SolrCore core, SolrResourceLoader loader) {
    SolrCore retCore;
    CoreContainer.log.info("Opening transient core {}", name);
    synchronized (modifyLock) {
      retCore = transientCores.put(name, core);
    }
    return retCore;
  }
  
  
  /**
   * 放入一个普通的core
   * @param name
   * @param core
   * @return
   */
  protected SolrCore putCore(String name, SolrCore core) {
    synchronized (modifyLock) {
      return cores.put(name, core);
    }
  }

  /**
   * 获取所有core
   * @return
   */
  List<SolrCore> getCores() {
    List<SolrCore> lst = new ArrayList<SolrCore>();

    synchronized (modifyLock) {
      lst.addAll(cores.values());
      return lst;
    }
  }

  /**
   * 获取所有的core名称
   * @return
   */
  Set<String> getCoreNames() {
    Set<String> set = new TreeSet<String>();

    synchronized (modifyLock) {
      set.addAll(cores.keySet());
      set.addAll(transientCores.keySet());
    }
    return set;
  }

  /**
   * 根据一个core对象，得到一个core的全部名称
   * @param core
   * @return
   */
  List<String> getCoreNames(SolrCore core) {
    List<String> lst = new ArrayList<String>();

    synchronized (modifyLock) {
      for (Map.Entry<String, SolrCore> entry : cores.entrySet()) {
        if (core == entry.getValue()) {
          lst.add(entry.getKey());
        }
      }
      for (Map.Entry<String, SolrCore> entry : transientCores.entrySet()) {
        if (core == entry.getValue()) {
          lst.add(entry.getKey());
        }
      }
    }
    return lst;
  }

  /**
   * Gets a list of all cores, loaded and unloaded (dynamic)
   *
   * @return all cores names, whether loaded or unloaded.
   */
  /**
   * 获取全部的core名称
   * @return
   */
  public Collection<String> getAllCoreNames() {
    Set<String> set = new TreeSet<String>();
    synchronized (modifyLock) {
      set.addAll(cores.keySet());
      set.addAll(transientCores.keySet());
      set.addAll(dynamicDescriptors.keySet());
      set.addAll(createdCores.keySet());
    }
    return set;
  }

  SolrCore getCore(String name) {

    synchronized (modifyLock) {
      return cores.get(name);
    }
  }

  /**
   * 交换两个core
   * @param n0
   * @param n1
   */
  protected void swap(String n0, String n1) {

    synchronized (modifyLock) {
      SolrCore c0 = cores.get(n0);
      SolrCore c1 = cores.get(n1);
      if (c0 == null) { // Might be an unloaded transient core
        c0 = container.getCore(n0);
        if (c0 == null) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n0);
        }
      }
      if (c1 == null) { // Might be an unloaded transient core
        c1 = container.getCore(n1);
        if (c1 == null) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No such core: " + n1);
        }
      }
      cores.put(n0, c1);
      cores.put(n1, c0);

      c0.setName(n1);
      c1.setName(n0);
    }

  }

  /**
   * 删除一个core
   * @param name
   * @param removeOrig
   * @return
   */
  protected SolrCore remove(String name, boolean removeOrig) {

    synchronized (modifyLock) {
      SolrCore tmp = cores.remove(name);
      SolrCore ret = null;
      if (removeOrig && tmp != null) {
        coreToOrigName.remove(tmp);
      }
      ret = (ret == null) ? tmp : ret;
      // It could have been a newly-created core. It could have been a transient core. The newly-created cores
      // in particular should be checked. It could have been a dynamic core.
      tmp = transientCores.remove(name);
      ret = (ret == null) ? tmp : ret;
      tmp = createdCores.remove(name);
      ret = (ret == null) ? tmp : ret;
      dynamicDescriptors.remove(name);
      return ret;
    }
  }

  /**
   * 设置core的原有名称
   * @param c
   * @param name
   */
  protected void putCoreToOrigName(SolrCore c, String name) {

    synchronized (modifyLock) {
      coreToOrigName.put(c, name);
    }

  }

  /**
   * core的名称恢复为初始名称,删除old core，放入新的core
   * @param newCore
   * @param core
   */
  protected void removeCoreToOrigName(SolrCore newCore, SolrCore core) {

    synchronized (modifyLock) {
      String origName = coreToOrigName.remove(core);
      if (origName != null) {
        coreToOrigName.put(newCore, origName);
      }
    }
  }

  /**
   * 只要有这个core就返回
   * @param name
   * @param incRefCount
   * @return
   */
  /* If you don't increment the reference count, someone could close the core before you use it. */
  protected SolrCore  getCoreFromAnyList(String name, boolean incRefCount) {
    synchronized (modifyLock) {
      SolrCore core = cores.get(name);

      if (core == null) {
        core = transientCores.get(name);
      }

      if (core != null && incRefCount) {
        core.open();
      }

      return core;
    }
  }
  
  //获取core的描述对象
  /**
   * 获取一个core的描述对象
   * @param name
   * @return
   */
  protected CoreDescriptor getDynamicDescriptor(String name) {
    synchronized (modifyLock) {
      return dynamicDescriptors.get(name);
    }
  }

  // See SOLR-5366 for why the UNLOAD command needs to know whether a core is actually loaded or not, it might have
  // to close the core. However, there's a race condition. If the core happens to be in the pending "to close" queue,//关闭队列在何处体现？
  // we should NOT close it in unload core.
  // 解决的是solr=5366这个bug，UNLOAD命令需要知道一个core是否已经加载，。。。
  protected boolean isLoadedNotPendingClose(String name) {
    // Just all be synchronized
    synchronized (modifyLock) {
      if (cores.containsKey(name)) {
        return true;
      }
      if (transientCores.containsKey(name)) {
        // Check pending
        for (SolrCore core : pendingCloses) {
          if (core.getName().equals(name)) {
            return false;
          }
        }

        return true;
      }
    }
    return false;
  }

  /**
   * 是否被加载
   * @param name
   * @return
   */
  protected boolean isLoaded(String name) {
    synchronized (modifyLock) {
      if (cores.containsKey(name)) {
        return true;
      }
      if (transientCores.containsKey(name)) {
        return true;
      }
    }
    return false;

  }
  
  /**
   * 获取没有加载core的描述对象
   * @param cname
   * @return
   */
  protected CoreDescriptor getUnloadedCoreDescriptor(String cname) {
    synchronized (modifyLock) {
      CoreDescriptor desc = dynamicDescriptors.get(cname);
      if (desc == null) {
        return null;
      }
      return new CoreDescriptor(cname, desc);
    }

  }
  
  /**
   * 得到core的原始名称
   * @param solrCore
   * @return
   */
  protected String getCoreToOrigName(SolrCore solrCore) {
    synchronized (modifyLock) {
      return coreToOrigName.get(solrCore);
    }
  }
  
  /**
   * 等待追加的core操作
   * @param name
   * @return
   */
  // Wait here until any pending operations (load, unload or reload) are completed on this core.
  protected SolrCore waitAddPendingCoreOps(String name) {

    // Keep multiple threads from operating on a core at one time.
    synchronized (modifyLock) {
      boolean pending;
      do { // Are we currently doing anything to this core? Loading, unloading, reloading?
        pending = pendingCoreOps.contains(name); // wait for the core to be done being operated upon
        if (! pending) { // Linear list, but shouldn't be too long
          for (SolrCore core : pendingCloses) {
            if (core.getName().equals(name)) {
              pending = true;
              break;
            }
          }
        }
        if (container.isShutDown()) return null; // Just stop already.

        if (pending) {
          try {
            modifyLock.wait();
          } catch (InterruptedException e) {
            return null; // Seems best not to do anything at all if the thread is interrupted
          }
        }
      } while (pending);
      // We _really_ need to do this within the synchronized block!
      if (! container.isShutDown()) {
        if (! pendingCoreOps.add(name)) {
          CoreContainer.log.warn("Replaced an entry in pendingCoreOps {}, we should not be doing this", name);
        }
        return getCoreFromAnyList(name, false); // we might have been _unloading_ the core, so return the core if it was loaded.
      }
    }
    return null;
  }
  
  /**
   * 删除等待执行的操作，目的是在其他操作正在进行时，不进行其他操作
   * @param name
   */
  // We should always be removing the first thing in the list with our name! The idea here is to NOT do anything n
  // any core while some other operation is working on that core.
  protected void removeFromPendingOps(String name) {
    synchronized (modifyLock) {
      if (! pendingCoreOps.remove(name)) {
        CoreContainer.log.warn("Tried to remove core {} from pendingCoreOps and it wasn't there. ", name);
      }
      modifyLock.notifyAll();
    }
  }
  
  /**
   * 返回锁，由于core都是被记录在Solrcores对象中的
   * @return
   */
  protected Object getModifyLock() {
    return modifyLock;
  }

  // Be a little careful. We don't want to either open or close a core unless it's _not_ being opened or closed by
  // another thread. So within this lock we'll walk along the list of pending closes until we find something NOT in
  // the list of threads currently being loaded or reloaded. The "usual" case will probably return the very first
  // one anyway..
  protected SolrCore getCoreToClose() {
    synchronized (modifyLock) {
      for (SolrCore core : pendingCloses) {
        if (! pendingCoreOps.contains(core.getName())) {
          pendingCoreOps.add(core.getName());
          pendingCloses.remove(core);
          return core;
        }
      }
    }
    return null;
  }
  
  /**
   * 添加创建的core
   * @param core
   */
  protected void addCreated(SolrCore core) {
    synchronized (modifyLock) {
      createdCores.put(core.getName(), core);
    }
  }

  /**
   * Return the CoreDescriptor corresponding to a given core name.
   * @param coreName the name of the core
   * @return the CoreDescriptor
   */
  /**
   * 得到core的描述对象
   * @param coreName
   * @return
   */
  public CoreDescriptor getCoreDescriptor(String coreName) {
    synchronized (modifyLock) {
      if (cores.containsKey(coreName))
        return cores.get(coreName).getCoreDescriptor();
      if (dynamicDescriptors.containsKey(coreName))
        return dynamicDescriptors.get(coreName);
      return null;
    }
  }

  /**
   * Get the CoreDescriptors for every SolrCore managed here
   * @return a List of CoreDescriptors
   */
  /**
   * 得到全部core的描述对象
   * @return
   */
  public List<CoreDescriptor> getCoreDescriptors() {
    List<CoreDescriptor> cds = Lists.newArrayList();
    synchronized (modifyLock) {
      for (String coreName : getAllCoreNames()) {
        // TODO: This null check is a bit suspicious - it seems that
        // getAllCoreNames might return deleted cores as well?
        CoreDescriptor cd = getCoreDescriptor(coreName);
        if (cd != null)
          cds.add(cd);
      }
    }
    return cds;
  }
}
