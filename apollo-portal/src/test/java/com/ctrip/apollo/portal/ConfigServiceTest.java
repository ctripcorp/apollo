package com.ctrip.apollo.portal;

import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.NamespaceVO;
import com.ctrip.apollo.portal.entity.form.NamespaceTextModel;
import com.ctrip.apollo.portal.service.ConfigService;
import com.ctrip.apollo.portal.service.txtresolver.PropertyResolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceTest extends AbstractPortalTest{

  @Mock
  private AdminServiceAPI.NamespaceAPI namespaceAPI;
  @Mock
  private AdminServiceAPI.ReleaseAPI releaseAPI;
  @Mock
  private AdminServiceAPI.ItemAPI itemAPI;
  @Mock
  private PropertyResolver resolver;

  @InjectMocks
  private ConfigService configService;

  @Before
  public void setup() {
  }

  @Test
  public void testFindNamespace() {
    String appId = "6666";
    String clusterName = "default";
    String namespaceName = "application";

    NamespaceDTO application = new NamespaceDTO();
    application.setId(1);
    application.setClusterName(clusterName);
    application.setAppId(appId);
    application.setNamespaceName(namespaceName);

    NamespaceDTO hermas = new NamespaceDTO();
    hermas.setId(2);
    hermas.setClusterName("default");
    hermas.setAppId(appId);
    hermas.setNamespaceName("hermas");
    List<NamespaceDTO> namespaces = Arrays.asList(application, hermas);

    ReleaseDTO someRelease = new ReleaseDTO();
    someRelease.setConfigurations("{\"a\":\"123\",\"b\":\"123\"}");

    ItemDTO i1 = new ItemDTO("a", "123", "", 1);
    ItemDTO i2 = new ItemDTO("b", "1", "", 2);
    ItemDTO i3 = new ItemDTO("", "", "#dddd", 3);
    ItemDTO i4 = new ItemDTO("c", "1", "", 4);
    List<ItemDTO> someItems = Arrays.asList(i1, i2, i3, i4);

    when(namespaceAPI.findNamespaceByCluster(appId, Env.DEV, clusterName)).thenReturn(namespaces);
    when(releaseAPI.loadLatestRelease(appId, Env.DEV, clusterName, namespaceName)).thenReturn(someRelease);
    when(itemAPI.findItems(appId, Env.DEV, clusterName, namespaceName)).thenReturn(someItems);

    List<NamespaceVO> namespaceVOs = configService.findNampspaces(appId, Env.DEV, clusterName);
    assertEquals(2, namespaceVOs.size());
    NamespaceVO namespaceVO = namespaceVOs.get(0);
    assertEquals(4, namespaceVO.getItems().size());
    assertEquals("a", namespaceVO.getItems().get(0).getItem().getKey());
    assertEquals(2, namespaceVO.getItemModifiedCnt());
    assertEquals(appId, namespaceVO.getNamespace().getAppId());
    assertEquals(clusterName, namespaceVO.getNamespace().getClusterName());
    assertEquals(namespaceName, namespaceVO.getNamespace().getNamespaceName());

  }

  @Test
  public void testUpdateConfigByText() {
    String appId = "6666";
    String clusterName = "default";
    String namespaceName = "application";

    NamespaceTextModel model = new NamespaceTextModel();
    model.setEnv("DEV");
    model.setModifyBy("ll");
    model.setNamespaceName(namespaceName);
    model.setClusterName(clusterName);
    model.setAppId(appId);
    model.setConfigText("a=b\nb=c\nc=d\nd=e");

    List<ItemDTO> itemDTOs = mockBaseItemHas3Key();
    ItemChangeSets changeSets = new ItemChangeSets();
    changeSets.addCreateItem(new ItemDTO("d", "c", "", 4));

    when(itemAPI.findItems(appId, Env.DEV, clusterName, namespaceName)).thenReturn(itemDTOs);
    when(resolver.resolve(0, model.getConfigText(), itemDTOs)).thenReturn(changeSets);
    try {
      configService.updateConfigItemByText(model);
    }catch (Exception e){
      Assert.fail();
    }
  }


  /**
   * a=b b=c c=d
   */
  private List<ItemDTO> mockBaseItemHas3Key() {
    ItemDTO item1 = new ItemDTO("a", "b", "", 1);
    ItemDTO item2 = new ItemDTO("b", "c", "", 2);
    ItemDTO item3 = new ItemDTO("c", "d", "", 3);
    return Arrays.asList(item1, item2, item3);
  }

}
