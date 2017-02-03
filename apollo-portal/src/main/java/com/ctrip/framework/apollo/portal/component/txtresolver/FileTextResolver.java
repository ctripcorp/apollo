package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component("fileTextResolver")
public class FileTextResolver implements ConfigTextResolver {


  @Override
  public ItemChangeSets resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
    ItemChangeSets changeSets = new ItemChangeSets();
    if (StringUtils.isEmpty(configText)) {
      return changeSets;
    }
    if (CollectionUtils.isEmpty(baseItems)) {
      changeSets.addCreateItem(createItem(namespaceId, 0, configText));
    } else {
      ItemDTO beforeItem = baseItems.get(0);
      if (!configText.equals(beforeItem.getValue())) {//update
        changeSets.addUpdateItem(createItem(namespaceId, beforeItem.getId(), configText));
      }
    }

    return changeSets;
  }

  private ItemDTO createItem(long namespaceId, long itemId, String value) {
    ItemDTO item = new ItemDTO();
    item.setId(itemId);
    item.setNamespaceId(namespaceId);
    item.setValue(value);
    item.setLineNum(1);
    item.setKey(ConfigConsts.CONFIG_FILE_CONTENT_KEY);
    return item;
  }
}
