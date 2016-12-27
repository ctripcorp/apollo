package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.dto.ReleaseDTO;
import com.ctrip.framework.apollo.common.entity.AppNamespace;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.components.txtresolver.PropertyResolver;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NamespaceServiceTest {

  @Mock
  private AdminServiceAPI.NamespaceAPI namespaceAPI;
  @Mock
  private ReleaseService releaseService;
  @Mock
  private ItemService itemService;
  @Mock
  private PropertyResolver resolver;
  @Mock
  private AppNamespaceService appNamespaceService;

  @InjectMocks
  private NamespaceService namespaceService;


  @Before
  public void setup() {
  }

  @Test
  public void testFindNamespace() {
    String appId = "6666";
    String clusterName = "default";
    String namespaceName = "application";

    AppNamespace applicationAppNamespace = mock(AppNamespace.class);
    AppNamespace hermesAppNamespace = mock(AppNamespace.class);

    NamespaceDTO application = new NamespaceDTO();
    application.setId(1);
    application.setClusterName(clusterName);
    application.setAppId(appId);
    application.setNamespaceName(namespaceName);

    NamespaceDTO hermes = new NamespaceDTO();
    hermes.setId(2);
    hermes.setClusterName("default");
    hermes.setAppId(appId);
    hermes.setNamespaceName("hermes");
    List<NamespaceDTO> namespaces = Arrays.asList(application, hermes);

    ReleaseDTO someRelease = new ReleaseDTO();
    someRelease.setConfigurations("{\"a\":\"123\",\"b\":\"123\"}");

    ItemDTO i1 = new ItemDTO("a", "123", "", 1);
    ItemDTO i2 = new ItemDTO("b", "1", "", 2);
    ItemDTO i3 = new ItemDTO("", "", "#dddd", 3);
    ItemDTO i4 = new ItemDTO("c", "1", "", 4);
    List<ItemDTO> someItems = Arrays.asList(i1, i2, i3, i4);

    when(applicationAppNamespace.getFormat()).thenReturn(ConfigFileFormat.Properties.getValue());
    when(hermesAppNamespace.getFormat()).thenReturn(ConfigFileFormat.XML.getValue());
    when(appNamespaceService.findByAppIdAndName(appId, namespaceName))
        .thenReturn(applicationAppNamespace);
    when(appNamespaceService.findPublicAppNamespace("hermes")).thenReturn(hermesAppNamespace);
    when(namespaceAPI.findNamespaceByCluster(appId, Env.DEV, clusterName)).thenReturn(namespaces);
    when(releaseService.loadLatestRelease(appId, Env.DEV, clusterName, namespaceName)).thenReturn(someRelease);
    when(releaseService.loadLatestRelease(appId, Env.DEV, clusterName, "hermes")).thenReturn(someRelease);
    when(itemService.findItems(appId, Env.DEV, clusterName, namespaceName)).thenReturn(someItems);

    List<NamespaceBO> namespaceVOs = namespaceService.findNamespaceBOs(appId, Env.DEV, clusterName);
    assertEquals(2, namespaceVOs.size());
    NamespaceBO namespaceVO = namespaceVOs.get(0);
    assertEquals(4, namespaceVO.getItems().size());
    assertEquals("a", namespaceVO.getItems().get(0).getItem().getKey());
    assertEquals(2, namespaceVO.getItemModifiedCnt());
    assertEquals(appId, namespaceVO.getBaseInfo().getAppId());
    assertEquals(clusterName, namespaceVO.getBaseInfo().getClusterName());
    assertEquals(namespaceName, namespaceVO.getBaseInfo().getNamespaceName());

  }

}
