package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import java.util.List;

/**
 * 配置文本解析器，用户可以在文本模式下修改配置。所以需要解析文本
 */
public interface ConfigTextResolver {

  /**
   * 解析
   *
   * @param namespaceId 名称空间id
   * @param configText  配置文本
   * @param baseItems   基配置项
   * @return 解析后的配置变更列表
   */
  ItemChangeSets resolve(long namespaceId, String configText, List<ItemDTO> baseItems);

}
