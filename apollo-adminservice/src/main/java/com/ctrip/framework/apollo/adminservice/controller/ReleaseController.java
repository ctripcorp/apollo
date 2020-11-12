package com.ctrip.framework.apollo.adminservice.controller;


import com.ctrip.framework.apollo.biz.entity.Namespace;
import com.ctrip.framework.apollo.biz.entity.Release;
import com.ctrip.framework.apollo.biz.message.MessageSender;
import com.ctrip.framework.apollo.biz.message.Topics;
import com.ctrip.framework.apollo.biz.service.NamespaceBranchService;
import com.ctrip.framework.apollo.biz.service.NamespaceService;
import com.ctrip.framework.apollo.biz.service.ReleaseService;
import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.common.constants.NamespaceBranchStatus;
import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.exception.NotFoundException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布 Controller
 */
@RestController
public class ReleaseController {

  /**
   * 发布分隔符，省略了空字符串、空格
   */
  private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings()
      .trimResults();

  private final ReleaseService releaseService;
  private final NamespaceService namespaceService;
  private final MessageSender messageSender;
  private final NamespaceBranchService namespaceBranchService;

  public ReleaseController(
      final ReleaseService releaseService,
      final NamespaceService namespaceService,
      final MessageSender messageSender,
      final NamespaceBranchService namespaceBranchService) {
    this.releaseService = releaseService;
    this.namespaceService = namespaceService;
    this.messageSender = messageSender;
    this.namespaceBranchService = namespaceBranchService;
  }

  /**
   * 根据发布id查询发布信息
   *
   * @param releaseId 发布id
   * @return 指定id的发布信息
   */
  @GetMapping("/releases/{releaseId}")
  public ReleaseDTO get(@PathVariable("releaseId") long releaseId) {
    Release release = releaseService.findOne(releaseId);
    if (release == null) {
      throw new NotFoundException(String.format("release not found for %s", releaseId));
    }
    return BeanUtils.transform(ReleaseDTO.class, release);
  }

  /**
   * 通过id列表找到发布信息列表
   *
   * @param releaseIds 发布id列表
   * @return 发布信息列表
   */
  @GetMapping("/releases")
  public List<ReleaseDTO> findReleaseByIds(@RequestParam("releaseIds") String releaseIds) {
    Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong)
        .collect(Collectors.toSet());

