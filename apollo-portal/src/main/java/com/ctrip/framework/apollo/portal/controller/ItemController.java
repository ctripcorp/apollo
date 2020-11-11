package com.ctrip.framework.apollo.portal.controller;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceSyncModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifier;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

/**
 * 配置项 Controller层
 */
@RestController
public class ItemController {

  private final ItemService configService;
  private final NamespaceService namespaceService;
  private final UserInfoHolder userInfoHolder;
  private final PermissionValidator permissionValidator;

  public ItemController(final ItemService configService, final UserInfoHolder userInfoHolder,
      final PermissionValidator permissionValidator, final NamespaceService namespaceService) {
    this.configService = configService;
    this.userInfoHolder = userInfoHolder;
    this.permissionValidator = permissionValidator;
    this.namespaceService = namespaceService;
  }

  /**
   * 批量更新指定名称空间下的配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param model         名称空间文本 model
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PutMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", consumes = {
      "application/json"})
  public void modifyItemsByText(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceTextModel model) {
    // 设置 PathVariable 到 `model` 中cc
    model.setAppId(appId);
    model.setClusterName(clusterName);
    model.setEnv(env);
    model.setNamespaceName(namespaceName);
    // 批量更新一个 Namespace 下的 Item 们
    configService.updateConfigItemByText(model);
  }

  /**
   * 创建配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param item          配置项信息
   * @return 创建的配置项信息
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item")
  public ItemDTO createItem(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody ItemDTO item) {
    // 校验 Item 格式正确
    checkModel(isValidItem(item));

    //protect
    item.setLineNum(0);
    item.setId(0);
    // 设置 ItemDTO 的创建和修改人为当前管理员
    String userId = userInfoHolder.getUser().getUserId();
    item.setDataChangeCreatedBy(userId);
    item.setDataChangeLastModifiedBy(userId);
    item.setDataChangeCreatedTime(null);
    item.setDataChangeLastModifiedTime(null);
    // 保存 Item 到 Admin Service
    return configService.createItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  /**
   * 更新配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param item          配置项信息
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PutMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item")
  public void updateItem(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody ItemDTO item) {
    checkModel(isValidItem(item));

    String username = userInfoHolder.getUser().getUserId();
    item.setDataChangeLastModifiedBy(username);

    configService.updateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  /**
   * 删除配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param itemId        配置项id
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env) ")
  @DeleteMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}")
  public void deleteItem(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @PathVariable long itemId) {
    ItemDTO item = configService.loadItemById(Env.valueOf(env), itemId);
    NamespaceDTO namespace = namespaceService
        .loadNamespaceBaseInfo(appId, Env.valueOf(env), clusterName, namespaceName);

    // In case someone constructs an attack scenario
    if (!item.getNamespaceId().equals(namespace.getId())) {
      throw new BadRequestException("Invalid request, item and namespace do not match!");
    }

    configService.deleteItem(Env.valueOf(env), itemId, userInfoHolder.getUser().getUserId());
  }

  /**
   * 查询配置项信息列表
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param orderBy       行号
   * @return 配置项信息列表
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items")
  public List<ItemDTO> findItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam(defaultValue = "lineNum") String orderBy) {

    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

    List<ItemDTO> items = configService
        .findItems(appId, Env.valueOf(env), clusterName, namespaceName);
    if ("lastModifiedTime".equals(orderBy)) {
      items.sort((o1, o2) -> {
        if (o1.getDataChangeLastModifiedTime().after(o2.getDataChangeLastModifiedTime())) {
          return -1;
        }
        if (o1.getDataChangeLastModifiedTime().before(o2.getDataChangeLastModifiedTime())) {
          return 1;
        }
        return 0;
      });
    }
    return items;
  }

  /**
   * 查询分支配置项列表信息
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param branchName    分支名称
   * @return 分支配置项列表信息
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/items")
  public List<ItemDTO> findBranchItems(@PathVariable("appId") String appId,
      @PathVariable String env, @PathVariable("clusterName") String clusterName,
      @PathVariable("namespaceName") String namespaceName,
      @PathVariable("branchName") String branchName) {

    return findItems(appId, env, branchName, namespaceName, "lastModifiedTime");
  }

  /**
   * 配置项对比
   *
   * @param model 名称空间同步Model
   * @return 配置项对比信息
   */
  @PostMapping(value = "/namespaces/{namespaceName}/diff", consumes = {"application/json"})
  public List<ItemDiffs> diff(@RequestBody NamespaceSyncModel model) {
    checkModel(!model.isInvalid());

    List<ItemDiffs> itemDiffs = configService
        .compare(model.getSyncToNamespaces(), model.getSyncItems());

    for (ItemDiffs diff : itemDiffs) {
      NamespaceIdentifier namespace = diff.getNamespace();
      if (namespace == null) {
        continue;
      }

      if (permissionValidator.shouldHideConfigToCurrentUser(namespace.getAppId(),
          namespace.getEnv().getName(), namespace.getNamespaceName())) {
        diff.setDiffs(new ItemChangeSets());
        diff.setExtInfo(
            "You are not this project's administrator, nor you have edit or release permission for the namespace in environment: "
                + namespace.getEnv());
      }
    }

    return itemDiffs;
  }

