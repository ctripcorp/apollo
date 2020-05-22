package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.entity.bo.ConfigBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ConfigsExportService {

  private final static Logger logger = LoggerFactory.getLogger(ConfigsExportService.class);

  private final AppService appService;

  private final ClusterService clusterService;

  private final NamespaceService namespaceService;

  public ConfigsExportService(
      AppService appService,
      ClusterService clusterService,
      final @Lazy NamespaceService namespaceService) {
    this.appService = appService;
    this.clusterService = clusterService;
    this.namespaceService = namespaceService;
  }

  public Stream<ConfigBO> export(
      final Env env,
      final String ownerName,
      final String appId,
      final String clusterName
  ) {
    final List<NamespaceBO> namespaceBOS = namespaceService.findNamespaceBOs(appId, env, clusterName);
    final Function<NamespaceBO, ConfigBO> function = namespaceBO -> new ConfigBO(env, ownerName, appId, clusterName, namespaceBO);
    return namespaceBOS.parallelStream().map(function);
  }

  public Stream<ConfigBO> export(
      final Env env,
      final String ownerName,
      final String appId
  ) {
    final List<ClusterDTO> clusterDTOS = clusterService.findClusters(env, appId);
    final Function<ClusterDTO, Stream<ConfigBO>> function = clusterDTO -> this.export(env, ownerName, appId, clusterDTO.getName());
    return clusterDTOS.parallelStream().flatMap(function);
  }

  public Stream<ConfigBO> export(
      final Env env
  ) {
    final List<App> apps = appService.findAll();
    final Function<App, Stream<ConfigBO>> function = app -> this.export(env, app.getOwnerName(), app.getAppId());
    return apps.parallelStream().flatMap(function);
  }

  public void exportAll(final Env env, OutputStream outputStream) throws IOException {
    // write a zip file content
    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      export(env).forEach(
          configBO -> {
            final String ownerName = configBO.getOwnerName();
            final String appId = configBO.getAppId();
            final String clusterName = configBO.getClusterName();
            final String namespace = configBO.getNamespace();
            final String configFileContent = configBO.getConfigFileContent();
            final ConfigFileFormat configFileFormat = configBO.getFormat();
            synchronized (zipOutputStream) {
              final String configFilename = ConfigFileUtils
                    .toFilename(appId, clusterName, namespace, configFileFormat);
              // path = ownerName/appId/configFilename
              final ZipEntry zipEntry = new ZipEntry(String.join(File.separator, ownerName, appId, configFilename));

              try {
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(configFileContent.getBytes());
                zipOutputStream.closeEntry();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
      );
    }
  }
}
