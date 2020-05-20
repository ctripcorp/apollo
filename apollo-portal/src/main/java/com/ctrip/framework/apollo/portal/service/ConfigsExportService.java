package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
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

  public void exportAll(Env env, OutputStream outputStream) throws IOException {
    // write a zip file content
    final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

    final List<App> apps = appService.findAll();
    logger.info("export {} app's config", apps.size());
    for (final App app : apps) {
      final String ownerName = app.getOwnerName();
      final String appId = app.getAppId();
      // find clusters
      final List<ClusterDTO> clusterDTOS = clusterService.findClusters(env, appId);
      for (final ClusterDTO clusterDTO : clusterDTOS) {
        final String clusterName = clusterDTO.getName();
        // find namespaces in one cluster
        final List<NamespaceBO> namespaceBOS = namespaceService.findNamespaceBOs(appId, env, clusterName);
        for (final NamespaceBO namespaceBO : namespaceBOS) {
          final String namespace = namespaceBO.getBaseInfo().getNamespaceName();
          final ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(namespaceBO.getFormat());
          final String configFileContent = NamespaceBOUtils.convert2configFileContent(namespaceBO);
          // put path
          final String configFilename = ConfigFileUtils.toFilename(appId, clusterName, namespace, configFileFormat);
          // path = ownerName/appId/configFilename
          final ZipEntry zipEntry = new ZipEntry(String.join(File.separator, ownerName, appId, configFilename));

          zipOutputStream.putNextEntry(zipEntry);
          zipOutputStream.write(configFileContent.getBytes());
          zipOutputStream.closeEntry();
        }
      }
    }
    zipOutputStream.close();
  }
}
