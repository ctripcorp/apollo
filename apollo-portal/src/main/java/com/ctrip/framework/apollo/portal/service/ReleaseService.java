package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.KVEntity;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseBO;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceGrayDelReleaseModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceReleaseModel;
import com.ctrip.framework.apollo.portal.entity.vo.ReleaseCompareResult;
import com.ctrip.framework.apollo.portal.enums.ChangeType;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 发布 Service
 */
@Service
public class ReleaseService {

  private static final Gson GSON = new Gson();

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.ReleaseAPI releaseAPI;

  public ReleaseService(final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.ReleaseAPI releaseAPI) {
    this.userInfoHolder = userInfoHolder;
    this.releaseAPI = releaseAPI;
  }

  /**
   * 发布
   *
   * @param model 名称空间发布model
   * @return 发布信息
   */
  public ReleaseDTO publish(NamespaceReleaseModel model) {
    Env env = model.getEnv();
    boolean isEmergencyPublish = model.getIsEmergencyPublish();
    String appId = model.getAppId();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();
    String releaseBy = StringUtils.isBlank(model.getReleasedBy()) ?
        userInfoHolder.getUser().getUserId() : model.getReleasedBy();

    // 调用 Admin Service API ，发布名称空间配置。
    ReleaseDTO releaseDTO = releaseAPI.createRelease(appId, env, clusterName, namespaceName,
        model.getReleaseTitle(), model.getReleaseComment(), releaseBy, isEmergencyPublish);

    Tracer.logEvent(TracerEventType.RELEASE_NAMESPACE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));

