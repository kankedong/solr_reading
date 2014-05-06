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

package org.apache.solr.core;

import com.google.common.collect.Maps;

import org.apache.solr.cloud.ZkController;
import org.apache.solr.cloud.ZkSolrResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.common.util.SolrjNamedThreadFactory;
import org.apache.solr.handler.admin.CollectionsHandler;
import org.apache.solr.handler.admin.CoreAdminHandler;
import org.apache.solr.handler.admin.InfoHandler;
import org.apache.solr.handler.component.ShardHandlerFactory;
import org.apache.solr.logging.LogWatcher;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.apache.solr.util.DefaultSolrThreadFactory;
import org.apache.solr.util.FileUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.solr.cloud.ZkController;
import org.apache.solr.cloud.ZkSolrResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.handler.admin.CollectionsHandler;
import org.apache.solr.handler.admin.CoreAdminHandler;
import org.apache.solr.handler.admin.InfoHandler;
import org.apache.solr.handler.component.ShardHandlerFactory;
import org.apache.solr.logging.LogWatcher;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.apache.solr.util.DefaultSolrThreadFactory;
import org.apache.solr.util.FileUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/**
 *
 * @since solr 1.3
 */
public class CoreContainer {

  protected static final Logger log = LoggerFactory.getLogger(CoreContainer.class);
  
  //core的真实存放的容器
  private final SolrCores solrCores = new SolrCores(this);

  //core的初始化失败信息
  protected final Map<String,Exception> coreInitFailures =
    Collections.synchronizedMap(new LinkedHashMap<String,Exception>());

  //core的管理处理器，注意管理处理器和请求处理器，是分开的存储的
  protected CoreAdminHandler coreAdminHandler = null;
  
  //分片集合处理器
  protected CollectionsHandler collectionsHandler = null;
  
  //信息处理器
  private InfoHandler infoHandler;

  //容器的配置信息对象
  protected Properties containerProperties;

  //缓存的schema对象
  protected Map<String ,IndexSchema> indexSchemaCache;
  
  //是否共享schema
  protected boolean shareSchema;

  //zookeeper容器，用于与zookeeper交互
  protected ZkContainer zkSys = new ZkContainer();
  
  //分片处理器工厂
  private ShardHandlerFactory shardHandlerFactory;
  
  //多线程处理器？
  private ExecutorService updateExecutor = Executors.newCachedThreadPool(
      new SolrjNamedThreadFactory("updateExecutor"));

  protected LogWatcher logging = null;

  //关闭线程？
  private CloserThread backgroundCloser = null;
  
  //solr.xml文件的存储对象
  protected final ConfigSolr cfg;
  
  //solr资源加载器
  protected final SolrResourceLoader loader;

  //solrHome的位置
  protected final String solrHome;

  //core的加载器
  protected final CoresLocator coresLocator;

  {
    log.info("New CoreContainer " + System.identityHashCode(this));
  }

  /**
   * Create a new CoreContainer using system properties to detect the solr home
   * directory.  The container's cores are not loaded.
   * @see #load()
   */
  /**
   * 使用系统信息检测，去本地文件系统区加载solr.xml
   */
  public CoreContainer() {
    this(new SolrResourceLoader(SolrResourceLoader.locateSolrHome()));
  }

  /**
   * Create a new CoreContainer using the given SolrResourceLoader.  The container's
   * cores are not loaded.
   * @param loader the SolrResourceLoader
   * @see #load()
   */
  /**
   * 使用一个指定的SolrResourceLoader对象来加载solr.xml
   * @param loader
   */
  public CoreContainer(SolrResourceLoader loader) {
    this(loader, ConfigSolr.fromSolrHome(loader, loader.getInstanceDir()));
  }

  /**
   * Create a new CoreContainer using the given solr home directory.  The container's
   * cores are not loaded.
   * @param solrHome a String containing the path to the solr home directory
   * @see #load()
   */
  /**
   * 指定solrhome的目录，创建
   * @param solrHome
   */
  public CoreContainer(String solrHome) {
    this(new SolrResourceLoader(solrHome));
  }

  /**
   * Create a new CoreContainer using the given SolrResourceLoader,
   * configuration and CoresLocator.  The container's cores are
   * not loaded.
   * @param loader the SolrResourceLoader
   * @param config a ConfigSolr representation of this container's configuration
   * @see #load()
   */
  /**
   * ? 
   * @param loader
   * @param config
   */
  public CoreContainer(SolrResourceLoader loader, ConfigSolr config) {
    this.loader = checkNotNull(loader);
    this.solrHome = loader.getInstanceDir();
    this.cfg = checkNotNull(config);
    this.coresLocator = config.getCoresLocator();
  }

