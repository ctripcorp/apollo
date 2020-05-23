package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.service.ConfigsImportService;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import the configs from file.
 * First version: move code from {@link ConfigsExportController}
 * @author wxq
 */
@RestController
public class ConfigsImportController {

  private final ConfigsImportService configsImportService;

  public ConfigsImportController(
      final ConfigsImportService configsImportService
  ) {
    this.configsImportService = configsImportService;
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import")
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("file") MultipartFile file) throws IOException {
    // check file
    ConfigFileUtils.check(file);
    final String format = ConfigFileUtils.getFormat(file.getOriginalFilename());
    final String standardFilename = ConfigFileUtils.toFilename(appId, clusterName, namespaceName, ConfigFileFormat.fromString(format));
    configsImportService.importOneConfigFromFile(env, standardFilename, file.getInputStream());
  }

  /**
   * Import multiple text file.
   * the name of config file must be special like
   * appId+cluster+namespace.format
   * Example:
   * <pre>
   *   123456+default+application.properties (appId is 123456, cluster is default, namespace is application, format is properties)
   *   654321+north+password.yml (appId is 654321, cluster is north, namespace is password, format is yml)
   * </pre>
   * so we can get the information of appId, cluster, namespace, format from the file name.
   * @param multipartFiles normal text files.
   */
  @PreAuthorize(value = "@permissionValidator.isAppAdmin(#appId)")
  @PostMapping("/envs/{env}/apps/{appId}/import")
  public Map<String, Object> importConfigFiles(
      @PathVariable final String env,
      @PathVariable final String appId,
      @RequestParam("files") MultipartFile[] multipartFiles
  ) {
    // check all files
    Stream.of(multipartFiles).forEach(ConfigFileUtils::check);

    Predicate<MultipartFile> inAppId = multipartFile ->
        appId.equals(ConfigFileUtils.getAppId(multipartFile.getOriginalFilename()));

    return Stream.of(multipartFiles).collect(
        Collectors.toMap(
            MultipartFile::getOriginalFilename,
            multipartFile -> inAppId.test(multipartFile) ?
                configsImportService.importOneConfigFromFileQuiet(env, multipartFile)
                :
                "fail. no permission with " + multipartFile.getOriginalFilename()
        )
    );
  }

  /**
   * import multiple config files.
   * the name of config file must be special like
   * appId+cluster+namespace.format
   * Example:
   * <pre>
   *   123456+default+application.properties (appId is 123456, cluster is default, namespace is application, format is properties)
   *   654321+north+password.yml (appId is 654321, cluster is north, namespace is password, format is yml)
   * </pre>
   * so we can get the information of appId, cluster, namespace, format from the file name.
   * @param env while environment's configs will be change
   * @param multipartFiles configs from files
   */
  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping("/envs/{env}/import")
  public Map<String, Object> importConfigFiles(
      @PathVariable final String env,
      @RequestParam("files") MultipartFile[] multipartFiles) {
    // check all files
    Stream.of(multipartFiles).forEach(ConfigFileUtils::check);

    final Map<String, Object> importResults = Stream.of(multipartFiles)
        .collect(
            Collectors.toMap(
                MultipartFile::getOriginalFilename,
                multipartFile -> configsImportService.importOneConfigFromFileQuiet(env, multipartFile)
            )
        );
    return importResults;
  }
}
