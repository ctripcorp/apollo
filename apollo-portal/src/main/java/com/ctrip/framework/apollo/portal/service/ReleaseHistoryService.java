package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.constants.GsonType;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseHistoryDTO;
import com.ctrip.framework.apollo.common.entity.EntityPair;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.RelativeDateFormat;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ReleaseHistoryService {

  private final static Gson GSON = new Gson();

  private final AdminServiceAPI.ReleaseHistoryAPI releaseHistoryAPI;
  private final ReleaseService releaseService;

  public ReleaseHistoryService(final AdminServiceAPI.ReleaseHistoryAPI releaseHistoryAPI,
      final ReleaseService releaseService) {
    this.releaseHistoryAPI = releaseHistoryAPI;
    this.releaseService = releaseService;
  }


  /**
   * 通过发布id和操作者获取最新的发布历史业务信息
   *
   * @param env       环境
   * @param releaseId 发布id
   * @param operation 操作者
   * @return 最新的发布历史业务信息
   */
  public ReleaseHistoryBO findLatestByReleaseIdAndOperation(Env env, long releaseId,
      int operation) {
    PageDTO<ReleaseHistoryDTO> pageDTO = releaseHistoryAPI.findByReleaseIdAndOperation(env,
        releaseId, operation, 0, 1);
    if (pageDTO != null && pageDTO.hasContent()) {
      ReleaseHistoryDTO releaseHistory = pageDTO.getContent().get(0);
      ReleaseDTO release = releaseService.findReleaseById(env, releaseHistory.getReleaseId());
      return transformReleaseHistoryDTO2BO(releaseHistory, release);
    }

    return null;
  }

  /**
   * 通过环境，前次的发布id和操作者获取最新的发布历史业务信息
   *
   * @param env               环境
   * @param previousReleaseId 前一次的发布id
   * @param operation         操作者
   * @return 最新的发布历史业务信息
   */
  public ReleaseHistoryBO findLatestByPreviousReleaseIdAndOperation(Env env, long previousReleaseId,
      int operation) {
    PageDTO<ReleaseHistoryDTO> pageDTO = releaseHistoryAPI
        .findByPreviousReleaseIdAndOperation(env, previousReleaseId, operation, 0, 1);
    if (pageDTO != null && pageDTO.hasContent()) {
      ReleaseHistoryDTO releaseHistory = pageDTO.getContent().get(0);
      ReleaseDTO release = releaseService.findReleaseById(env, releaseHistory.getReleaseId());
      return transformReleaseHistoryDTO2BO(releaseHistory, release);
    }

    return null;
  }

  /**
   * 获取名称空间发布历史
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          页码
   * @param size          页面大小
   * @return 名称空间发布历史
   */
  public List<ReleaseHistoryBO> findNamespaceReleaseHistory(String appId, Env env,
      String clusterName, String namespaceName, int page, int size) {
    PageDTO<ReleaseHistoryDTO> result = releaseHistoryAPI.findReleaseHistoriesByNamespace(appId,
        env, clusterName, namespaceName, page, size);
    if (result == null || !result.hasContent()) {
      return Collections.emptyList();
    }

    List<ReleaseHistoryDTO> content = result.getContent();
    Set<Long> releaseIds = new HashSet<>();
    for (ReleaseHistoryDTO releaseHistoryDTO : content) {
      long releaseId = releaseHistoryDTO.getReleaseId();
      if (releaseId != 0) {
        releaseIds.add(releaseId);
      }
    }

    List<ReleaseDTO> releases = releaseService.findReleaseByIds(env, releaseIds);
    return transformReleaseHistoryDTO2BO(content, releases);
  }

  /**
   * ReleaseHistoryDTO列表 转 ReleaseHistoryBO列表
   *
   * @param source   发布历史列表
   * @param releases 发布信息列表
   * @return 转换后的ReleaseHistoryBO列表
   */
  private List<ReleaseHistoryBO> transformReleaseHistoryDTO2BO(List<ReleaseHistoryDTO> source,
      List<ReleaseDTO> releases) {

    Map<Long, ReleaseDTO> releasesMap = BeanUtils.mapByKey("id", releases);

    List<ReleaseHistoryBO> bos = new ArrayList<>(source.size());
    for (ReleaseHistoryDTO dto : source) {
      ReleaseDTO release = releasesMap.get(dto.getReleaseId());
      bos.add(transformReleaseHistoryDTO2BO(dto, release));
    }

    return bos;
  }

  /**
   * ReleaseHistoryDTO列表 转 ReleaseHistoryBO
   *
   * @param dto     发布历史
   * @param release 发布信息
   * @return 转换后的ReleaseHistoryBO
   */
  private ReleaseHistoryBO transformReleaseHistoryDTO2BO(ReleaseHistoryDTO dto,
      ReleaseDTO release) {
    ReleaseHistoryBO bo = new ReleaseHistoryBO();
    bo.setId(dto.getId());
    bo.setAppId(dto.getAppId());
    bo.setClusterName(dto.getClusterName());
    bo.setNamespaceName(dto.getNamespaceName());
    bo.setBranchName(dto.getBranchName());
    bo.setReleaseId(dto.getReleaseId());
    bo.setPreviousReleaseId(dto.getPreviousReleaseId());
    bo.setOperator(dto.getDataChangeCreatedBy());
    bo.setOperation(dto.getOperation());
    Date releaseTime = dto.getDataChangeLastModifiedTime();
    bo.setReleaseTime(releaseTime);
    bo.setReleaseTimeFormatted(RelativeDateFormat.format(releaseTime));
    bo.setOperationContext(dto.getOperationContext());
    //set release info
    setReleaseInfoToReleaseHistoryBO(bo, release);

    return bo;
  }

  /**
   * 设置发布信息至发布历史业务对象中
   *
   * @param bo      发布历史业务对象
   * @param release 发布信息
   */
  private void setReleaseInfoToReleaseHistoryBO(ReleaseHistoryBO bo, ReleaseDTO release) {
    if (release != null) {
      bo.setReleaseTitle(release.getName());
      bo.setReleaseComment(release.getComment());
      bo.setReleaseAbandoned(release.getIsAbandoned());

      // 配置项列表
      Map<String, String> configuration = GSON.fromJson(release.getConfigurations(),
          GsonType.CONFIG);
      // 添加
      List<EntityPair<String>> items = new ArrayList<>(configuration.size());
      configuration.forEach((key, value) -> {
        EntityPair<String> entityPair = new EntityPair<>(key, value);
        items.add(entityPair);
      });
      bo.setConfiguration(items);

    } else {
      // 没有信息
      bo.setReleaseTitle("no release information");
      bo.setConfiguration(null);
    }
  }
}