  public CoreContainer(SolrResourceLoader loader, ConfigSolr config, CoresLocator locator) {
    this.loader = checkNotNull(loader);
    this.solrHome = loader.getInstanceDir();
    this.cfg = checkNotNull(config);
    this.coresLocator = locator;
  }

  /**
   * Create a new CoreContainer and load its cores
   * @param solrHome the solr home directory
   * @param configFile the file containing this container's configuration
   * @return a loaded CoreContainer
   */
  public static CoreContainer createAndLoad(String solrHome, File configFile) {
    SolrResourceLoader loader = new SolrResourceLoader(solrHome);
    CoreContainer cc = new CoreContainer(loader, ConfigSolr.fromFile(loader, configFile));
    cc.load();
    return cc;
  }
  
  public Properties getContainerProperties() {
    return containerProperties;
  }

  //-------------------------------------------------------------------
  // Initialization / Cleanup
  //-------------------------------------------------------------------

  /**
   * Load the cores defined for this CoreContainer
   */
  public void load()  {

    log.info("Loading cores into CoreContainer [instanceDir={}]", loader.getInstanceDir());

    // add the sharedLib to the shared resource loader before initializing cfg based plugins
    String libDir = cfg.getSharedLibDirectory();
    if (libDir != null) {
      File f = FileUtils.resolvePath(new File(solrHome), libDir);
      log.info("loading shared library: " + f.getAbsolutePath());
      loader.addToClassLoader(libDir, null, false);
      //重新加载Lucene的Service provider interface
      loader.reloadLuceneSPI();
    }
    
    //创建共享资源处理器，第三方jar？
    shardHandlerFactory = ShardHandlerFactory.newInstance(cfg.getShardHandlerFactoryPluginInfo(), loader);

    //为临时core分配内存空间
    solrCores.allocateLazyCores(cfg.getTransientCacheSize(), loader);

    //创建日志记录器
    logging = LogWatcher.newRegisteredLogWatcher(cfg.getLogWatcherConfig(), loader);
    
    //是否有schema的缓存？
    shareSchema = cfg.hasSchemaCache();

    //如果有schema缓存？
    if (shareSchema) {
      indexSchemaCache = new ConcurrentHashMap<String,IndexSchema>();
    }
    
    //初始化zookeeper
    zkSys.initZooKeeper(this, solrHome, cfg);

    //创建solrcloud中的collection处理器
    collectionsHandler = new CollectionsHandler(this);
    
    //信息处理器，比如日志信息，系统信息
    infoHandler = new InfoHandler(this);
    
    //core管理界面处理器
    coreAdminHandler = createMultiCoreHandler(cfg.getCoreAdminHandlerClass());
    
    //从solr目录中读取容器的配置信息
    containerProperties = cfg.getSolrProperties("solr");

    // setup executor to load cores in parallel，并行读取core信息；
    // do not limit the size of the executor in zk mode since cores may try and wait for each other.为什么不限制执行器的大小，为什么会相互等待呢？
    ExecutorService coreLoadExecutor = Executors.newFixedThreadPool(
        ( zkSys.getZkController() == null ? cfg.getCoreLoadThreadCount() : Integer.MAX_VALUE ),
        new DefaultSolrThreadFactory("coreLoadExecutor") );

    try {
      
      //completionService，将生产新的异步任务与使用已完成任务的结果分离开来的服务。
      CompletionService<SolrCore> completionService = new ExecutorCompletionService<SolrCore>(
          coreLoadExecutor);
      
      //pendind用于存放任务直接结果
      Set<Future<SolrCore>> pending = new HashSet<Future<SolrCore>>();
      
      //获取全部的core描述对象
      List<CoreDescriptor> cds = coresLocator.discover(this);
      checkForDuplicateCoreNames(cds);
      
      //遍历每一个core描述对象，core的描述对象可以理解为core的元数据信息吗？，根据core的描述信息来创建一个任务来进行core的创建于注册
      for (final CoreDescriptor cd : cds) {

        final String name = cd.getName();
        try {
          
          //是否是短暂的core，是否不是启动时加载
          if (cd.isTransient() || ! cd.isLoadOnStartup()) {
            // Store it away for later use. includes non-transient but not
            // loaded at startup cores.
            //
            solrCores.putDynamicDescriptor(name, cd);
          }
          if (cd.isLoadOnStartup()) { // The normal case,启动时加载的core

            //创建一个有结果任务，该任务负责创建注册一个core，并注册这个core
            Callable<SolrCore> task = new Callable<SolrCore>() {
              @Override
              public SolrCore call() {
                SolrCore c = null;
                try {
                  
                  if (zkSys.getZkController() != null) {
                    //如果存在zookeeper处理器，将core的描述对象，预注册在zookeeper上
                    preRegisterInZk(cd);
                  }
                  
                  //创建一个solrcore
                  c = create(cd);
                  
                  //在zookeeper上注册这个core
                  registerCore(cd.isTransient(), name, c, false);
                  
                } catch (Throwable t) {
              /*    if (isZooKeeperAware()) {
                    try {
                      zkSys.zkController.unregister(name, cd);
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                      SolrException.log(log, null, e);
                    } catch (KeeperException e) {
                      SolrException.log(log, null, e);
                    }
                  }*/
                  SolrException.log(log, null, t);
                  if (c != null) {
                    c.close();
                  }
                }
                return c;
              }
            };
            
            //completionService提交任务，便于新任务与已完成任务的分离工作
            pending.add(completionService.submit(task));

          }
        } catch (Throwable ex) {
          SolrException.log(log, null, ex);
        }
      }
      
      //遍历pengding中记录的线程执行结果
      while (pending != null && pending.size() > 0) {
        try {
          
          //从completionService中得到一个执行完的任务结果
          Future<SolrCore> future = completionService.take();
          
          //为什么返回null值是return而不是continue;
          if (future == null) return;
          
          //pengding中删除一个结果，这个不会造成问题吗？
          pending.remove(future);

          try {
            SolrCore c = future.get();
            // track original names
            if (c != null) {
              //放入到solrcores的相应对象中，同步方法
              solrCores.putCoreToOrigName(c, c.getName());
            }
          } catch (ExecutionException e) {
            SolrException.log(SolrCore.log, "Error loading core", e);
          }

        } catch (InterruptedException e) {
          throw new SolrException(SolrException.ErrorCode.SERVICE_UNAVAILABLE,
              "interrupted while loading core", e);
        }
      }

      // Start the background thread
      //后台运行的关闭线程，目前还不清楚进行了什么关闭工作
      backgroundCloser = new CloserThread(this, solrCores, cfg);
      backgroundCloser.start();

    } finally {
      if (coreLoadExecutor != null) {
        ExecutorUtil.shutdownNowAndAwaitTermination(coreLoadExecutor);
      }
    }
  }