    List<Release> releases = releaseService.findByReleaseIds(releaseIdSet);
    return BeanUtils.batchTransform(ReleaseDTO.class, releases);
  }

  /**
   * 获取所有发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 发布信息列表
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/all")
  public List<ReleaseDTO> findAllReleases(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, Pageable page) {
    List<Release> releases = releaseService
        .findAllReleases(appId, clusterName, namespaceName, page);
    return BeanUtils.batchTransform(ReleaseDTO.class, releases);
  }


  /**
   * 通过应用id、集群名称、名称空间名称按id降序查询未废弃的发布信息列表
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param page          分页对象
   * @return 未废弃的发布信息列表
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/active")
  public List<ReleaseDTO> findActiveReleases(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, Pageable page) {
    List<Release> releases = releaseService
        .findActiveReleases(appId, clusterName, namespaceName, page);
    return BeanUtils.batchTransform(ReleaseDTO.class, releases);
  }

  /**
   * 查询名称空间最近的发布信息
   *
   * @param appId         应用id
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @return 最近的发布信息
   */
  @GetMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases/latest")
  public ReleaseDTO getLatest(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName) {
    Release release = releaseService.findLatestActiveRelease(appId, clusterName, namespaceName);
    return BeanUtils.transform(ReleaseDTO.class, release);
  }

  /**
   * 发布
   *
   * @param appId              应用id
   * @param clusterName        集群名称
   * @param namespaceName      名称空间名称
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param operator           操作者
   * @param isEmergencyPublish 是否紧急发布
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/releases")
  public ReleaseDTO publish(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName, @RequestParam("name") String releaseName,
      @RequestParam(name = "comment", required = false) String releaseComment,
      @RequestParam("operator") String operator,
      @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish) {

    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
          clusterName, namespaceName));
    }

    Release release = releaseService.publish(namespace, releaseName, releaseComment, operator,
        isEmergencyPublish);

    Namespace parentNamespace = namespaceService.findParentNamespace(namespace);
    String messageCluster;
    if (parentNamespace != null) {
      messageCluster = parentNamespace.getClusterName();
    } else {
      messageCluster = clusterName;
    }

    // 发送发布消息
    messageSender.sendMessage(ReleaseMessageKeyGenerator
        .generate(appId, messageCluster, namespaceName), Topics.APOLLO_RELEASE_TOPIC);
    return BeanUtils.transform(ReleaseDTO.class, release);
  }


  /**
   * 更新并发布（合并分支属性配置项至master并且发布master）
   *
   * @param appId              应用id
   * @param clusterName        集群id
   * @param namespaceName      名称空间名称
   * @param releaseName        发布名称
   * @param branchName         分支名称
   * @param deleteBranch       删除分支
   * @param releaseComment     发布备注
   * @param isEmergencyPublish 是否紧急发布
   * @param changeSets         改变的配置集
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/updateAndPublish")
  public ReleaseDTO updateAndPublish(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName,
      @RequestParam("releaseName") String releaseName,
      @RequestParam("branchName") String branchName,
      @RequestParam(value = "deleteBranch", defaultValue = "true") boolean deleteBranch,
      @RequestParam(name = "releaseComment", required = false) String releaseComment,
      @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish,
      @RequestBody ItemChangeSets changeSets) {

    // 获取名称空间信息
    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
          clusterName, namespaceName));
    }

    // 更新并发布
    Release release = releaseService
        .mergeBranchChangeSetsAndRelease(namespace, branchName, releaseName,
            releaseComment, isEmergencyPublish, changeSets);

    // 删除分支
    if (deleteBranch) {
      namespaceBranchService.deleteBranch(appId, clusterName, namespaceName, branchName,
          NamespaceBranchStatus.MERGED, changeSets.getDataChangeLastModifiedBy());
    }

    // 发送发布消息
    messageSender.sendMessage(ReleaseMessageKeyGenerator
        .generate(appId, clusterName, namespaceName), Topics.APOLLO_RELEASE_TOPIC);

    return BeanUtils.transform(ReleaseDTO.class, release);

  }

  /**
   * 回滚
   *
   * @param releaseId   开始的发布id
   * @param toReleaseId 结束的发布id
   * @param operator    操作者
   */
  @Transactional(rollbackFor = Exception.class)
  @PutMapping("/releases/{releaseId}/rollback")
  public void rollback(@PathVariable("releaseId") long releaseId,
      @RequestParam(name = "toReleaseId", defaultValue = "-1") long toReleaseId,
      @RequestParam("operator") String operator) {

    // 回滚
    Release release;
    if (toReleaseId > -1) {
      release = releaseService.rollbackTo(releaseId, toReleaseId, operator);
    } else {
      release = releaseService.rollback(releaseId, operator);
    }

    String appId = release.getAppId();
    String clusterName = release.getClusterName();
    String namespaceName = release.getNamespaceName();
    // 发送发布消息
    messageSender.sendMessage(ReleaseMessageKeyGenerator
        .generate(appId, clusterName, namespaceName), Topics.APOLLO_RELEASE_TOPIC);
  }

  /**
   * 发布
   *
   * @param appId              应用id
   * @param clusterName        集群名称
   * @param namespaceName      名称空间名称
   * @param operator           操作者
   * @param releaseName        发布名称
   * @param releaseComment     发布备注
   * @param isEmergencyPublish 是否紧急发布
   * @param grayDelKeys        灰度发布待删除的规则key
   * @return 发布信息
   */
  @Transactional(rollbackFor = Exception.class)
  @PostMapping("/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/gray-del-releases")
  public ReleaseDTO publish(@PathVariable("appId") String appId,
      @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName,
      @RequestParam("operator") String operator,
      @RequestParam("releaseName") String releaseName,
      @RequestParam(name = "comment", required = false) String releaseComment,
      @RequestParam(name = "isEmergencyPublish", defaultValue = "false") boolean isEmergencyPublish,
      @RequestParam(name = "grayDelKeys") Set<String> grayDelKeys) {

    Namespace namespace = namespaceService.findOne(appId, clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
          clusterName, namespaceName));
    }
    // 删除灰色规则指定的KEY后再发布
    Release release = releaseService.grayDeletionPublish(namespace, releaseName, releaseComment,
        operator, isEmergencyPublish, grayDelKeys);

    // 发送发布消息
    Namespace parentNamespace = namespaceService.findParentNamespace(namespace);
    String messageCluster;
    if (parentNamespace != null) {
      messageCluster = parentNamespace.getClusterName();
    } else {
      messageCluster = clusterName;
    }
    messageSender
        .sendMessage(ReleaseMessageKeyGenerator.generate(appId, messageCluster, namespaceName),
            Topics.APOLLO_RELEASE_TOPIC);
    return BeanUtils.transform(ReleaseDTO.class, release);
  }

}
