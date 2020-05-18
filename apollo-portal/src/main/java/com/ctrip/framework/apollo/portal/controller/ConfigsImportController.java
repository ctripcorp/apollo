package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ConfigsImportService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.ctrip.framework.apollo.portal.util.MultipartFileUtils;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Lazy;
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

  private final NamespaceService namespaceService;

  private final ConfigsImportService configsImportService;

  public ConfigsImportController(
      final @Lazy NamespaceService namespaceService,
      final ConfigsImportService configsImportService) {
    this.namespaceService = namespaceService;
    this.configsImportService = configsImportService;
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import")
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("file") MultipartFile file) {
    // check file
    MultipartFileUtils.check(file);
    // get file format
    final String format = MultipartFileUtils.getFormat(file.getOriginalFilename());

    final NamespaceDTO namespaceDTO = namespaceService
        .loadNamespaceBaseInfo(appId, Env.valueOf(env), clusterName, namespaceName);

    final String configText;
    try(InputStream in = file.getInputStream()){
      configText = ConfigToFileUtils.fileToString(in);
    } catch (IOException e) {
      throw new ServiceException("Read config file errors:{}", e);
    }

    configsImportService.importConfig(appId, env, clusterName, namespaceName, namespaceDTO.getId(), format, configText);
  }

}
