package com.ctrip.apollo.biz.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.entity.Namespace;
import com.ctrip.apollo.biz.entity.Release;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.biz.repository.NamespaceRepository;
import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.core.exception.NotFoundException;
import com.google.gson.Gson;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseService {

  @Autowired
  private ReleaseRepository releaseRepository;

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Autowired
  private ItemRepository itemRepository;

  private Gson gson = new Gson();

  public Release findOne(long releaseId) {
    Release release = releaseRepository.findOne(releaseId);
    return release;
  }

  public Release buildRelease(String name, String comment, String appId, String clusterName,
      String namespaceName) {
    Namespace namespace = namespaceRepository.findByAppIdAndClusterNameAndNamespaceName(appId,
        clusterName, namespaceName);
    if (namespace == null) {
      throw new NotFoundException(String.format("Could not find namespace for %s %s %s", appId,
          clusterName, namespaceName));
    }
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespace.getId());
    Map<String, String> configurations = new HashMap<String, String>();
    for (Item item : items) {
      configurations.put(item.getKey(), item.getValue());
    }

    Release release = new Release();
    release.setDataChangeCreatedTime(new Date());
    release.setDataChangeCreatedBy(name);
    release.setDataChangeLastModifiedBy(name);
    release.setName(name);
    release.setComment(comment);
    release.setAppId(appId);
    release.setClusterName(clusterName);
    release.setNamespaceName(namespaceName);
    release.setConfigurations(gson.toJson(configurations));
    return releaseRepository.save(release);
  }

}
