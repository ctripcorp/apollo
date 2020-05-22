package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.entity.bo.ConfigBO;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
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

  private final PortalSettings portalSettings;

  public ConfigsExportService(
      AppService appService,
      ClusterService clusterService,
      final @Lazy NamespaceService namespaceService,
      PortalSettings portalSettings
  ) {
    this.appService = appService;
    this.clusterService = clusterService;
    this.namespaceService = namespaceService;
    this.portalSettings = portalSettings;
  }

  /**
   * write multiple namespace to a zip.
   * use {@link Stream#reduce(Object, BiFunction, BinaryOperator)} to forbid concurrent write.
   * @param configBOStream namespace's stream
   * @param outputStream receive zip file output stream
   * @throws IOException
   */
  public static void writeAsZipOutputStream(Stream<ConfigBO> configBOStream, OutputStream outputStream)
      throws IOException {
    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      // use reduce
      final BiFunction<ZipOutputStream, ConfigBO, ZipOutputStream> biFunction = (zipOutputStream1, configBO1) -> {
        try {
          return write2ZipOutputStream(
              zipOutputStream1, configBO1);
        } catch (IOException e) {
          logger.error("Write error. {}", configBO1);
          throw new IllegalStateException(e);
        }
      };
      final BinaryOperator<ZipOutputStream> binaryOperator = (zipOutputStream1, zipOutputStream2) -> zipOutputStream;
      configBOStream.reduce(zipOutputStream, biFunction, binaryOperator);
    }
  }

  /**
   * write {@link ConfigBO} as file to {@link ZipOutputStream}.
   * Watch out the concurrent problem!
   * zip output stream is same like cannot write concurrently!
   * the name of file is determined by {@link ConfigFileUtils#toFilename(String, String, String, ConfigFileFormat)}.
   * the path of file is determined by {@link ConfigFileUtils#toFilePath(String, String, Env, String)}.
   * @param zipOutputStream zip file output stream
   * @param configBO a namespace represent
   * @return zip file output stream same as parameter zipOutputStream
   */
  public static ZipOutputStream write2ZipOutputStream(
      final ZipOutputStream zipOutputStream,
      final ConfigBO configBO
  ) throws IOException {
    final Env env = configBO.getEnv();
    final String ownerName = configBO.getOwnerName();
    final String appId = configBO.getAppId();
    final String clusterName = configBO.getClusterName();
    final String namespace = configBO.getNamespace();
    final String configFileContent = configBO.getConfigFileContent();
    final ConfigFileFormat configFileFormat = configBO.getFormat();
    final String configFilename = ConfigFileUtils.toFilename(appId, clusterName, namespace, configFileFormat);
    final String filePath = ConfigFileUtils.toFilePath(ownerName, appId, env, configFilename);
    final ZipEntry zipEntry = new ZipEntry(filePath);
    try {
      zipOutputStream.putNextEntry(zipEntry);
      zipOutputStream.write(configFileContent.getBytes());
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      logger.error("config export failed. {}", configBO);
      throw new IOException("config export failed", e);
    }
    return zipOutputStream;
  }

  public Stream<ConfigBO> makeStreamBy(
      final Env env,
      final String ownerName,
      final String appId,
      final String clusterName
  ) {
    final List<NamespaceBO> namespaceBOS = namespaceService.findNamespaceBOs(appId, env, clusterName);
    final Function<NamespaceBO, ConfigBO> function = namespaceBO -> new ConfigBO(env, ownerName, appId, clusterName, namespaceBO);
    return namespaceBOS.parallelStream().map(function);
  }

  public Stream<ConfigBO> makeStreamBy(
      final Env env,
      final String ownerName,
      final String appId
  ) {
    final List<ClusterDTO> clusterDTOS = clusterService.findClusters(env, appId);
    final Function<ClusterDTO, Stream<ConfigBO>> function = clusterDTO -> this.makeStreamBy(env, ownerName, appId, clusterDTO.getName());
    return clusterDTOS.parallelStream().flatMap(function);
  }

  public Stream<ConfigBO> makeStreamBy(
      final Env env
  ) {
    final List<App> apps = appService.findAll();
    final Function<App, Stream<ConfigBO>> function = app -> this.makeStreamBy(env, app.getOwnerName(), app.getAppId());
    return apps.parallelStream().flatMap(function);
  }

  public Stream<ConfigBO> makeStreamBy(
      final Collection<Env> envs
  ) {
    return envs.parallelStream().flatMap(this::makeStreamBy);
  }

  public void exportBy(Env env, String appId, String clusterName, OutputStream outputStream)
      throws IOException {
    final App app = appService.findByAppId(appId);
    final String ownerName = app.getOwnerName();
    final Stream<ConfigBO> configBOStream = this.makeStreamBy(env, ownerName, appId, clusterName);
    writeAsZipOutputStream(configBOStream, outputStream);
  }

  public void exportBy(Env envEnum, String appId, OutputStream outputStream) throws IOException {
    final App app = appService.findByAppId(appId);
    final String ownerName = app.getOwnerName();
    final Stream<ConfigBO> configBOStream = this.makeStreamBy(envEnum, ownerName, appId);
    writeAsZipOutputStream(configBOStream, outputStream);
  }

  public void exportBy(final Env env, OutputStream outputStream) throws IOException {
    final Stream<ConfigBO> configBOStream = this.makeStreamBy(env);
    writeAsZipOutputStream(configBOStream, outputStream);
  }

  public void exportBy(OutputStream outputStream) throws IOException {
    final List<Env> activeEnvs = portalSettings.getActiveEnvs();
    final Stream<ConfigBO> configBOStream = this.makeStreamBy(activeEnvs);
    writeAsZipOutputStream(configBOStream, outputStream);
  }
}
