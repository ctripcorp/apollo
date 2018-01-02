package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceSyncModel;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.entity.vo.ItemDiffs;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
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

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
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

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/item", method = RequestMethod.PUT)
  public void updateItem(@PathVariable String appId, @PathVariable String env,
                         @PathVariable String clusterName, @PathVariable String namespaceName,
                         @RequestBody ItemDTO item) {
    checkModel(isValidItem(item));

    String username = userInfoHolder.getUser().getUserId();
    item.setDataChangeLastModifiedBy(username);

    configService.updateItem(appId, Env.valueOf(env), clusterName, namespaceName, item);
  }


  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
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

    return configService.compare(model.getSyncToNamespaces(), model.getSyncItems());
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName)")
  @RequestMapping(value = "/apps/{appId}/namespaces/{namespaceName}/items", method = RequestMethod.PUT, consumes = {
      "application/json"})
  public ResponseEntity<Void> update(@PathVariable String appId, @PathVariable String namespaceName,
                                     @RequestBody NamespaceSyncModel model) {
    checkModel(Objects.nonNull(model) && !model.isInvalid());

    configService.syncItems(model.getSyncToNamespaces(), model.getSyncItems());
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private boolean isValidItem(ItemDTO item) {
    return Objects.nonNull(item) && !StringUtils.isContainEmpty(item.getKey());
  }


}
