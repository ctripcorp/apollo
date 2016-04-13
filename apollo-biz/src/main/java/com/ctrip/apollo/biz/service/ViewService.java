package com.ctrip.apollo.biz.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Cluster;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.repository.ClusterRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.google.common.base.Strings;

/**
 * config service for admin
 */
@Service
public class ViewService {

  @Autowired
  private ClusterRepository clusterRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ReleaseRepository releaseRepository;

  public List<Cluster> findClusters(String appId) {
    if (Strings.isNullOrEmpty(appId)) {
      return Collections.EMPTY_LIST;
    }

    List<Cluster> clusters = clusterRepository.findByAppId(appId);
    if (clusters == null) {
      return Collections.EMPTY_LIST;
    }
    return clusters;
  }

  public List<Namespace> findNamespaces(String appId, String clusterName) {
    List<Namespace> groups = namespaceRepository.findByAppIdAndClusterName(appId, clusterName);
    if (groups == null) {
      return Collections.EMPTY_LIST;
    }
    return groups;
  }

  public List<Item> findItems(String appId, String clusterName, String namespaceName) {
    Namespace group = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId, clusterName,
        namespaceName);
    if (group != null) {
      return findItems(group.getId());
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  public List<Item> findItems(Long namespaceId) {
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumDesc(namespaceId);
    if (items == null) {
      return Collections.EMPTY_LIST;
    }
    return items;
  }

  public List<Release> findReleases(String appId, String clusterName, String namespaceName) {
    List<Release> releases = releaseRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
        clusterName, namespaceName);
    if (releases == null) {
      return Collections.EMPTY_LIST;
    }
    return releases;
  }

}
