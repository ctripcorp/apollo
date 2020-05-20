package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ConfigsExportService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * jian.tan
 */
@RestController
@RequestMapping("/apps")
public class ConfigsExportController {

  private final ConfigsExportService configsExportService;

  private final NamespaceService namespaceService;

  private final PortalSettings portalSettings;

  public ConfigsExportController(
      final ConfigsExportService configsExportService,
      final @Lazy NamespaceService namespaceService,
      PortalSettings portalSettings) {
    this.configsExportService = configsExportService;
    this.namespaceService = namespaceService;
    this.portalSettings = portalSettings;
  }

  @GetMapping("/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/export")
  public void exportItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      HttpServletResponse res) {
    List<String> fileNameSplit = Splitter.on(".").splitToList(namespaceName);

    String fileName = fileNameSplit.size() <= 1 ? Joiner.on(".")
        .join(namespaceName, ConfigFileFormat.Properties.getValue()) : namespaceName;
    NamespaceBO namespaceBO = namespaceService.loadNamespaceBO(appId, Env.fromString
        (env), clusterName, namespaceName);

    //generate a file.
    res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
    // file content
    final String configFileContent = NamespaceBOUtils.convert2configFileContent(namespaceBO);
    try {
      // write content to net
      res.getOutputStream().write(configFileContent.getBytes());
    } catch (Exception e) {
      throw new ServiceException("export items failed:{}", e);
    }
  }

  // add permission, TODO
  @GetMapping("/envs/{env}/export/all")
  public void exportAll(
      @PathVariable String env,
      HttpServletResponse response
  ) throws IOException {
    final Env envEnum = Env.valueOf(env);
    response.setHeader("Content-Disposition", "attachment;filename=" + envEnum.toString() + ".zip");
    try (OutputStream outputStream = response.getOutputStream()) {
      configsExportService.exportAll(envEnum, outputStream);
    }
  }

}
