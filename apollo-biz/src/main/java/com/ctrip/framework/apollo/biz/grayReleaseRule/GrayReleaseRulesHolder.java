package com.ctrip.framework.apollo.biz.grayReleaseRule;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.biz.entity.GrayReleaseRule;
import com.ctrip.framework.apollo.biz.entity.ReleaseMessage;
import com.ctrip.framework.apollo.biz.message.ReleaseMessageListener;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.repository.GrayReleaseRuleRepository;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.dto.GrayReleaseRuleItemDTO;
import com.ctrip.framework.apollo.common.utils.GrayReleaseRuleItemTransformer;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * 灰度发布规则持有者（灰度规则变化监听器）
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class GrayReleaseRulesHolder implements ReleaseMessageListener, InitializingBean {

  /**
   * 字符串追加器
   */
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  /**
   * 字符串分割器
   */
  private static final Splitter STRING_SPLITTER = Splitter.on(ConfigConsts
      .CLUSTER_NAMESPACE_SEPARATOR).omitEmptyStrings();

  @Autowired
  private GrayReleaseRuleRepository grayReleaseRuleRepository;
  @Autowired
  private BizConfig bizConfig;

  /**
   * 灰度发布规则扫描间隔值
   */
  @Getter
  private int databaseScanInterval;
  private ScheduledExecutorService executorService;

  /**
   * 组装灰度发布规则缓存<灰度发布规则id,灰度发布规则缓存>（存储configAppId + configCluster + configNamespace ->
   * GrayReleaseRuleCache)
   */
  private Multimap<String, GrayReleaseRuleCache> grayReleaseRuleCache;

  /**
   * 灰度发布规则缓存<灰度发布规则key，规则id>(规则id逆序)<(clientAppId+clientNamespace+ip) -> ruleId>
   */
  private Multimap<String, Long> reversedGrayReleaseRuleCache;
  /**
   * 自动递增的版本号，表示示规则的年龄
   */
  private AtomicLong loadVersion;

  /**
   * 初始化
   */
  public GrayReleaseRulesHolder() {
    loadVersion = new AtomicLong();
    grayReleaseRuleCache = Multimaps.synchronizedSetMultimap(
        TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural()));
    reversedGrayReleaseRuleCache = Multimaps.synchronizedSetMultimap(
        TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural()));
    executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory
        .create("GrayReleaseRulesHolder", true));
  }

  @Override
  public void afterPropertiesSet() {
    // 填充灰度发布规则扫描间隔值
    populateDataBaseInterval();
    //定期扫描规则,第一次强制同步加载
    periodicScanRules();
    executorService.scheduleWithFixedDelay(this::periodicScanRules,
        getDatabaseScanInterval(), getDatabaseScanInterval(), getDatabaseScanTimeUnit()
    );
  }

  @Override
  public void handleMessage(ReleaseMessage message, String channel) {
    log.info("message received - channel: {}, message: {}", channel, message);
    String releaseMessage = message.getMessage();
    // topic不匹配或者发布信息为空，退出
    if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || Strings.isNullOrEmpty(releaseMessage)) {
      return;
    }
    List<String> keys = STRING_SPLITTER.splitToList(releaseMessage);
    // key由appId+cluster+namespace组成，不为3表示key非法
    if (keys.size() != 3) {
      log.error("message format invalid - {}", releaseMessage);
      return;
    }
    String appId = keys.get(0);
    String cluster = keys.get(1);
    String namespace = keys.get(2);

    // 灰度发布规则列表
    List<GrayReleaseRule> rules = grayReleaseRuleRepository
        .findByAppIdAndClusterNameAndNamespaceName(appId, cluster, namespace);
    // 合并灰度发布规则
    mergeGrayReleaseRules(rules);
  }

  /**
   * 定期扫描规则
   */
  private void periodicScanRules() {
    Transaction transaction = Tracer.newTransaction("Apollo.GrayReleaseRulesScanner",
        "scanGrayReleaseRules");
    try {
      // 增加版本
      loadVersion.incrementAndGet();
      // 扫描灰度发布规则
      scanGrayReleaseRules();
      transaction.setStatus(Transaction.SUCCESS);
    } catch (Throwable ex) {
      transaction.setStatus(ex);
      log.error("Scan gray release rule failed", ex);
    } finally {
      transaction.complete();
    }
  }

  /**
   * 获取灰度release id,从应用的缓存中获取规则
   *
   * @param clientAppId         客户端应用id
   * @param clientIp            客户端ip
   * @param configAppId         配置应用id
   * @param configCluster       配置集群
   * @param configNamespaceName 配置名称空间名称
   * @return 灰度发布id
   */
  public Long findReleaseIdFromGrayReleaseRule(String clientAppId, String clientIp, String
      configAppId, String configCluster, String configNamespaceName) {
    // 灰度发布规则key
    String key = assembleGrayReleaseRuleKey(configAppId, configCluster, configNamespaceName);
    if (!grayReleaseRuleCache.containsKey(key)) {
      return null;
    }
    // 创建一个新的列表以避免ConcurrentModificationException
    List<GrayReleaseRuleCache> rules = Lists.newArrayList(grayReleaseRuleCache.get(key));
    for (GrayReleaseRuleCache rule : rules) {
      //如果判断不为活跃的，跳过
      if (rule.getBranchStatus() != NamespaceBranchStatus.ACTIVE) {
        continue;
      }
      // 如果客户端应用id和客户端IP匹配，返回发布id
      if (rule.matches(clientAppId, clientIp)) {
        return rule.getReleaseId();
      }
    }
    return null;
  }

  /**
   * 检查客户端应用id、客户端ip和名称空间是否存在灰色发布规则。
   * <p>请注意，即使有灰色发布规则，这并不意味着它将总是加载灰色发布。因为灰色发布规则实际上适用于另一个维度——集群。</p>
   *
   * @param clientAppId   客户端应用id
   * @param clientIp      客户端ip
   * @param namespaceName 名称空间名称
   * @return 存在灰色发布规则，true,否则，false
   */
  public boolean hasGrayReleaseRule(String clientAppId, String clientIp, String namespaceName) {
    return reversedGrayReleaseRuleCache.containsKey(assembleReversedGrayReleaseRuleKey(clientAppId,
        namespaceName, clientIp)) || reversedGrayReleaseRuleCache.containsKey
        (assembleReversedGrayReleaseRuleKey(clientAppId, namespaceName, GrayReleaseRuleItemDTO
            .ALL_IP));
  }

  /**
   * 扫描灰度发布规则
   */
  private void scanGrayReleaseRules() {
    // 扫描最大的id
    long maxIdScanned = 0;
    // 有没有更多的元素
    boolean hasMore = true;

    while (hasMore && !Thread.currentThread().isInterrupted()) {
      // 灰度规则信息列表
      List<GrayReleaseRule> grayReleaseRules = grayReleaseRuleRepository
          .findFirst500ByIdGreaterThanOrderByIdAsc(maxIdScanned);
      if (CollectionUtils.isEmpty(grayReleaseRules)) {
        break;
      }
      // 合并灰度发布规则
      mergeGrayReleaseRules(grayReleaseRules);
      // 已经扫描的规则大小
      int rulesScanned = grayReleaseRules.size();
      maxIdScanned = grayReleaseRules.get(rulesScanned - 1).getId();
      // 只要批量大小为500就表示有更多的元素
      hasMore = rulesScanned == 500;
    }
  }

  /**
   * 合并灰度发布规则列表
   *
   * @param grayReleaseRules 灰度发布规则列表
   */
  private void mergeGrayReleaseRules(List<GrayReleaseRule> grayReleaseRules) {
    if (CollectionUtils.isEmpty(grayReleaseRules)) {
      return;
    }
    for (GrayReleaseRule grayReleaseRule : grayReleaseRules) {
      if (grayReleaseRule.getReleaseId() == null || grayReleaseRule.getReleaseId() == 0) {
        // 过滤规则没有发布id，即从未发布
        continue;
      }
      // 组装灰度发布规则key
      String key = assembleGrayReleaseRuleKey(grayReleaseRule.getAppId(), grayReleaseRule
          .getClusterName(), grayReleaseRule.getNamespaceName());
      // 创建一个新的列表以避免ConcurrentModificationException
      // 灰度发布规则列表
      List<GrayReleaseRuleCache> rules = Lists.newArrayList(grayReleaseRuleCache.get(key));
      GrayReleaseRuleCache oldRule = null;
      for (GrayReleaseRuleCache ruleCache : rules) {
        if (ruleCache.getBranchName().equals(grayReleaseRule.getBranchName())) {
          oldRule = ruleCache;
          break;
        }
      }

      //如果旧规则为null，而新规则的分支状态为不活动，则忽略
      if (oldRule == null && grayReleaseRule.getBranchStatus() != NamespaceBranchStatus.ACTIVE) {
        continue;
      }

      // 使用id比较来避免同步
      if (oldRule == null || grayReleaseRule.getId() > oldRule.getRuleId()) {
        // 添加缓存
        addCache(key, transformRuleToRuleCache(grayReleaseRule));
        // 旧规则不为空就删除
        if (oldRule != null) {
          removeCache(key, oldRule);
        }
      } else {
        // 分支状态为活跃时
        if (oldRule.getBranchStatus() == NamespaceBranchStatus.ACTIVE) {
          //更新加载的版本
          oldRule.setLoadVersion(loadVersion.get());
        } else if ((loadVersion.get() - oldRule.getLoadVersion()) > 1) {
          // 在2个更新周期后删除过时的非活动分支规则
          removeCache(key, oldRule);
        }
      }
    }
  }

  /**
   * 添加缓存
   *
   * @param key       灰度发布规则缓存Key
   * @param ruleCache 灰度发布规则缓存
   */
  private void addCache(String key, GrayReleaseRuleCache ruleCache) {
    if (ruleCache.getBranchStatus() == NamespaceBranchStatus.ACTIVE) {
      for (GrayReleaseRuleItemDTO ruleItemDTO : ruleCache.getRuleItems()) {
        for (String clientIp : ruleItemDTO.getClientIpList()) {
          reversedGrayReleaseRuleCache.put(assembleReversedGrayReleaseRuleKey(ruleItemDTO
              .getClientAppId(), ruleCache.getNamespaceName(), clientIp), ruleCache.getRuleId());
        }
      }
    }
    grayReleaseRuleCache.put(key, ruleCache);
  }

  /**
   * 移除缓存
   *
   * @param key       灰度发布规则key
   * @param ruleCache 灰度发布规则缓存
   */
  private void removeCache(String key, GrayReleaseRuleCache ruleCache) {
    // 灰度发布规则缓存移除key
    grayReleaseRuleCache.remove(key, ruleCache);
    // 移除灰度发布规则缓存
    for (GrayReleaseRuleItemDTO ruleItemDTO : ruleCache.getRuleItems()) {
      for (String clientIp : ruleItemDTO.getClientIpList()) {
        reversedGrayReleaseRuleCache.remove(assembleReversedGrayReleaseRuleKey(ruleItemDTO
            .getClientAppId(), ruleCache.getNamespaceName(), clientIp), ruleCache.getRuleId());
      }
    }
  }

  /**
   * grayReleaseRule转换为 GrayReleaseRuleCache
   *
   * @param grayReleaseRule 灰度发布规则信息
   * @return 规则发布规则缓存
   */
  private GrayReleaseRuleCache transformRuleToRuleCache(GrayReleaseRule grayReleaseRule) {
    //将grayReleaseRule转换为 GrayReleaseRuleItemDTO
    Set<GrayReleaseRuleItemDTO> ruleItems;
    try {
      ruleItems = GrayReleaseRuleItemTransformer.batchTransformFromJSON(grayReleaseRule.getRules());
    } catch (Throwable ex) {
      ruleItems = Sets.newHashSet();
      Tracer.logError(ex);
      log.error("parse rule for gray release rule {} failed", grayReleaseRule.getId(), ex);
    }

    GrayReleaseRuleCache ruleCache = new GrayReleaseRuleCache(grayReleaseRule.getId(),
        grayReleaseRule.getBranchName(), grayReleaseRule.getNamespaceName(), grayReleaseRule
        .getReleaseId(), loadVersion.get(), grayReleaseRule.getBranchStatus(), ruleItems);
    // 返回灰度发布规则缓存
    return ruleCache;
  }

  /**
   * 填充灰度发布规则扫描间隔值
   */
  private void populateDataBaseInterval() {
    databaseScanInterval = bizConfig.grayReleaseRuleScanInterval();
  }

  /**
   * 灰度发布规则扫描间隔值时间单位
   *
   * @return 灰度发布规则扫描间隔值时间单位（秒）
   */
  private TimeUnit getDatabaseScanTimeUnit() {
    return TimeUnit.SECONDS;
  }

  /**
   * 组装灰度发布规则key
   *
   * @param configAppId         配置应用id
   * @param configCluster       配置集群
   * @param configNamespaceName 配置名称空间名称
   * @return 灰度发布规则key
   */
  private String assembleGrayReleaseRuleKey(String configAppId, String configCluster, String
      configNamespaceName) {
    return STRING_JOINER.join(configAppId, configCluster, configNamespaceName);
  }

  /**
   * 组装灰度发布规则key
   *
   * @param clientAppId         客户端应用id
   * @param clientNamespaceName 客户端名称空间名称
   * @param clientIp            客户端id
   * @return 灰度发布规则key
   */
  private String assembleReversedGrayReleaseRuleKey(String clientAppId, String
      clientNamespaceName, String clientIp) {
    return STRING_JOINER.join(clientAppId, clientNamespaceName, clientIp);
  }

}