    return releaseDTO;
  }

  /**
   * 灰度删除发布
   *
   * @param model     名称空间灰度待删除发布信息
   * @param releaseBy 发布者
   * @return 发布的信息
   */
  public ReleaseDTO publish(NamespaceGrayDelReleaseModel model, String releaseBy) {
    Env env = model.getEnv();
    boolean isEmergencyPublish = model.getIsEmergencyPublish();
    String appId = model.getAppId();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();

    ReleaseDTO releaseDTO = releaseAPI.createGrayDeletionRelease(appId, env, clusterName,
        namespaceName, model.getReleaseTitle(), model.getReleaseComment(), releaseBy,
        isEmergencyPublish, model.getGrayDelKeys());

    Tracer.logEvent(TracerEventType.RELEASE_NAMESPACE,
        String.format("%s+%s+%s+%s", appId, env, clusterName, namespaceName));

    return releaseDTO;
  }

  /**
   * 更新并发布（合并分支属性配置项至master并且发布master）
   *
   * @param appId              应用id
   * @param env                环境
   * @param clusterName        集群名称
   * @param namespaceName      名称空间名称
   * @param releaseTitle       发布标题
   * @param releaseComment     发布备注
   * @param branchName         分支名称
   * @param deleteBranch       删除分支
   * @param releaseComment     发布备注
   * @param isEmergencyPublish 是否紧急发布
   * @param changeSets         改变的配置集
   * @return 发布信息
   */
  public ReleaseDTO updateAndPublish(String appId, Env env, String clusterName,
      String namespaceName, String releaseTitle, String releaseComment, String branchName,
      boolean isEmergencyPublish, boolean deleteBranch, ItemChangeSets changeSets) {

    return releaseAPI.updateAndPublish(appId, env, clusterName, namespaceName, releaseTitle,
        releaseComment, branchName, isEmergencyPublish, deleteBranch, changeSets);
  }

  /**
   * 获取所有发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 发布信息列表
   */
  public List<ReleaseBO> findAllReleases(String appId, Env env, String clusterName,
      String namespaceName, int page, int size) {
    List<ReleaseDTO> releaseDTOs = releaseAPI
        .findAllReleases(appId, env, clusterName, namespaceName, page, size);

    if (CollectionUtils.isEmpty(releaseDTOs)) {
      return Collections.emptyList();
    }

    List<ReleaseBO> releases = new LinkedList<>();
    for (ReleaseDTO releaseDTO : releaseDTOs) {
      ReleaseBO release = new ReleaseBO();
      release.setBaseInfo(releaseDTO);

      Set<KVEntity> kvEntities = new LinkedHashSet<>();
      Map<String, String> configurations = GSON
          .fromJson(releaseDTO.getConfigurations(), GsonType.CONFIG);
      Set<Map.Entry<String, String>> entries = configurations.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        kvEntities.add(new KVEntity(entry.getKey(), entry.getValue()));
      }
      release.setItems(kvEntities);
      //为了减少数据量
      releaseDTO.setConfigurations("");
      releases.add(release);
    }

    return releases;
  }

  /**
   * 最新的发布信息列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 最新的发布信息列表
   */
  public List<ReleaseDTO> findActiveReleases(String appId, Env env, String clusterName,
      String namespaceName, int page,
      int size) {
    return releaseAPI.findActiveReleases(appId, env, clusterName, namespaceName, page, size);
  }

  /**
   * 通过id列表找到发布信息列表
   *
   * @param env       环境
   * @param releaseId 发布id
   * @return 发布信息列表
   */
  public ReleaseDTO findReleaseById(Env env, long releaseId) {
    Set<Long> releaseIds = new HashSet<>(1);
    releaseIds.add(releaseId);
    List<ReleaseDTO> releases = findReleaseByIds(env, releaseIds);
    if (CollectionUtils.isEmpty(releases)) {
      return null;
    }
    return releases.get(0);

  }

  /**
   * 通过id列表找到发布信息列表
   *
   * @param env        环境
   * @param releaseIds 发布id列表
   * @return 发布信息列表
   */
  public List<ReleaseDTO> findReleaseByIds(Env env, Set<Long> releaseIds) {
    return releaseAPI.findReleaseByIds(env, releaseIds);
  }

  /**
   * 查询名称空间最新的发布信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 最近的发布信息
   */
  public ReleaseDTO loadLatestRelease(String appId, Env env, String clusterName,
      String namespaceName) {
    return releaseAPI.loadLatestRelease(appId, env, clusterName, namespaceName);
  }

  /**
   * 回滚
   *
   * @param env       环境
   * @param releaseId 开始的发布id
   * @param operator  操作者
   */
  public void rollback(Env env, long releaseId, String operator) {
    releaseAPI.rollback(env, releaseId, operator);
  }

  /**
   * 回滚
   *
   * @param env         环境
   * @param releaseId   开始的发布id
   * @param toReleaseId 结束的发布id
   * @param operator    操作者
   */
  public void rollbackTo(Env env, long releaseId, long toReleaseId, String operator) {
    releaseAPI.rollbackTo(env, releaseId, toReleaseId, operator);
  }

  /**
   * 比较
   *
   * @param env                环境
   * @param baseReleaseId      基发布id
   * @param toCompareReleaseId 待比较发布id
   * @return 发布信息比较结果
   */
  public ReleaseCompareResult compare(Env env, long baseReleaseId, long toCompareReleaseId) {

    ReleaseDTO baseRelease = null;
    ReleaseDTO toCompareRelease = null;
    if (baseReleaseId != 0) {
      baseRelease = releaseAPI.loadRelease(env, baseReleaseId);
    }

    if (toCompareReleaseId != 0) {
      toCompareRelease = releaseAPI.loadRelease(env, toCompareReleaseId);
    }

    return compare(baseRelease, toCompareRelease);
  }

  /**
   * 比较
   *
   * @param baseRelease      基发布信息
   * @param toCompareRelease 待比较发布信息
   * @return 发布信息比较结果
   */
  public ReleaseCompareResult compare(ReleaseDTO baseRelease, ReleaseDTO toCompareRelease) {
    Map<String, String> baseReleaseConfiguration = baseRelease == null ? new HashMap<>() :
        GSON.fromJson(baseRelease.getConfigurations(), GsonType.CONFIG);
    Map<String, String> toCompareReleaseConfiguration = toCompareRelease == null ? new HashMap<>() :
        GSON.fromJson(toCompareRelease.getConfigurations(),
            GsonType.CONFIG);

    ReleaseCompareResult compareResult = new ReleaseCompareResult();

    for (Map.Entry<String, String> entry : baseReleaseConfiguration.entrySet()) {
      String key = entry.getKey();
      String firstValue = entry.getValue();
      String secondValue = toCompareReleaseConfiguration.get(key);
      // 最后一次没有，说明删除了
      if (secondValue == null) {
        compareResult.addEntityPair(ChangeType.DELETED, new KVEntity(key, firstValue),
            new KVEntity(key, null));
      } else if (!Objects.equal(firstValue, secondValue)) {
        // 不相等说明修改了
        compareResult.addEntityPair(ChangeType.MODIFIED, new KVEntity(key, firstValue),
            new KVEntity(key, secondValue));
      }

    }

    for (Map.Entry<String, String> entry : toCompareReleaseConfiguration.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      // 不存在，说明是新增的数据
      if (baseReleaseConfiguration.get(key) == null) {
        compareResult.addEntityPair(ChangeType.ADDED, new KVEntity(key, ""),
            new KVEntity(key, value));
      }

    }

    return compareResult;
  }
}
