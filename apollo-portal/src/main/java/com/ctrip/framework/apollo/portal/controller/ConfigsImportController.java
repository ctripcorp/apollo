package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.service.ConfigsImportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import the configs from file.
 * First version: move code from {@link ConfigsExportController}
 * @author wxq
 */
@RestController
@RequestMapping("/apps")
public class ConfigsImportController {

  private final ConfigsImportService configsImportService;

  public ConfigsImportController(
      final ConfigsImportService configsImportService
  ) {
    this.configsImportService = configsImportService;
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import")
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("file") MultipartFile file) {
    configsImportService.importOneConfigFromFile(appId, env, clusterName, namespaceName, file);
  }

}
