package com.ctrip.apollo.portal.service.txtresolver;

import com.ctrip.apollo.core.dto.ItemDTO;

import java.util.List;

/**
 * users can modify config in text mode.so need resolveConfigText text.
 */
public interface ConfigTextResolver {

  TextResolverResult resolve(long namespaceId, String configText, List<ItemDTO> baseItems);

}
