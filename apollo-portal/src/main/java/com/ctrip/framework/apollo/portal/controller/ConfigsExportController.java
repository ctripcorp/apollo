package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ConfigsExportService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * jian.tan
 */
@RestController
public class ConfigsExportController {

  private final ConfigsExportService configsExportService;

  private final NamespaceService namespaceService;

  public ConfigsExportController(
      final ConfigsExportService configsExportService,
      final @Lazy NamespaceService namespaceService
  ) {
    this.configsExportService = configsExportService;
    this.namespaceService = namespaceService;
  }

  // add permission, TODO
  /**
   * export one config as file.
   * file name examples:
   * <pre>
   *   application.properties
   *   application.yml
   *   application.json
   * </pre>
   */
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/export")
  public void exportItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      HttpServletResponse res) {
    List<String> fileNameSplit = Splitter.on(".").splitToList(namespaceName);

    String fileName = fileNameSplit.size() <= 1 ? Joiner.on(".")
        .join(namespaceName, ConfigFileFormat.Properties.getValue()) : namespaceName;
    NamespaceBO namespaceBO = namespaceService.loadNamespaceBO(appId, Env.fromString
        (env), clusterName, namespaceName);

    //generate a file.
    res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
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
  /**
   * export one config as file.
   * file name examples:
   * <pre>
   *   123455+default+application.properties
   *   123455+beijing+application.yml
   *   765323+shanghai+application.json
   * </pre>
   */
  @GetMapping("/envs/{env}/apps/{appId}/clusters/{clusterName}/namespaces/{namespaceName}/export")
  public void exportBy(
      @PathVariable String env,
      @PathVariable String appId,
      @PathVariable String clusterName,
      @PathVariable String namespaceName,
      HttpServletResponse response
  ) throws IOException {
    final NamespaceBO namespaceBO = namespaceService.loadNamespaceBO(appId, Env.valueOf(env), clusterName, namespaceName);
    final String configFileContent = NamespaceBOUtils.convert2configFileContent(namespaceBO);
    final ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(namespaceBO.getFormat());
    // set download file name
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + ConfigFileUtils.toFilename(appId, clusterName, namespaceName, configFileFormat));
    try (OutputStream outputStream = response.getOutputStream()) {
      // write content
      outputStream.write(configFileContent.getBytes());
    }
  }

  // add permission, TODO
  /**
   * export multiple configs in a compressed file.
   */
  @GetMapping("/envs/{env}/apps/{appId}/clusters/{clusterName}/export")
  public void exportBy(
      @PathVariable String env,
      @PathVariable String appId,
      @PathVariable String clusterName,
      HttpServletResponse response
  ) throws IOException {
    final Env envEnum = Env.valueOf(env);
    final String downloadFilename = String.join("+", Arrays.asList(env, appId, clusterName)) + ".zip";
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + downloadFilename);
    try (OutputStream outputStream = response.getOutputStream()) {
      // write content
      configsExportService.exportBy(envEnum, appId, clusterName, outputStream);
    }
  }

  // add permission, TODO
  /**
   * export multiple configs in a compressed file.
   */
  @GetMapping("/envs/{env}/apps/{appId}/export")
  public void exportBy(
      @PathVariable String env,
      @PathVariable String appId,
      HttpServletResponse response
  ) throws IOException {
    final Env envEnum = Env.valueOf(env);
    final String downloadFilename = String.join("+", Arrays.asList(env, appId)) + ".zip";
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + downloadFilename);
    try (OutputStream outputStream = response.getOutputStream()) {
      // write content
      configsExportService.exportBy(envEnum, appId, outputStream);
    }
  }

  // add permission, TODO
  /**
   * export an environment's configs in a compressed file.
   */
  @GetMapping("/envs/{env}/export")
  public void exportBy(
      @PathVariable String env,
      HttpServletResponse response
  ) throws IOException {
    final Env envEnum = Env.valueOf(env);
    final String downloadFilename = envEnum.getName() + ".zip";
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + downloadFilename);
    try (OutputStream outputStream = response.getOutputStream()) {
      configsExportService.exportBy(envEnum, outputStream);
    }
  }

  // add permission, TODO
  /**
   * export all configs in a compressed file.
   */
  @GetMapping("/export")
  public void exportBy(
      HttpServletResponse response,
      @RequestParam(value = "filename", defaultValue = "AllConfigs") final String filename
  ) throws IOException {
    final String downloadFilename = filename + ".zip";
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + downloadFilename);
    try (OutputStream outputStream = response.getOutputStream()) {
      configsExportService.exportBy(outputStream);
    }
  }

}
