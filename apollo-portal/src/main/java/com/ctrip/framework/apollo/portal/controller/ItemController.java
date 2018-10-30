package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceSyncModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.entity.vo.NamespaceIdentifier;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.ctrip.framework.apollo.common.utils.RequestPrecondition.checkModel;

@RestController
public class ItemController {

  @Autowired
  private ItemService configService;
  @Autowired
  private UserInfoHolder userInfoHolder;
  @Autowired
  private PermissionValidator permissionValidator;

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public void modifyItemsByText(@PathVariable String appId, @PathVariable String env,
                                @PathVariable String clusterName, @PathVariable String namespaceName,
                                @RequestBody NamespaceTextModel model) {

    checkModel(model != null);

    model.setAppId(appId);
    model.setClusterName(clusterName);
    model.setEnv(env);
    model.setNamespaceName(namespaceName);

    configService.updateConfigItemByText(model);
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.POST)
  public ItemDTO createItem(@PathVariable String appId, @PathVariable String env,
                            @PathVariable String clusterName, @PathVariable String namespaceName,
                            @RequestBody ItemDTO item) {
    checkModel(isValidItem(item));

    //protect
    item.setLineNum(0);
    item.setId(0);
    String userId = userInfoHolder.getUser().getUserId();
    item.setDataChangeCreatedBy(userId);
    item.setDataChangeLastModifiedBy(userId);
    item.setDataChangeCreatedTime(null);
    item.setDataChangeLastModifiedTime(null);

    return configService.createItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.PUT)
  public void updateItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                         @RequestBody ItemDTO item) {
    checkModel(isValidItem(item));

    String username = userInfoHolder.getUser().getUserId();
    item.setDataChangeLastModifiedBy(username);

    configService.updateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }


  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env) ")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/{itemId}", method = RequestMethod.DELETE)
  public void deleteItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                         @PathVariable long itemId) {
    if (itemId <= 0) {
      throw new BadRequestException("item id invalid");
    }
    configService.deleteItem(Env.valueOf(env), itemId, userInfoHolder.getUser().getUserId());
  }


  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items", method = RequestMethod.GET)
  public List<ItemDTO> findItems(@PathVariable String appId, @PathVariable String env,
                                 @PathVariable String clusterName, @PathVariable String namespaceName,
                                 @RequestParam(defaultValue = "lineNum") String orderBy) {

    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

    List<ItemDTO> items = configService.findItems(appId, Env.valueOf(env), clusterName, namespaceName);
    if ("lastModifiedTime".equals(orderBy)) {
      Collections.sort(items, (o1, o2) -> {
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

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/branches/{branchName}/items", method = RequestMethod.GET)
  public List<ItemDTO> findBranchItems(@PathVariable("appId") String appId, @PathVariable String env,
                                       @PathVariable("clusterName") String clusterName,
                                       @PathVariable("namespaceName") String namespaceName,
                                       @PathVariable("branchName") String branchName) {

    return findItems(appId, env, branchName, namespaceName, "lastModifiedTime");
  }

  @RequestMapping(value = "/namespaces/{namespaceName}/diff", method = RequestMethod.POST, consumes = {
      "application/json"})
  public List<ItemDiffs> diff(@RequestBody NamespaceSyncModel model) {
    checkModel(Objects.nonNull(model) && !model.isInvalid());

    List<ItemDiffs> itemDiffs = configService.compare(model.getSyncToNamespaces(), model.getSyncItems());

    for (ItemDiffs diff : itemDiffs) {
      NamespaceIdentifier namespace = diff.getNamespace();
      if (namespace == null) {
        continue;
      }

      if (permissionValidator
          .shouldHideConfigToCurrentUser(namespace.getAppId(), namespace.getEnv().name(), namespace.getNamespaceName())) {
        diff.setDiffs(new ItemChangeSets());
        diff.setExtInfo("您不是该项目的管理员，也没有该Namespace在 " + namespace.getEnv() +  " 环境的编辑或发布权限");
      }
    }

    return itemDiffs;
  }

  @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public ResponseEntity<Void> update(@PathVariable String appId, @PathVariable String namespaceName,
                                     @RequestBody NamespaceSyncModel model) {
    checkModel(Objects.nonNull(model) && !model.isInvalid());
    boolean hasPermission = permissionValidator.hasModifyNamespacePermission(appId, namespaceName);
    Env envNoPermission = null;
    // if uses has ModifyNamespace permission then he has permission
    if (!hasPermission) {
      // else check if user has every env's ModifyNamespace permission
      hasPermission = true;
      for (NamespaceIdentifier namespaceIdentifier : model.getSyncToNamespaces()) {
        // once user has not one of the env's ModifyNamespace permission, then break the loop
        hasPermission &= permissionValidator.hasModifyNamespacePermission(namespaceIdentifier.getAppId(), namespaceIdentifier.getNamespaceName(), namespaceIdentifier.getEnv().toString());
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
    else
      throw new AccessDeniedException(String.format("您没有修改环境%s的权限", envNoPermission));
  }

  private boolean isValidItem(ItemDTO item) {
    return Objects.nonNull(item) && !StringUtils.isContainEmpty(item.getKey());
  }


}
