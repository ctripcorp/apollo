package com.ctrip.apollo.portal;

import com.ctrip.apollo.core.ConfigConsts;
import com.ctrip.apollo.core.dto.ItemChangeSets;
import com.ctrip.apollo.core.dto.ItemDTO;
import com.ctrip.apollo.core.dto.NamespaceDTO;
import com.ctrip.apollo.core.dto.ReleaseDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.ItemDiffs;
import com.ctrip.apollo.portal.entity.NamespaceIdentifer;
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
public class ConfigServiceTest {

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
    when(releaseAPI.loadLatestRelease(appId, Env.DEV, clusterName, "hermas")).thenReturn(someRelease);
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

  @Test
  public void testCompareTargetNamespaceHasNoItems(){
    ItemDTO sourceItem1 = new ItemDTO("a","b","comment",1);
    List<ItemDTO> sourceItems = Arrays.asList(sourceItem1);

    String appId = "6666", env = "LOCAL", clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT,
        namespaceName = ConfigConsts.NAMESPACE_DEFAULT;
    List<NamespaceIdentifer> namespaceIdentifers = generateNamespaceIdentifer(appId, env, clusterName, namespaceName);
    NamespaceDTO namespaceDTO = generateNamespaceDTO(appId, clusterName, namespaceName);

    when(namespaceAPI.loadNamespace(appId, Env.valueOf(env), clusterName, namespaceName)).thenReturn(namespaceDTO);
    when(itemAPI.findItems(appId, Env.valueOf(env), clusterName, namespaceName)).thenReturn(null);

    List<ItemDiffs> itemDiffses = configService.compare(sourceItems, namespaceIdentifers);

    assertEquals(1,itemDiffses.size());
    ItemDiffs itemDiffs = itemDiffses.get(0);
    ItemChangeSets changeSets = itemDiffs.getDiffs();
    assertEquals(0, changeSets.getUpdateItems().size());
    assertEquals(0, changeSets.getDeleteItems().size());

    List<ItemDTO> createItems = changeSets.getCreateItems();
    ItemDTO createItem = createItems.get(0);
    assertEquals(1, createItem.getLineNum());
    assertEquals("a", createItem.getKey());
    assertEquals("b", createItem.getValue());
    assertEquals("comment", createItem.getComment());
  }

  @Test
  public void testCompare(){
    ItemDTO sourceItem1 = new ItemDTO("a","b","comment",1);//not modified
    ItemDTO sourceItem2 = new ItemDTO("newKey","c","comment",2);//new item
    ItemDTO sourceItem3 = new ItemDTO("c","newValue","comment",3);// update value
    ItemDTO sourceItem4 = new ItemDTO("d","b","newComment",4);// update comment
    List<ItemDTO> sourceItems = Arrays.asList(sourceItem1, sourceItem2, sourceItem3, sourceItem4);

    ItemDTO targetItem1 = new ItemDTO("a","b","comment",1);
    ItemDTO targetItem2 = new ItemDTO("c","oldValue","comment",2);
    ItemDTO targetItem3 = new ItemDTO("d","b","oldComment",3);
    List<ItemDTO> targetItems = Arrays.asList(targetItem1, targetItem2, targetItem3);

    String appId = "6666", env = "LOCAL", clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT,
        namespaceName = ConfigConsts.NAMESPACE_DEFAULT;
    List<NamespaceIdentifer> namespaceIdentifers = generateNamespaceIdentifer(appId, env, clusterName, namespaceName);
    NamespaceDTO namespaceDTO = generateNamespaceDTO(appId, clusterName, namespaceName);

    when(namespaceAPI.loadNamespace(appId, Env.valueOf(env), clusterName, namespaceName)).thenReturn(namespaceDTO);
    when(itemAPI.findItems(appId, Env.valueOf(env), clusterName, namespaceName)).thenReturn(targetItems);

    List<ItemDiffs> itemDiffses = configService.compare(sourceItems, namespaceIdentifers);
    assertEquals(1, itemDiffses.size());

    ItemDiffs itemDiffs = itemDiffses.get(0);

    ItemChangeSets changeSets = itemDiffs.getDiffs();
    assertEquals(0, changeSets.getDeleteItems().size());
    assertEquals(2, changeSets.getUpdateItems().size());
    assertEquals(1, changeSets.getCreateItems().size());

    NamespaceIdentifer namespaceIdentifer = itemDiffs.getNamespace();
    assertEquals(appId, namespaceIdentifer.getAppId());
    assertEquals(Env.valueOf("LOCAL"), namespaceIdentifer.getEnv());
    assertEquals(clusterName, namespaceIdentifer.getClusterName());
    assertEquals(namespaceName, namespaceIdentifer.getNamespaceName());

    ItemDTO createdItem = changeSets.getCreateItems().get(0);
    assertEquals("newKey", createdItem.getKey());
    assertEquals("c", createdItem.getValue());
    assertEquals("comment", createdItem.getComment());
    assertEquals(4, createdItem.getLineNum());

    List<ItemDTO> updateItems = changeSets.getUpdateItems();
    ItemDTO updateItem1 = updateItems.get(0);
    ItemDTO updateItem2 = updateItems.get(1);
    assertEquals("c", updateItem1.getKey());
    assertEquals("newValue", updateItem1.getValue());
    assertEquals("comment", updateItem1.getComment());
    assertEquals(2, updateItem1.getLineNum());

    assertEquals("d", updateItem2.getKey());
    assertEquals("b", updateItem2.getValue());
    assertEquals("newComment", updateItem2.getComment());
    assertEquals(3, updateItem2.getLineNum());


  }

  private NamespaceDTO generateNamespaceDTO(String appId, String clusterName, String namespaceName){
    NamespaceDTO namespaceDTO = new NamespaceDTO();
    namespaceDTO.setAppId(appId);
    namespaceDTO.setId(1);
    namespaceDTO.setClusterName(clusterName);
    namespaceDTO.setNamespaceName(namespaceName);
    return namespaceDTO;
  }

  private List<NamespaceIdentifer> generateNamespaceIdentifer(String appId, String env, String clusterName, String namespaceName){
    NamespaceIdentifer targetNamespace = new NamespaceIdentifer();
    targetNamespace.setAppId(appId);
    targetNamespace.setEnv(env);
    targetNamespace.setClusterName(clusterName);
    targetNamespace.setNamespaceName(namespaceName);
    return Arrays.asList(targetNamespace);
  }

}