  /**
   * 更新
   *
   * @param appId         应用id
   * @param namespaceName 名称空间名称
   * @param model         名称空间同步 Model
   * @return ResponseEntity.status(HttpStatus.OK)
   */
  @PutMapping(value = "/apps/{appId}/namespaces/{namespaceName}/items", consumes = {
      "application/json"})
  public ResponseEntity<Void> update(@PathVariable String appId, @PathVariable String namespaceName,
      @RequestBody NamespaceSyncModel model) {
    checkModel(!model.isInvalid());
    boolean hasPermission = permissionValidator.hasModifyNamespacePermission(appId, namespaceName);
    Env envNoPermission = null;
    // if uses has ModifyNamespace permission then he has permission
    if (!hasPermission) {
      // else check if user has every env's ModifyNamespace permission
      hasPermission = true;
      for (NamespaceIdentifier namespaceIdentifier : model.getSyncToNamespaces()) {
        // once user has not one of the env's ModifyNamespace permission, then break the loop
        hasPermission &= permissionValidator
            .hasModifyNamespacePermission(namespaceIdentifier.getAppId(),
                namespaceIdentifier.getNamespaceName(), namespaceIdentifier.getEnv().toString());
        if (!hasPermission) {
          envNoPermission = namespaceIdentifier.getEnv();
          break;
        }
      }
    }
    if (hasPermission) {
      configService.syncItems(model.getSyncToNamespaces(), model.getSyncItems());
      return ResponseEntity.status(HttpStatus.OK).build();
    }
    throw new AccessDeniedException(
        String.format("You don't have the permission to modify environment: %s", envNoPermission));
  }

  /**
   * 语法检查文本
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param model         名称空间文本model
   * @return ResponseEntity.ok()
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/syntax-check", consumes = {
      "application/json"})
  public ResponseEntity<Void> syntaxCheckText(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestBody NamespaceTextModel model) {

    doSyntaxCheck(model);

    return ResponseEntity.ok().build();
  }

  /**
   * 撤消配置项
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PutMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/revoke-items")
  public void revokeItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName) {
    configService.revokeItem(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  /**
   * 语法检查
   *
   * @param model 名称空间文本Model
   */
  void doSyntaxCheck(NamespaceTextModel model) {
    if (StringUtils.isBlank(model.getConfigText())) {
      return;
    }

    // 仅支持yaml语法检查
    if (model.getFormat() != ConfigFileFormat.YAML && model.getFormat() != ConfigFileFormat.YML) {
      return;
    }

    // 使用 YamlPropertiesFactoryBean 去检查yaml语法
    TypeLimitedYamlPropertiesFactoryBean yamlPropertiesFactoryBean = new TypeLimitedYamlPropertiesFactoryBean();
    yamlPropertiesFactoryBean.setResources(new ByteArrayResource(model.getConfigText().getBytes()));
    //此调用将yaml转换为属性，如果转换失败，将引发异常
    yamlPropertiesFactoryBean.getObject();
  }

  /**
   * 校验配置项不为空
   *
   * @param item 配置项信息
   * @return true, 配置项不为空，否则，false
   */
  private boolean isValidItem(ItemDTO item) {
    return Objects.nonNull(item) && !com.ctrip.framework.apollo.core.utils.StringUtils
        .isContainEmpty(item.getKey());
  }

  /**
   * 限制Yaml类型属性工厂bean
   */
  private static class TypeLimitedYamlPropertiesFactoryBean extends YamlPropertiesFactoryBean {

    @Override
    protected Yaml createYaml() {
      LoaderOptions loaderOptions = new LoaderOptions();
      loaderOptions.setAllowDuplicateKeys(false);
      return new Yaml(new SafeConstructor(), new Representer(),
          new DumperOptions(), loaderOptions);
    }
  }

}
