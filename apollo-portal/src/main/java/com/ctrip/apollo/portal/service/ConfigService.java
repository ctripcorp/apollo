package com.ctrip.apollo.portal.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.utils.StringUtils;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.form.NamespaceModifyModel;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.entity.form.NamespaceReleaseModel;
import com.ctrip.apollo.portal.service.txtresolver.ConfigTextResolver;
import com.ctrip.apollo.portal.service.txtresolver.TextResolverResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ConfigService {

  private Logger logger = LoggerFactory.getLogger(ConfigService.class);

  @Autowired
  private AdminServiceAPI.NamespaceAPI groupAPI;
  @Autowired
  private AdminServiceAPI.ItemAPI itemAPI;
  @Autowired
  private AdminServiceAPI.ReleaseAPI releaseAPI;

  @Autowired
  private ConfigTextResolver resolver;

  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * load cluster all namespace info with items
   * @param appId
   * @param env
   * @param clusterName
   * @return
   */
  public List<NamespaceVO> findNampspaces(String appId, Apollo.Env env, String clusterName) {

    List<NamespaceDTO> namespaces = groupAPI.findGroupsByAppAndCluster(appId, env, clusterName);
    if (namespaces == null || namespaces.size() == 0) {
      return Collections.EMPTY_LIST;
    }

    List<NamespaceVO> namespaceVOs = new LinkedList<>();
    for (NamespaceDTO namespace : namespaces) {

      NamespaceVO namespaceVO = null;
      try {
        namespaceVO = parseNamespace(appId, env, clusterName, namespace);
        namespaceVOs.add(namespaceVO);
      } catch (Exception e) {
        logger.error("parse namespace error. app id:{}, env:{}, clusterName:{}, namespace:{}", appId, env, clusterName,
                     namespace.getNamespaceName(), e);
        return namespaceVOs;
      }
    }

    return namespaceVOs;
  }

  private NamespaceVO parseNamespace(String appId, Apollo.Env env, String clusterName, NamespaceDTO namespace) {

    NamespaceVO namespaceVO = new NamespaceVO();
    namespaceVO.setNamespace(namespace);

    List<NamespaceVO.ItemVO> itemVos = new LinkedList<>();
    namespaceVO.setItems(itemVos);

    String namespaceName = namespace.getNamespaceName();

    //latest release
    ReleaseDTO release = releaseAPI.loadLatestRelease(appId, env, clusterName, namespaceName);
    Map<String, String> releaseItems = new HashMap<>();
    if (release != null) {
      try {
        releaseItems = objectMapper.readValue(release.getConfigurations(), Map.class);
      } catch (IOException e) {
        logger.error("parse release json error. appId:{},env:{},clusterName:{},namespace:{}", appId,
                     env, clusterName, namespaceName);
        return namespaceVO;
      }
    }

    //not release config items
    List<ItemDTO> items = itemAPI.findItems(appId, env, clusterName, namespaceName);
    int modifiedItemCnt = 0;
    for (ItemDTO itemDTO : items) {

      NamespaceVO.ItemVO itemVO = parseItemVO(itemDTO, releaseItems);

      if (itemVO.isModified()) {
        modifiedItemCnt++;
      }

      itemVos.add(itemVO);
    }
    namespaceVO.setItemModifiedCnt(modifiedItemCnt);

    return namespaceVO;
  }

  private NamespaceVO.ItemVO parseItemVO(ItemDTO itemDTO, Map<String, String> releaseItems) {
    String key = itemDTO.getKey();
    NamespaceVO.ItemVO itemVO = new NamespaceVO.ItemVO();
    itemVO.setItem(itemDTO);
    String newValue = itemDTO.getValue();
    String oldValue = releaseItems.get(key);
    if (!StringUtils.isEmpty(key) && (oldValue == null || !newValue.equals(oldValue))) {
      itemVO.setModified(true);
      itemVO.setOldValue(oldValue == null ? "" : oldValue);
      itemVO.setNewValue(newValue);
    }
    return itemVO;
  }

  /**
   * parse config text and update config items
   * @return  parse result
   */
  public TextResolverResult resolveConfigText(NamespaceModifyModel model) {
    String appId = model.getAppId();
    Apollo.Env env = model.getEnv();
    String clusterName = model.getClusterName();
    String namespaceName = model.getNamespaceName();
    long namespaceId = model.getNamespaceId();
    String configText = model.getConfigText();

    TextResolverResult result = new TextResolverResult();
    try {
      result = resolver.resolve( namespaceId, configText, itemAPI.findItems(appId, env, clusterName, namespaceName));
    } catch (Exception e) {
      logger
          .error("resolve config text error. app id:{}, env:{}, clusterName:{}, namespace:{}", appId, env, clusterName,
                 namespaceName, e);
      result.setResolveSuccess(false);
      result.setMsg("oops! server resolveConfigText config text error.");
      return result;
    }
    if (result.isResolveSuccess()) {
      try {
        ItemChangeSets changeSets = result.getChangeSets();
        changeSets.setModifyBy(model.getModifyBy());
        enrichChangeSetBaseInfo(changeSets);
        itemAPI.updateItems(appId, env, clusterName, namespaceName, changeSets);
      } catch (Exception e) {
        logger.error("resolve config text error. app id:{}, env:{}, clusterName:{}, namespace:{}", appId, env,
                     clusterName, namespaceName, e);
        result.setResolveSuccess(false);
        result.setMsg("oops! server update config error.");
        return result;
      }
    } else {
      logger.warn("resolve config text error by format error. app id:{}, env:{}, clusterName:{}, namespace:{},cause:{}",
                  appId,env, clusterName, namespaceName, result.getMsg());
    }

    return result;
  }

  private void enrichChangeSetBaseInfo(ItemChangeSets changeSets){
    for (ItemDTO item: changeSets.getCreateItems()){
      item.setDataChangeCreatedTime(new Date());
    }
  }

  /**
   * release config items
   * @return
   */
  public ReleaseDTO release(NamespaceReleaseModel model){
    return releaseAPI.release(model.getAppId(), model.getEnv(), model.getClusterName(), model.getNamespaceName(),
                              model.getReleaseBy(), model.getReleaseComment());
  }
}
