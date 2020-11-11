package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.biz.entity.Instance;
import com.ctrip.framework.apollo.biz.entity.InstanceConfig;
import com.ctrip.framework.apollo.biz.service.InstanceService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class InstanceConfigAuditUtil implements InitializingBean {

  /**
   * {@link #audits} 配置审核实例队列最大大小
   */
  private static final int INSTANCE_CONFIG_AUDIT_MAX_SIZE = 10000;
  /**
   * {@link #instanceCache} 实例缓存最大大小
   */
  private static final int INSTANCE_CACHE_MAX_SIZE = 50000;
  /**
   * {@link #instanceConfigReleaseKeyCache} 实例配置缓存最大大小
   */
  private static final int INSTANCE_CONFIG_CACHE_MAX_SIZE = 50000;
  private static final long OFFER_TIME_LAST_MODIFIED_TIME_THRESHOLD_IN_MILLI = TimeUnit.MINUTES
      .toMillis(10);//10 minutes
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  /**
   * ExecutorService 对象。队列大小为 1 。
   */
  private final ExecutorService auditExecutorService;
  /**
   * 是否停止
   */
  private final AtomicBoolean auditStopped;
  /**
   * 配置审核实例队列
   */
  private BlockingQueue<InstanceConfigAuditModel> audits = Queues.newLinkedBlockingQueue
      (INSTANCE_CONFIG_AUDIT_MAX_SIZE);
  /**
   * Instance的Id缓存
   * <p>
   * KEY：{@link #assembleInstanceKey(String, String, String, String)} VALUE：{@link
   * Instance#getId()}
   */
  private Cache<String, Long> instanceCache;
  /**
   * InstanceConfig的发布Key的缓存
   * <p>
   * KEY：{@link #assembleInstanceConfigKey(long, String, String)} VALUE：{@link
   * InstanceConfig#getId()}
   */
  private Cache<String, String> instanceConfigReleaseKeyCache;
  /**
   * 实例Service
   */
  private final InstanceService instanceService;

  /**
   * 构造InstanceConfigAuditUtil
   *
   * @param instanceService 实例Service
   */
  public InstanceConfigAuditUtil(final InstanceService instanceService) {
    this.instanceService = instanceService;
    auditExecutorService = Executors.newSingleThreadExecutor(
        ApolloThreadFactory.create("InstanceConfigAuditUtil", true));
    auditStopped = new AtomicBoolean(false);
    instanceCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(INSTANCE_CACHE_MAX_SIZE).build();
    instanceConfigReleaseKeyCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(INSTANCE_CONFIG_CACHE_MAX_SIZE).build();
  }

  /**
   * 添加审计
   *
   * @param appId             应用id
   * @param clusterName       集群名称
   * @param dataCenter        数据中心
   * @param ip                客户端ip
   * @param configAppId       配置应用id
   * @param configClusterName 配置集群名称
   * @param configNamespace   配置名称空间
   * @param releaseKey        发布key
   * @return true, 添加成功，否则，false
   */
  public boolean audit(String appId, String clusterName, String dataCenter, String ip,
      String configAppId, String configClusterName, String configNamespace, String releaseKey) {
    // 添加到队列中
    return this.audits.offer(new InstanceConfigAuditModel(appId, clusterName, dataCenter, ip,
        configAppId, configClusterName, configNamespace, releaseKey));
  }

  /**
   * 记录 Instance 和 InstanceConfig
   *
   * @param auditModel 审记Model
   */
  void doAudit(InstanceConfigAuditModel auditModel) {
    // 拼接 instanceCache 的 KEY
    String instanceCacheKey = assembleInstanceKey(auditModel.getAppId(), auditModel
        .getClusterName(), auditModel.getClientIp(), auditModel.getDataCenter());
    // 获得实例id
    Long instanceId = instanceCache.getIfPresent(instanceCacheKey);
    // 查询不到，从 DB 加载或者创建，并添加到缓存中
    if (instanceId == null) {
      instanceId = prepareInstanceId(auditModel);
      instanceCache.put(instanceCacheKey, instanceId);
    }

    //load instance config release key from cache, and check if release key is the same
    // 获得 instanceConfigReleaseKeyCache 的 KEY
    String instanceConfigCacheKey = assembleInstanceConfigKey(instanceId, auditModel
        .getConfigAppId(), auditModel.getConfigNamespace());
    // 获得缓存的 cacheReleaseKey
    String cacheReleaseKey = instanceConfigReleaseKeyCache.getIfPresent(instanceConfigCacheKey);

    //if release key is the same, then skip audit
    // 若相等，跳过
    if (cacheReleaseKey != null && Objects.equals(cacheReleaseKey, auditModel.getReleaseKey())) {
      return;
    }

    // 更新对应的 instanceConfigReleaseKeyCache 缓存
    instanceConfigReleaseKeyCache.put(instanceConfigCacheKey, auditModel.getReleaseKey());

    //if release key is not the same or cannot find in cache, then do audit
    // 获得 InstanceConfig 对象
    InstanceConfig instanceConfig = instanceService.findInstanceConfig(instanceId, auditModel
        .getConfigAppId(), auditModel.getConfigNamespace());

    // 若 InstanceConfig 已经存在，进行更新
    if (instanceConfig != null) {
      // ReleaseKey 发生变化
      if (!Objects.equals(instanceConfig.getReleaseKey(), auditModel.getReleaseKey())) {
        instanceConfig.setConfigClusterName(auditModel.getConfigClusterName());
        instanceConfig.setReleaseKey(auditModel.getReleaseKey());

        instanceConfig.setReleaseDeliveryTime(auditModel.getOfferTime());
      } else if (offerTimeAndLastModifiedTimeCloseEnough(auditModel.getOfferTime(),
          instanceConfig.getDataChangeLastModifiedTime())) {
        // 配置入队时间与实例最后修改时间过近，例如 Client 先请求的 Config Service A 节点，再请求 Config Service B 节点的情况
        //when releaseKey is the same, optimize to reduce writes if the record was updated not long ago
        return;
      }
      //we need to update no matter the release key is the same or not, to ensure the
      //last modified time is updated each day
      // 更新
      instanceConfig.setDataChangeLastModifiedTime(auditModel.getOfferTime());
      instanceService.updateInstanceConfig(instanceConfig);
      return;
    }

    // 若InstanceConfig不存在，创建InstanceConfig对象
    instanceConfig = new InstanceConfig();
    instanceConfig.setInstanceId(instanceId);
    instanceConfig.setConfigAppId(auditModel.getConfigAppId());
    instanceConfig.setConfigClusterName(auditModel.getConfigClusterName());
    instanceConfig.setConfigNamespaceName(auditModel.getConfigNamespace());
    instanceConfig.setReleaseKey(auditModel.getReleaseKey());
    instanceConfig.setReleaseDeliveryTime(auditModel.getOfferTime());
    instanceConfig.setDataChangeCreatedTime(auditModel.getOfferTime());

    // 保存 InstanceConfig 对象到数据库中
    try {
      instanceService.createInstanceConfig(instanceConfig);
    } catch (DataIntegrityViolationException ex) {
      //concurrent insertion, safe to ignore
    }
  }

  /**
   * 时间是否过近，仅相差 10 分钟
   *
   * @param offerTime        提交的时间
   * @param lastModifiedTime 最后修改的时间
   * @return true, 过近，否则，false
   */
  private boolean offerTimeAndLastModifiedTimeCloseEnough(Date offerTime, Date lastModifiedTime) {
    return (offerTime.getTime() - lastModifiedTime.getTime()) <
        OFFER_TIME_LAST_MODIFIED_TIME_THRESHOLD_IN_MILLI;
  }

  /**
   * 查询实例Id,查询不到，从 DB 加载或者创建，并添加到缓存中
   *
   * @param auditModel 实例配置审核Model
   * @return 实例Id
   */
  private long prepareInstanceId(InstanceConfigAuditModel auditModel) {
    // 查询 Instance 对象
    Instance instance = instanceService.findInstance(auditModel.getAppId(), auditModel
        .getClusterName(), auditModel.getDataCenter(), auditModel.getClientIp());
    // 已存在，返回实例id
    if (instance != null) {
      return instance.getId();
    }
    // 若 Instance 不存在，创建 Instance 对象
    instance = new Instance();
    instance.setAppId(auditModel.getAppId());
    instance.setClusterName(auditModel.getClusterName());
    instance.setDataCenter(auditModel.getDataCenter());
    instance.setIp(auditModel.getClientIp());

    // 保存 Instance 对象到数据库中
    try {
      return instanceService.createInstance(instance).getId();
    } catch (DataIntegrityViolationException ex) {
      // 发生唯一索引冲突，意味着已经存在，进行查询 Instance 对象，并返回
      //return the one exists
      return instanceService.findInstance(instance.getAppId(), instance.getClusterName(),
          instance.getDataCenter(), instance.getIp()).getId();
    }
  }

  @Override
  public void afterPropertiesSet() {
    // 提交任务
    auditExecutorService.submit(() -> {
      // 循环，直到停止或线程打断
      while (!auditStopped.get() && !Thread.currentThread().isInterrupted()) {
        try {
          // 获得队首 InstanceConfigAuditModel 元素，非阻塞
          InstanceConfigAuditModel model = audits.poll();
          // 若获取不到，sleep 等待 1 秒
          if (model == null) {
            TimeUnit.SECONDS.sleep(1);
            continue;
          }
          // 若获取到，记录 Instance 和 InstanceConfig
          doAudit(model);
        } catch (Throwable ex) {
          Tracer.logError(ex);
        }
      }
    });
  }

  /**
   * 拼接 instanceCache 的 KEY
   *
   * @param appId      应用id
   * @param cluster    集群
   * @param ip         客户端ip
   * @param datacenter 数据中心
   * @return 拼接后的instanceCache的KEY
   */
  private String assembleInstanceKey(String appId, String cluster, String ip, String datacenter) {
    //拼接key
    List<String> keyParts = Lists.newArrayList(appId, cluster, ip);
    if (StringUtils.isNotBlank(datacenter)) {
      keyParts.add(datacenter);
    }
    return STRING_JOINER.join(keyParts);
  }

  /**
   * 拼接实例配置Key
   *
   * @param instanceId      实例id
   * @param configAppId     配置应用id
   * @param configNamespace 配置名称空间
   * @return 拼接好的实例配置key
   */
  private String assembleInstanceConfigKey(long instanceId, String configAppId,
      String configNamespace) {
    return STRING_JOINER.join(instanceId, configAppId, configNamespace);
  }

  /**
   * 实例配置审记Model
   */
  @Getter
  public static class InstanceConfigAuditModel {

    /**
     * 应用id
     */
    private String appId;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 数据中心
     */
    private String dataCenter;
    /**
     * 客户端ip地址
     */
    private String clientIp;
    /**
     * 配置的应用id
     */
    private String configAppId;
    /**
     * 配置的集群名称
     */
    private String configClusterName;
    /**
     * 配置的名称空间
     */
    private String configNamespace;
    /**
     * 发布Key
     */
    private String releaseKey;
    /**
     * 入队时间
     */
    private Date offerTime;

    public InstanceConfigAuditModel(String appId, String clusterName, String dataCenter, String
        clientIp, String configAppId, String configClusterName, String configNamespace, String
        releaseKey) {
      this.offerTime = new Date();
      this.appId = appId;
      this.clusterName = clusterName;
      this.dataCenter = Strings.isNullOrEmpty(dataCenter) ? "" : dataCenter;
      this.clientIp = clientIp;
      this.configAppId = configAppId;
      this.configClusterName = configClusterName;
      this.configNamespace = configNamespace;
      this.releaseKey = releaseKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      InstanceConfigAuditModel model = (InstanceConfigAuditModel) o;
      return Objects.equals(appId, model.appId) &&
          Objects.equals(clusterName, model.clusterName) &&
          Objects.equals(dataCenter, model.dataCenter) &&
          Objects.equals(clientIp, model.clientIp) &&
          Objects.equals(configAppId, model.configAppId) &&
          Objects.equals(configClusterName, model.configClusterName) &&
          Objects.equals(configNamespace, model.configNamespace) &&
          Objects.equals(releaseKey, model.releaseKey);
    }

    @Override
    public int hashCode() {
      return Objects.hash(appId, clusterName, dataCenter, clientIp, configAppId, configClusterName,
          configNamespace,
          releaseKey);
    }
  }
}
