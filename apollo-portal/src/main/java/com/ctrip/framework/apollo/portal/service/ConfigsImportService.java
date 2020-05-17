package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import org.springframework.stereotype.Service;

/**
 * @author wxq
 */
@Service
public class ConfigsImportService {

  private final ItemService itemService;

  public ConfigsImportService(
      final ItemService itemService
  ) {
    this.itemService = itemService;
  }

  /**
   * move from {@link com.ctrip.framework.apollo.portal.controller.ConfigsImportController}
   */
  public void importConfig(
      final String appId,
      final String env,
      final String clusterName,
      final String namespaceName,
      final long namespaceId,
      final String format,
      final String configText
  ) {
    final NamespaceTextModel model = new NamespaceTextModel();

    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceId);
    model.setFormat(format);
    model.setConfigText(configText);

    itemService.updateConfigItemByText(model);
  }

}
