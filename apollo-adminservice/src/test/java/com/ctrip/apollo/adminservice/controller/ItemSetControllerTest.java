package com.ctrip.apollo.adminservice.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.ctrip.apollo.biz.entity.Item;
import com.ctrip.apollo.biz.repository.ItemRepository;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;

public class ItemSetControllerTest extends AbstractControllerTest {

  @Autowired
  ItemRepository itemRepository;

  @Test
  @Sql(scripts = "/controller/test-itemset.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/controller/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  public void testItemSetCreated() {
    String appId = "someAppId";
    AppDTO app =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + appId, AppDTO.class);

    ClusterDTO cluster = restTemplate.getForObject(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/default",
        ClusterDTO.class);

    NamespaceDTO namespace =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + app.getAppId()
            + "/clusters/" + cluster.getName() + "/namespaces/application", NamespaceDTO.class);

    Assert.assertEquals("someAppId", app.getAppId());
    Assert.assertEquals("default", cluster.getName());
    Assert.assertEquals("application", namespace.getNamespaceName());

    ItemChangeSets itemSet = new ItemChangeSets();
    itemSet.setModifyBy("created");

    int createdSize = 3;
    for (int i = 0; i < createdSize; i++) {
      ItemDTO item = new ItemDTO();
      item.setNamespaceId(namespace.getId());
      item.setKey("key_" + i);
      item.setValue("created_value_" + i);
      itemSet.addCreateItem(item);
    }

    ResponseEntity<Void> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/"
                + cluster.getName() + "/namespaces/" + namespace.getNamespaceName() + "/itemset",
            itemSet, Void.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    List<Item> items = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespace.getId());
    Assert.assertEquals(createdSize, items.size());
    Item item0 = items.get(0);
    Assert.assertEquals("key_0", item0.getKey());
    Assert.assertEquals("created_value_0", item0.getValue());
    Assert.assertEquals("created", item0.getDataChangeCreatedBy());
    Assert.assertNotNull(item0.getDataChangeCreatedTime());
  }

  @Test
  @Sql(scripts = "/controller/test-itemset.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/controller/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  public void testItemSetUpdated() {
    String appId = "someAppId";
    AppDTO app =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + appId, AppDTO.class);

    ClusterDTO cluster = restTemplate.getForObject(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/default",
        ClusterDTO.class);

    NamespaceDTO namespace =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + app.getAppId()
            + "/clusters/" + cluster.getName() + "/namespaces/application", NamespaceDTO.class);

    Assert.assertEquals("someAppId", app.getAppId());
    Assert.assertEquals("default", cluster.getName());
    Assert.assertEquals("application", namespace.getNamespaceName());

    ItemChangeSets createChangeSet = new ItemChangeSets();
    createChangeSet.setModifyBy("created");

    int createdSize = 3;
    for (int i = 0; i < createdSize; i++) {
      ItemDTO item = new ItemDTO();
      item.setNamespaceId(namespace.getId());
      item.setKey("key_" + i);
      item.setValue("created_value_" + i);
      createChangeSet.addCreateItem(item);
    }

    ResponseEntity<Void> response = restTemplate.postForEntity(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/" + cluster.getName()
            + "/namespaces/" + namespace.getNamespaceName() + "/itemset",
        createChangeSet, Void.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    ItemDTO[] items =
        restTemplate.getForObject(
            "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/"
                + cluster.getName() + "/namespaces/" + namespace.getNamespaceName() + "/items",
            ItemDTO[].class);

    ItemChangeSets udpateChangeSet = new ItemChangeSets();
    udpateChangeSet.setModifyBy("updated");

    int updatedSize = 2;
    for (int i = 0; i < updatedSize; i++) {
      items[i].setValue("updated_value_" + i);
      udpateChangeSet.addUpdateItem(items[i]);
    }

    response = restTemplate.postForEntity(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/" + cluster.getName()
            + "/namespaces/" + namespace.getNamespaceName() + "/itemset",
        udpateChangeSet, Void.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    List<Item> savedItems = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespace.getId());
    Assert.assertEquals(createdSize, savedItems.size());
    Item item0 = savedItems.get(0);
    Assert.assertEquals("key_0", item0.getKey());
    Assert.assertEquals("updated_value_0", item0.getValue());
    Assert.assertEquals("created", item0.getDataChangeCreatedBy());
    Assert.assertEquals("updated", item0.getDataChangeLastModifiedBy());
    Assert.assertNotNull(item0.getDataChangeCreatedTime());
    Assert.assertNotNull(item0.getDataChangeLastModifiedTime());
  }

  @Test
  @Sql(scripts = "/controller/test-itemset.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/controller/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  public void testItemSetDeleted() {
    String appId = "someAppId";
    AppDTO app =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + appId, AppDTO.class);

    ClusterDTO cluster = restTemplate.getForObject(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/default",
        ClusterDTO.class);

    NamespaceDTO namespace =
        restTemplate.getForObject("http://localhost:" + port + "/apps/" + app.getAppId()
            + "/clusters/" + cluster.getName() + "/namespaces/application", NamespaceDTO.class);

    Assert.assertEquals("someAppId", app.getAppId());
    Assert.assertEquals("default", cluster.getName());
    Assert.assertEquals("application", namespace.getNamespaceName());

    ItemChangeSets createChangeSet = new ItemChangeSets();
    createChangeSet.setModifyBy("created");

    int createdSize = 3;
    for (int i = 0; i < createdSize; i++) {
      ItemDTO item = new ItemDTO();
      item.setNamespaceId(namespace.getId());
      item.setKey("key_" + i);
      item.setValue("created_value_" + i);
      createChangeSet.addCreateItem(item);
    }

    ResponseEntity<Void> response = restTemplate.postForEntity(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/" + cluster.getName()
            + "/namespaces/" + namespace.getNamespaceName() + "/itemset",
        createChangeSet, Void.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    ItemDTO[] items =
        restTemplate.getForObject(
            "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/"
                + cluster.getName() + "/namespaces/" + namespace.getNamespaceName() + "/items",
            ItemDTO[].class);

    ItemChangeSets deleteChangeSet = new ItemChangeSets();
    deleteChangeSet.setModifyBy("deleted");

    int deletedSize = 1;
    for (int i = 0; i < deletedSize; i++) {
      items[i].setValue("deleted_value_" + i);
      deleteChangeSet.addDeleteItem(items[i]);
    }

    response = restTemplate.postForEntity(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/" + cluster.getName()
            + "/namespaces/" + namespace.getNamespaceName() + "/itemset",
        deleteChangeSet, Void.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    List<Item> savedItems = itemRepository.findByNamespaceIdOrderByLineNumAsc(namespace.getId());
    Assert.assertEquals(createdSize - deletedSize, savedItems.size());
    Item item0 = savedItems.get(0);
    Assert.assertEquals("key_1", item0.getKey());
    Assert.assertEquals("created_value_1", item0.getValue());
    Assert.assertEquals("created", item0.getDataChangeCreatedBy());
    Assert.assertNotNull(item0.getDataChangeCreatedTime());
  }
}