  /**
   * 重复core名称检测
   * @param cds
   */
  private static void checkForDuplicateCoreNames(List<CoreDescriptor> cds) {
    Map<String, String> addedCores = Maps.newHashMap();
    for (CoreDescriptor cd : cds) {
      final String name = cd.getName();
      if (addedCores.containsKey(name))
        throw new SolrException(ErrorCode.SERVER_ERROR,
            String.format(Locale.ROOT, "Found multiple cores with the name [%s], with instancedirs [%s] and [%s]",
                name, addedCores.get(name), cd.getInstanceDir()));
      addedCores.put(name, cd.getInstanceDir());
    }
  }

  private volatile boolean isShutDown = false;
  
  public boolean isShutDown() {
    return isShutDown;
  }

  /**
   * Stops all cores.停止core是什么意思?，修改在zookeeper上的状态吗？
   */
  public void shutdown() {
    log.info("Shutting down CoreContainer instance="
        + System.identityHashCode(this));
    
    if (isZooKeeperAware()) {
      try {
        zkSys.getZkController().publishAndWaitForDownStates();
      } catch (KeeperException e) {
        log.error("", e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("", e);
      }
    }
    isShutDown = true;

    if (isZooKeeperAware()) {
      zkSys.publishCoresAsDown(solrCores.getCores());
      cancelCoreRecoveries();
    }


    try {
      // First wake up the closer thread, it'll terminate almost immediately since it checks isShutDown.
      synchronized (solrCores.getModifyLock()) {
        solrCores.getModifyLock().notifyAll(); // wake up anyone waiting
      }
      if (backgroundCloser != null) { // Doesn't seem right, but tests get in here without initializing the core.
        try {
          backgroundCloser.join();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          if (log.isDebugEnabled()) {
            log.debug("backgroundCloser thread was interrupted before finishing");
          }
        }
      }
      // Now clear all the cores that are being operated upon.
      solrCores.close();

      // It's still possible that one of the pending dynamic load operation is waiting, so wake it up if so.
      // Since all the pending operations queues have been drained, there should be nothing to do.
      synchronized (solrCores.getModifyLock()) {
        solrCores.getModifyLock().notifyAll(); // wake up the thread
      }

    } finally {
      if (shardHandlerFactory != null) {
        shardHandlerFactory.close();
      }
      
      ExecutorUtil.shutdownAndAwaitTermination(updateExecutor);
      
      // we want to close zk stuff last

      zkSys.close();

    }
    org.apache.lucene.util.IOUtils.closeWhileHandlingException(loader); // best effort
  }

  /**
   * 取消core的恢复工作
   */
  public void cancelCoreRecoveries() {

    List<SolrCore> cores = solrCores.getCores();

    // we must cancel without holding the cores sync
    // make sure we wait for any recoveries to stop
    for (SolrCore core : cores) {
      try {
        core.getSolrCoreState().cancelRecovery();
      } catch (Throwable t) {
        SolrException.log(log, "Error canceling recovery for core", t);
      }
    }
  }
  
  @Override
  protected void finalize() throws Throwable {
    try {
      if(!isShutDown){
        log.error("CoreContainer was not shutdown prior to finalize(), indicates a bug -- POSSIBLE RESOURCE LEAK!!!  instance=" + System.identityHashCode(this));
      }
    } finally {
      super.finalize();
    }
  }

  /**
   * 返回core的加载器
   * @return
   */
  public CoresLocator getCoresLocator() {
    return coresLocator;
  }
  
  /**
   * 在zookeeper上注册一个core
   * @param isTransientCore
   * @param name
   * @param core
   * @param returnPrevNotClosed
   * @return
   */
  protected SolrCore registerCore(boolean isTransientCore, String name, SolrCore core, boolean returnPrevNotClosed) {
    
    //null不能注册
    if( core == null ) {
      throw new RuntimeException( "Can not register a null core." );
    }
    
    //core的名称是否合法？
    if( name == null ||
        name.indexOf( '/'  ) >= 0 ||
        name.indexOf( '\\' ) >= 0 ){
      throw new RuntimeException( "Invalid core name: "+name );
    }
    // We can register a core when creating them via the admin UI, so we need to insure that the dynamic descriptors
    // are up to date
    //因为可以通过管理界面创建一个core，所以需要确保这些动态的core描述对象，是最新的
    CoreDescriptor cd = core.getCoreDescriptor();
    if ((cd.isTransient() || ! cd.isLoadOnStartup())
        && solrCores.getDynamicDescriptor(name) == null) {
      // Store it away for later use. includes non-transient but not
      // loaded at startup cores.
      //为了后续的使用而存贮这些core描述，包括那些非临时，但是启动没加载的core
      solrCores.putDynamicDescriptor(name, cd);
    }
    
    //用old表示老的solrcore对象？为什么会出现老对象
    SolrCore old = null;

    //如果关闭了，isShutDown是volatile变量
    if (isShutDown) {
      core.close();
      throw new IllegalStateException("This CoreContainer has been shutdown");
    }
    if (isTransientCore) {
      //是临时core，放入solrcores的响应对象中，并返回
      old = solrCores.putTransientCore(cfg, name, core, loader);
    } else {
      //非临时core，放入solrcores的响应对象中，并返回
      old = solrCores.putCore(name, core);
    }
      /*
      * set both the name of the descriptor and the name of the
      * core, since the descriptors name is used for persisting.
      */
    //设置描述对象的名字和core的名字，因为描述对象的名字用于持久化
    core.setName(name);

    //所操作coreInitFailures的删除操作，有可能是因为core会多次注册，只要成功一次就删除错误的记录？所以remove吗？
    synchronized (coreInitFailures) {
      coreInitFailures.remove(name);
    }

    if( old == null || old == core) {
      log.info( "registering core: "+name );
      //注册core
      zkSys.registerInZk(core);
      //返回null?表示什么含义
      return null;
    }
    else {
      log.info( "replacing core: "+name );
      if (!returnPrevNotClosed) {
        old.close();
      }
      //替换注册core
      zkSys.registerInZk(core);
      return old;
    }
  }

  /**
   * Registers a SolrCore descriptor in the registry using the core's name.
   * If returnPrev==false, the old core, if different, is closed.
   * @return a previous core having the same name if it existed and returnPrev==true
   */
  //使用core的名称注册core
  public SolrCore register(SolrCore core, boolean returnPrev) {
    return registerCore(core.getCoreDescriptor().isTransient(), core.getName(), core, returnPrev);
  }
  
  //使用core的名称注册core
  public SolrCore register(String name, SolrCore core, boolean returnPrev) {
    return registerCore(core.getCoreDescriptor().isTransient(), name, core, returnPrev);
  }

  // Helper method to separate out creating a core from local configuration files. See create()
  /**
   * 从本地配置文件来创建core
   * @param instanceDir
   * @param dcore
   * @return
   */
  private SolrCore createFromLocal(String instanceDir, CoreDescriptor dcore) {
    SolrResourceLoader solrLoader = null;

    SolrConfig config = null;
    solrLoader = new SolrResourceLoader(instanceDir, loader.getClassLoader(), dcore.getSubstitutableProperties());
    try {
      config = new SolrConfig(solrLoader, dcore.getConfigName(), null);
    } catch (Exception e) {
      log.error("Failed to load file {}", new File(instanceDir, dcore.getConfigName()).getAbsolutePath());
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "Could not load config file " + new File(instanceDir, dcore.getConfigName()).getAbsolutePath(),
          e);
    }

    IndexSchema schema = null;
    if (indexSchemaCache != null) {
      final String resourceNameToBeUsed = IndexSchemaFactory.getResourceNameToBeUsed(dcore.getSchemaName(), config);
      File schemaFile = new File(resourceNameToBeUsed);
      if (!schemaFile.isAbsolute()) {
        schemaFile = new File(solrLoader.getConfigDir(), schemaFile.getPath());
      }
      if (schemaFile.exists()) {
        String key = schemaFile.getAbsolutePath()
            + ":"
            + new SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT).format(new Date(
            schemaFile.lastModified()));
        schema = indexSchemaCache.get(key);
        if (schema == null) {
          log.info("creating new schema object for core: " + dcore.getName());
          schema = IndexSchemaFactory.buildIndexSchema(dcore.getSchemaName(), config);
          indexSchemaCache.put(key, schema);
        } else {
          log.info("re-using schema object for core: " + dcore.getName());
        }
      }
    }

