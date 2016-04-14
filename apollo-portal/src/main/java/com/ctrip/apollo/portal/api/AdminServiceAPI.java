package com.ctrip.apollo.portal.api;


import com.ctrip.apollo.Apollo;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;


@Service
public class AdminServiceAPI {

  private static final Logger logger = LoggerFactory.getLogger(AdminServiceAPI.class);

  @Service
  public static class AppAPI extends API {

    public static String APP_API = "/apps";

    public List<AppDTO> getApps(Apollo.Env env) {
      return Arrays.asList(restTemplate.getForObject(getAdminServiceHost(env) + APP_API, AppDTO[].class));
    }

    public AppDTO save(Apollo.Env env, AppDTO app) {
      return restTemplate.postForEntity(getAdminServiceHost(env) + APP_API, app, AppDTO.class).getBody();
    }
  }


  @Service
  public static class NamespaceAPI extends API {

    public List<NamespaceDTO> findGroupsByAppAndCluster(String appId, Apollo.Env env,
                                                        String clusterName) {
      if (StringUtils.isContainEmpty(appId, clusterName)) {
        return null;
      }

      return Arrays.asList(restTemplate.getForObject(
          getAdminServiceHost(env) + String.format("apps/%s/clusters/%s/namespaces", appId, clusterName),
          NamespaceDTO[].class));
    }

    public NamespaceDTO loadNamespace(String appId, Apollo.Env env,
                                      String clusterName, String namespaceName) {
      if (StringUtils.isContainEmpty(appId, clusterName, namespaceName)) {
        return null;
      }
      return restTemplate.getForObject(getAdminServiceHost(env) +
                                       String.format("apps/%s/clusters/%s/namespaces/%s", appId, clusterName,
                                                     namespaceName), NamespaceDTO.class);
    }
  }

  @Service
  public static class ItemAPI extends API {

    public List<ItemDTO> findItems(String appId, Apollo.Env env, String clusterName, String namespace) {
      if (StringUtils.isContainEmpty(appId, clusterName, namespace)) {
        return null;
      }

      return Arrays.asList(restTemplate.getForObject(getAdminServiceHost(env) + String
                                                         .format("apps/%s/clusters/%s/namespaces/%s/items", appId,
                                                                 clusterName, namespace),
                                                     ItemDTO[].class));
    }

    public void updateItems(String appId, Apollo.Env env, String clusterName, String namespace,
                            ItemChangeSets changeSets) {
      if (StringUtils.isContainEmpty(appId, clusterName, namespace)) {
        return;
      }
      restTemplate.postForEntity(getAdminServiceHost(env) + String.format("apps/%s/clusters/%s/namespaces/%s/itemset",
                                                                          appId, clusterName, namespace), changeSets,
                                 Void.class);
    }


  }

  @Service
  public static class ClusterAPI extends API {

    public List<ClusterDTO> findClustersByApp(String appId, Apollo.Env env) {
      if (StringUtils.isContainEmpty(appId)) {
        return null;
      }

      return Arrays
          .asList(restTemplate.getForObject(getAdminServiceHost(env) + String.format("apps/%s/clusters", appId),
                                            ClusterDTO[].class));
    }
  }

  @Service
  public static class ReleaseAPI extends API {

    public ReleaseDTO loadLatestRelease(String appId, Apollo.Env env, String clusterName, String namespace) {
      if (StringUtils.isContainEmpty(appId, clusterName, namespace)) {
        return null;
      }
      try {
        ReleaseDTO releaseDTO = restTemplate.getForObject(getAdminServiceHost(env) + String
            .format("apps/%s/clusters/%s/namespaces/%s/releases/latest", appId,
                    clusterName, namespace), ReleaseDTO.class);
        return releaseDTO;
      } catch (HttpClientErrorException e) {
        logger.warn(
            " call [ReleaseAPI.loadLatestRelease] and return not fount exception.app id:{}, env:{}, clusterName:{}, namespace:{}",
            appId, env, clusterName, namespace);
        return null;
      }
    }

    public ReleaseDTO release(String appId, Apollo.Env env, String clusterName, String namespace, String releaseBy,
                              String comment) {
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
      parameters.add("name", releaseBy);
      parameters.add("comment", comment);
      HttpEntity<MultiValueMap<String, String>> entity =
          new HttpEntity<MultiValueMap<String, String>>(parameters, null);
      ResponseEntity<ReleaseDTO> response = restTemplate.postForEntity(getAdminServiceHost(env) + String.
          format("apps/%s/clusters/%s/namespaces/%s/releases", appId, clusterName, namespace),
                                                                       entity, ReleaseDTO.class);
      if (response.getStatusCode() == HttpStatus.OK){
        return response.getBody();
      }else {
        logger.error("release fail.id:{}, env:{}, clusterName:{}, namespace:{},releaseBy{}",appId, env, clusterName, namespace, releaseBy);
        return null;
      }
    }
  }

}
