package com.ctrip.framework.apollo.adminservice.controller;

import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import com.ctrip.framework.apollo.biz.service.ReleaseHistoryService;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseHistoryDTO;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布历史记录 Controller
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
public class ReleaseHistoryController {

  private static final Gson GSON = new Gson();

  private Type configurationTypeReference = new TypeToken<Map<String, Object>>() {
  }.getType();

  private final ReleaseHistoryService releaseHistoryService;

  public ReleaseHistoryController(final ReleaseHistoryService releaseHistoryService) {
    this.releaseHistoryService = releaseHistoryService;
  }

  /**
   * 通过名称空间查找发布历史记录
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param pageable      分页对象
   * @return 发布历史记录分页信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories")
  public PageDTO<ReleaseHistoryDTO> findReleaseHistoriesByNamespace(@PathVariable String appId,
      @PathVariable String clusterName, @PathVariable String namespaceName, Pageable pageable) {

    Page<ReleaseHistory> result = releaseHistoryService.findReleaseHistoriesByNamespace(appId,
        clusterName, namespaceName, pageable);
    return transform2PageDTO(result, pageable);
  }

  /**
   * 根据发布id和操作查询发布历史信息
   *
   * @param releaseId 发布id
   * @param operation 发布操作
   * @param pageable  分页对象
   * @return 发布历史记录分页信息
   */
  @GetMapping("/releases/histories/by_release_id_and_operation")
  public PageDTO<ReleaseHistoryDTO> findReleaseHistoryByReleaseIdAndOperation(
      @RequestParam("releaseId") long releaseId, @RequestParam("operation") int operation,
      Pageable pageable) {

    Page<ReleaseHistory> result = releaseHistoryService.findByReleaseIdAndOperation(releaseId,
        operation, pageable);
    return transform2PageDTO(result, pageable);
  }

  /**
   * 查询指定之前的发布id和指定发布操作的发布历史信息
   *
   * @param previousReleaseId 之前的发布id
   * @param operation         发布操作
   * @param pageable          分页对象
   * @return 发布历史记录分页信息
   */
  @GetMapping("/releases/histories/by_previous_release_id_and_operation")
  public PageDTO<ReleaseHistoryDTO> findReleaseHistoryByPreviousReleaseIdAndOperation(
      @RequestParam("previousReleaseId") long previousReleaseId,
      @RequestParam("operation") int operation, Pageable pageable) {

    Page<ReleaseHistory> result = releaseHistoryService
        .findByPreviousReleaseIdAndOperation(previousReleaseId, operation, pageable);
    return transform2PageDTO(result, pageable);

  }

  /**
   * 转换分页对象（ReleaseHistory -> ReleaseHistoryDTO）
   *
   * @param releaseHistoriesPage 发布历史分页信息
   * @param pageable             分页对象
   * @return 发布历史记录Dto分页信息
   */
  private PageDTO<ReleaseHistoryDTO> transform2PageDTO(Page<ReleaseHistory> releaseHistoriesPage,
      Pageable pageable) {
    if (!releaseHistoriesPage.hasContent()) {
      return null;
    }

    List<ReleaseHistory> releaseHistories = releaseHistoriesPage.getContent();
    List<ReleaseHistoryDTO> releaseHistoryDTOs = new ArrayList<>(releaseHistories.size());
    for (ReleaseHistory releaseHistory : releaseHistories) {
      releaseHistoryDTOs.add(transformReleaseHistory2DTO(releaseHistory));
    }

    return new PageDTO<>(releaseHistoryDTOs, pageable, releaseHistoriesPage.getTotalElements());
  }

  /**
   * 转换实体对象（ReleaseHistory -> ReleaseHistoryDTO）
   *
   * @param releaseHistory 发布历史信息
   * @return 发布历史记录Dto信息
   */
  private ReleaseHistoryDTO transformReleaseHistory2DTO(ReleaseHistory releaseHistory) {
    ReleaseHistoryDTO dto = new ReleaseHistoryDTO();
    BeanUtils.copyProperties(releaseHistory, dto, "operationContext");
    dto.setOperationContext(GSON.fromJson(releaseHistory.getOperationContext(),
        configurationTypeReference));
    return dto;
  }
}