    if (schema == null) {
      schema = IndexSchemaFactory.buildIndexSchema(dcore.getSchemaName(), config);
    }

    SolrCore core = new SolrCore(dcore.getName(), null, config, schema, dcore);

    if (core.getUpdateHandler().getUpdateLog() != null) {
      // always kick off recovery if we are in standalone mode.
      core.getUpdateHandler().getUpdateLog().recoverFromLog();
    }
    return core;
  }

  /**
   * Creates a new core based on a descriptor but does not register it.
   * 创建一个core，基于一个core的描述对象，创建一个core，但不注册它
   *
   * @param dcore a core descriptor
   * @return the newly created core
   */
  public SolrCore create(CoreDescriptor dcore) {

    if (isShutDown) {
      throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE, "Solr has shutdown.");
    }
    
    final String name = dcore.getName();

    try {
      // Make the instanceDir relative to the cores instanceDir if not absolute
      File idir = new File(dcore.getInstanceDir());
      String instanceDir = idir.getPath();
      log.info("Creating SolrCore '{}' using instanceDir: {}",
               dcore.getName(), instanceDir);

      // Initialize the solr config
      SolrCore created = null;
      if (zkSys.getZkController() != null) {
        //存在zookeeper处理器，那么从zookeeper上创建它
        created = zkSys.createFromZk(instanceDir, dcore, loader);
      } else {
        //从本地创建
        created = createFromLocal(instanceDir, dcore);
      }
      
      //持久化新创建的core
      solrCores.addCreated(created); // For persisting newly-created cores.
      return created;

      // :TODO: Java7...
      // http://docs.oracle.com/javase/7/docs/technotes/guides/language/catch-multiple.html
    } catch (Exception ex) {
      throw recordAndThrow(name, "Unable to create core: " + name, ex);
    }
  }

  /**
   * @return a Collection of registered SolrCores
   */
  public Collection<SolrCore> getCores() {
    return solrCores.getCores();
  }

  /**
   * @return a Collection of the names that cores are mapped to
   */
  /**
   * 返回一个分片集合的全部core名称
   * @return
   */
  public Collection<String> getCoreNames() {
    return solrCores.getCoreNames();
  }

  /** This method is currently experimental.//该方法在实验阶段
   * @return a Collection of the names that a specific core is mapped to.
   */
  public Collection<String> getCoreNames(SolrCore core) {
    return solrCores.getCoreNames(core);
  }

  /**
   * get a list of all the cores that are currently loaded
   * @return a list of al lthe available core names in either permanent or transient core lists.
   */
  /**
   * 获取全部的core名称
   * @return
   */
  public Collection<String> getAllCoreNames() {
    return solrCores.getAllCoreNames();

  }

  /**
   * Returns an immutable Map of Exceptions that occured when initializing 
   * SolrCores (either at startup, or do to runtime requests to create cores) 
   * keyed off of the name (String) of the SolrCore that had the Exception 
   * during initialization.
   * <p>
   * While the Map returned by this method is immutable and will not change 
   * once returned to the client, the source data used to generate this Map 
   * can be changed as various SolrCore operations are performed:
   * </p>
   * <ul>
   *  <li>Failed attempts to create new SolrCores will add new Exceptions.</li>
   *  <li>Failed attempts to re-create a SolrCore using a name already contained in this Map will replace the Exception.</li>
   *  <li>Failed attempts to reload a SolrCore will cause an Exception to be added to this list -- even though the existing SolrCore with that name will continue to be available.</li>
   *  <li>Successful attempts to re-created a SolrCore using a name already contained in this Map will remove the Exception.</li>
   *  <li>Registering an existing SolrCore with a name already contained in this Map (ie: ALIAS or SWAP) will remove the Exception.</li>
   * </ul>
   */
  public Map<String,Exception> getCoreInitFailures() {
    synchronized ( coreInitFailures ) {
      return Collections.unmodifiableMap(new LinkedHashMap<String,Exception>
                                         (coreInitFailures));
    }
  }


  // ---------------- Core name related methods --------------- 
  /**
   * Recreates a SolrCore.
   * While the new core is loading, requests will continue to be dispatched to
   * and processed by the old core
   * 
   * @param name the name of the SolrCore to reload
   */
  /**
   * 重新加载一个core
   * @param name
   */
  public void reload(String name) {
    try {
      //检测名称.5以后不进行摸人家检测了？
      name = checkDefault(name);
      
      //获取重新加载前的core
      SolrCore core = solrCores.getCoreFromAnyList(name, false);
      if (core == null)
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "No such core: " + name );

      try {
        //等待添加core的操作，其中包含同步操作
        solrCores.waitAddPendingCoreOps(name);
        //获取core的描述对象
        CoreDescriptor cd = core.getCoreDescriptor();
        //得到core的路径
        File instanceDir = new File(cd.getInstanceDir());

        log.info("Reloading SolrCore '{}' using instanceDir: {}",
                 cd.getName(), instanceDir.getAbsolutePath());
        //定义一个SolrResourceLoader对象
        SolrResourceLoader solrLoader;
        if(zkSys.getZkController() == null) {
          //非solrcloud方式？
          solrLoader = new SolrResourceLoader(instanceDir.getAbsolutePath(), loader.getClassLoader(),
                                                cd.getSubstitutableProperties());
        } else {
          //solrcloud方式？
          try {
            String collection = cd.getCloudDescriptor().getCollectionName();
            zkSys.getZkController().createCollectionZkNode(cd.getCloudDescriptor());
            
            //通过分片集合名称得到zookeeper中的配置文件名称
            String zkConfigName = zkSys.getZkController().readConfigName(collection);
            if (zkConfigName == null) {
              log.error("Could not find config name for collection:" + collection);
              throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
                                           "Could not find config name for collection:" + collection);
            }
            solrLoader = new ZkSolrResourceLoader(instanceDir.getAbsolutePath(), zkConfigName, loader.getClassLoader(),
                cd.getSubstitutableProperties(), zkSys.getZkController());
          } catch (KeeperException e) {
            log.error("", e);
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
                                         "", e);
          } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            log.error("", e);
            throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR,
                                         "", e);
          }
        }
        //使用特定的solrLoader重载core
        SolrCore newCore = core.reload(solrLoader, core);
        // keep core to orig name link
        solrCores.removeCoreToOrigName(newCore, core);//删除old core，放入新的core
        //在zookeeper上注册一个core
        registerCore(false, name, newCore, false);
      } finally {
        solrCores.removeFromPendingOps(name);
      }
      // :TODO: Java7...
      // http://docs.oracle.com/javase/7/docs/technotes/guides/language/catch-multiple.html
    } catch (Exception ex) {
      throw recordAndThrow(name, "Unable to reload core: " + name, ex);
    }
  }

  //5.0 remove all checkDefaults?
  private String checkDefault(String name) {
    return (null == name || name.isEmpty()) ? getDefaultCoreName() : name;
  } 

  /**
   * Swaps two SolrCore descriptors.
   */
  /**
   * 交换两个core的描述对象
   * @param n0
   * @param n1
   */
  public void swap(String n0, String n1) {
    if( n0 == null || n1 == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Can not swap unnamed cores." );
    }
    n0 = checkDefault(n0);
    n1 = checkDefault(n1);
    solrCores.swap(n0, n1);

    coresLocator.persist(this, solrCores.getCoreDescriptor(n0), solrCores.getCoreDescriptor(n1));

    log.info("swapped: "+n0 + " with " + n1);
  }
  
  
  /** Removes and returns registered core w/o decrementing it's reference count */
  /**
   * 删除一个core，并将其引用计数减去1
   * @param name
   * @return
   */
  public SolrCore remove( String name ) {
    name = checkDefault(name);
    CoreDescriptor cd = solrCores.getCoreDescriptor(name);
    SolrCore removed = solrCores.remove(name, true);
    coresLocator.delete(this, cd);
    return removed;
  }

  /**
   * 对一个core进行创建新的名字
   * @param name
   * @param toName
   */
  public void rename(String name, String toName) {
    SolrCore core = getCore(name);
    try {
      if (core != null) {
        registerCore(false, toName, core, false);
        name = checkDefault(name);
        SolrCore old = solrCores.remove(name, false);
        coresLocator.rename(this, old.getCoreDescriptor(), core.getCoreDescriptor());
      }
    } finally {
      if (core != null) {
        core.close();
      }
    }
  }

  /**
   * Get the CoreDescriptors for all cores managed by this container
   * @return a List of CoreDescriptors
   */
  /***
   * 获取containner中的全部描述对象
   * @return
   */
  public List<CoreDescriptor> getCoreDescriptors() {
    return solrCores.getCoreDescriptors();
  }

  /**
   * 通过一个core的名称，获取core的描述对象
   * @param coreName
   * @return
   */
  public CoreDescriptor getCoreDescriptor(String coreName) {
    // TODO make this less hideous!
    for (CoreDescriptor cd : getCoreDescriptors()) {
      if (cd.getName().equals(coreName))
        return cd;
    }
    return null;
  }

  /** 
   * Gets a core by name and increase its refcount.
   *
   * @see SolrCore#close() 
   * @param name the core name
   * @return the core if found, null if a SolrCore by this name does not exist
   * @exception SolrException if a SolrCore with this name failed to be initialized
   */
  /**
   * 获取一个core，并且将引用计数加1
   * @param name
   * @return
   */
  public SolrCore getCore(String name) {

    name = checkDefault(name);

    // Do this in two phases since we don't want to lock access to the cores over a load.
    // 
    SolrCore core = solrCores.getCoreFromAnyList(name, true);

    if (core != null) {
      return core;
    }

    // OK, it's not presently in any list, is it in the list of dynamic cores but not loaded yet? If so, load it.
    CoreDescriptor desc = solrCores.getDynamicDescriptor(name);
    if (desc == null) { //Nope, no transient core with this name
      
      // if there was an error initalizing this core, throw a 500
      // error with the details for clients attempting to access it.
      Exception e = getCoreInitFailures().get(name);
      if (null != e) {
        throw new SolrException(ErrorCode.SERVER_ERROR, "SolrCore '" + name +
                                "' is not available due to init failure: " +
                                e.getMessage(), e);
      }
      // otherwise the user is simply asking for something that doesn't exist.
      return null;
    }

    // This will put an entry in pending core ops if the core isn't loaded
    core = solrCores.waitAddPendingCoreOps(name);

    if (isShutDown) return null; // We're quitting, so stop. This needs to be after the wait above since we may come off
                                 // the wait as a consequence of shutting down.
    try {
      if (core == null) {
        if (zkSys.getZkController() != null) {
          preRegisterInZk(desc);
        }
        core = create(desc); // This should throw an error if it fails.
        core.open();
        registerCore(desc.isTransient(), name, core, false);
      } else {
        core.open();
      }
    } catch(Exception ex){
      // remains to be seen how transient cores and such
      // will work in SolrCloud mode, but just to be future 
      // proof...
      /*if (isZooKeeperAware()) {
        try {
          getZkController().unregister(name, desc);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          SolrException.log(log, null, e);
        } catch (KeeperException e) {
          SolrException.log(log, null, e);
        }
      }*/
      throw recordAndThrow(name, "Unable to create core: " + name, ex);
    } finally {
      solrCores.removeFromPendingOps(name);
    }

    return core;
  }

  // ---------------- Multicore self related methods ---------------
  /** 
   * Creates a CoreAdminHandler for this MultiCore.
   * @return a CoreAdminHandler
   */
  /**
   * 创建一个多core处理器
   * @param adminHandlerClass
   * @return
   */
  protected CoreAdminHandler createMultiCoreHandler(final String adminHandlerClass) {
    return loader.newAdminHandlerInstance(CoreContainer.this, adminHandlerClass);
  }

  /**
   * 获取一个多core处理器
   * @return
   */
  public CoreAdminHandler getMultiCoreHandler() {
    return coreAdminHandler;
  }
  
  /**
   * 获取一个分片集合处理器
   * @return
   */
  public CollectionsHandler getCollectionsHandler() {
    return collectionsHandler;
  }
  
  /**
   * 获取一个信息处理器
   * @return
   */
  public InfoHandler getInfoHandler() {
    return infoHandler;
  }
  
  /**
   * the default core name, or null if there is no default core name
   */
  /**
   * 获取一个默认的core名称
   * @return
   */
  public String getDefaultCoreName() {
    return cfg.getDefaultCoreName();
  }
  
  // all of the following properties aren't synchronized
  // but this should be OK since they normally won't be changed rapidly
  @Deprecated
  /**
   * 是否是持久的
   * @return
   */
  public boolean isPersistent() {
    return cfg.isPersistent();
  }
  
  public String getAdminPath() {
    return cfg.getAdminPath();
  }

  /**
   * Gets the alternate path for multicore handling:
   * This is used in case there is a registered unnamed core (aka name is "") to
   * declare an alternate way of accessing named cores.
   * This can also be used in a pseudo single-core environment so admins can prepare
   * a new version before swapping.
   */
  public String getManagementPath() {
    return cfg.getManagementPath();
  }

  public LogWatcher getLogging() {
    return logging;
  }

  /**
   * Determines whether the core is already loaded or not but does NOT load the core
   *
   */
  /**
   * 检查一个core是不是加载了
   * @param name
   * @return
   */
  public boolean isLoaded(String name) {
    return solrCores.isLoaded(name);
  }

  /**
   * 是否已经加载没有等待关闭
   * @param name
   * @return
   */
  public boolean isLoadedNotPendingClose(String name) {
    return solrCores.isLoadedNotPendingClose(name);
  }

  /**
   * Gets a solr core descriptor for a core that is not loaded. Note that if the caller calls this on a
   * loaded core, the unloaded descriptor will be returned.
   *
   * @param cname - name of the unloaded core descriptor to load. NOTE:
   * @return a coreDescriptor. May return null
   */
  public CoreDescriptor getUnloadedCoreDescriptor(String cname) {
    return solrCores.getUnloadedCoreDescriptor(cname);
  }

  /**
   * 将core的配置描述对象持久化到zookeeper中
   * @param p
   */
  public void preRegisterInZk(final CoreDescriptor p) {
    zkSys.getZkController().preRegister(p);
  }

  /**
   * 获取到solr.xml的目录
   * @return
   */
  public String getSolrHome() {
    return solrHome;
  }

  /**
   * zookeeper是否可用
   * @return
   */
  public boolean isZooKeeperAware() {
    return zkSys.getZkController() != null;
  }
  
  /**
   * 获取zookeeper的处理器
   * @return
   */
  public ZkController getZkController() {
    return zkSys.getZkController();
  }
  
  /**
   * 是否是共享的schema
   * @return
   */
  public boolean isShareSchema() {
    return shareSchema;
  }
  
  /**
   * 获取分片处理器工厂
   */
  /** The default ShardHandlerFactory used to communicate with other solr instances */
  public ShardHandlerFactory getShardHandlerFactory() {
    return shardHandlerFactory;
  }
  
  /**
   * 获取更新处理器
   * @return
   */
  public ExecutorService getUpdateExecutor() {
    return updateExecutor;
  }
  
  // Just to tidy up the code where it did this in-line.
  /**
   * 记录并抛出异常
   * @param name
   * @param msg
   * @param ex
   * @return
   */
  private SolrException recordAndThrow(String name, String msg, Exception ex) {
    synchronized (coreInitFailures) {
      coreInitFailures.remove(name);
      coreInitFailures.put(name, ex);
    }
    log.error(msg, ex);
    return new SolrException(ErrorCode.SERVER_ERROR, msg, ex);
  }
  
  
  String getCoreToOrigName(SolrCore core) {
    return solrCores.getCoreToOrigName(core);
  }
  

}

