package com.ctrip.apollo.adminservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ctrip.apollo.biz.repository.ReleaseRepository;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.dto.ClusterDTO;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.google.gson.Gson;

public class ReleaseControllerTest extends AbstractControllerTest {

  @Autowired
  ReleaseRepository releaseRepository;

  @Test
  @Sql(scripts = "/controller/test-release.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/controller/test-release-cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
  public void testReleaseBuild() {
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

    ItemDTO[] items =
        restTemplate.getForObject(
            "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/"
                + cluster.getName() + "/namespaces/" + namespace.getNamespaceName() + "/items",
            ItemDTO[].class);
    Assert.assertEquals(3, items.length);

    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("name", "someReleaseName");
    parameters.add("comment", "someComment");
    HttpEntity<MultiValueMap<String, String>> entity =
        new HttpEntity<MultiValueMap<String, String>>(parameters, null);
    ResponseEntity<ReleaseDTO> response = restTemplate.postForEntity(
        "http://localhost:" + port + "/apps/" + app.getAppId() + "/clusters/" + cluster.getName()
            + "/namespaces/" + namespace.getNamespaceName() + "/releases",
        entity, ReleaseDTO.class);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    ReleaseDTO release = response.getBody();
    Assert.assertEquals("someReleaseName", release.getName());
    Assert.assertEquals("someComment", release.getComment());
    Assert.assertEquals("someAppId", release.getAppId());
    Assert.assertEquals("default", release.getClusterName());
    Assert.assertEquals("application", release.getNamespaceName());

    Map<String, String> configurations = new HashMap<String, String>();
    configurations.put("k1", "v1");
    configurations.put("k2", "v2");
    configurations.put("k3", "v3");
    Gson gson = new Gson();
    Assert.assertEquals(gson.toJson(configurations), release.getConfigurations());
  }
}