/**
 * 内部类，用于后台关闭？
 */
class CloserThread extends Thread {
  
  //core容器
  CoreContainer container;
  //SolrCores对象
  SolrCores solrCores;
  //solr.xml对应的对象
  ConfigSolr cfg;

  /**
   * 构造方法
   * @param container
   * @param solrCores
   * @param cfg
   */
  CloserThread(CoreContainer container, SolrCores solrCores, ConfigSolr cfg) {
    this.container = container;
    this.solrCores = solrCores;
    this.cfg = cfg;
  }

  // It's important that this be the _only_ thread removing things from pendingDynamicCloses!
  // This is single-threaded, but I tried a multi-threaded approach and didn't see any performance gains, so
  // there's no good justification for the complexity. I suspect that the locking on things like DefaultSolrCoreState
  // essentially create a single-threaded process anyway.
  @Override
  public void run() {
    //core的容器如果没有被关闭就继续
    while (! container.isShutDown()) {
      synchronized (solrCores.getModifyLock()) { // need this so we can wait and be awoken.
        try {
          solrCores.getModifyLock().wait();
        } catch (InterruptedException e) {
          // Well, if we've been told to stop, we will. Otherwise, continue on and check to see if there are
          // any cores to close.
        }
      }
      for (SolrCore removeMe = solrCores.getCoreToClose();
           removeMe != null && !container.isShutDown();
           removeMe = solrCores.getCoreToClose()) {
        try {
          removeMe.close();
        } finally {
          solrCores.removeFromPendingOps(removeMe.getName());
        }
      }
    }
  }
  
}
