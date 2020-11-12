package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 配置导入 Service
 */
@Slf4j
@Service
public class ConfigsExportService {

  private final AppService appService;

  private final ClusterService clusterService;

  private final NamespaceService namespaceService;

  private final PortalSettings portalSettings;

  private final PermissionValidator permissionValidator;

  public ConfigsExportService(
      AppService appService,
      ClusterService clusterService,
      final @Lazy NamespaceService namespaceService,
      PortalSettings portalSettings,
      PermissionValidator permissionValidator) {
    this.appService = appService;
    this.clusterService = clusterService;
    this.namespaceService = namespaceService;
    this.portalSettings = portalSettings;
    this.permissionValidator = permissionValidator;
  }

  /**
   * 写入多个名称空间至zip包中. 使用 {@link Stream#reduce(Object, BiFunction, BinaryOperator)} 去禁止并发写入.
   *
   * @param configBOStream 名称空间Stream
   * @param outputStream   接收zip文件输出流
   * @throws IOException 如果发生了写入问题，抛出
   */
  private static void writeAsZipOutputStream(
      Stream<ConfigBO> configBOStream, OutputStream outputStream) throws IOException {
    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      final Consumer<ConfigBO> configBOConsumer = configBO -> {
        try {
          // TODO, Stream.reduce 会引起一些问题。有没有其他方法可以加快
          // downloading?
          synchronized (zipOutputStream) {
            write2ZipOutputStream(zipOutputStream, configBO);
          }
        } catch (IOException e) {
          log.error("Write error. {}", configBO);
          throw new IllegalStateException(e);
        }
      };
      configBOStream.forEach(configBOConsumer);
    }
  }

  /**
   * 将{@link ConfigBO}作为文件写入{@link ZipOutputStream}。小心并发问题！zip输出流就像不能并发写一样！
   * <p>
   * 文件名由 {@link ConfigFileUtils#toFilename(String, String, String, ConfigFileFormat)},文件的路径由{@link
   * ConfigFileUtils#toFilePath(String, String, Env, String)}。
   *
   * @param zipOutputStream zip文件输出流
   * @param configBO        配置业务对象
   * @return zip文件输出流与参数zipOutputStream相同
   */
  private static ZipOutputStream write2ZipOutputStream(
      final ZipOutputStream zipOutputStream, final ConfigBO configBO) throws IOException {
    // 取出配置信息
    final Env env = configBO.getEnv();
    final String ownerName = configBO.getOwnerName();
    final String appId = configBO.getAppId();
    final String clusterName = configBO.getClusterName();
    final String namespace = configBO.getNamespace();
    final String configFileContent = configBO.getConfigFileContent();
    final ConfigFileFormat configFileFormat = configBO.getFormat();

    // 配置文件名
    final String configFilename =
        ConfigFileUtils.toFilename(appId, clusterName, namespace, configFileFormat);
    // 文件路径
    final String filePath = ConfigFileUtils.toFilePath(ownerName, appId, env, configFilename);

    // 写入
    final ZipEntry zipEntry = new ZipEntry(filePath);
    try {
      zipOutputStream.putNextEntry(zipEntry);
      zipOutputStream.write(configFileContent.getBytes());
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      log.error("config export failed. {}", configBO);
      throw new IOException("config export failed", e);
    }
    return zipOutputStream;
  }

  /**
   * 将名称空间信息转为流
   *
   * @param env         环境
   * @param ownerName   所有人名称
   * @param appId       应用id
   * @param clusterName 集群名称
   * @return 配置业务信息流
   */
  private Stream<ConfigBO> makeStreamBy(
      final Env env, final String ownerName, final String appId, final String clusterName) {
    final List<NamespaceBO> namespaceBOS = namespaceService
        .findNamespaceBOs(appId, env, clusterName);
    return namespaceBOS.parallelStream()
        .map(namespaceBO -> new ConfigBO(env, ownerName, appId, clusterName, namespaceBO));
  }

  /**
   * 将集群信息转为流
   *
   * @param env       环境
   * @param ownerName 所有人名称
   * @param appId     应用id
   * @return 配置业务信息流
   */
  private Stream<ConfigBO> makeStreamBy(final Env env, final String ownerName, final String appId) {
    final List<ClusterDTO> clusterDTOS = clusterService.findClusters(env, appId);
    return clusterDTOS.parallelStream()
        .flatMap(clusterDTO -> this.makeStreamBy(env, ownerName, appId, clusterDTO.getName()));
  }

  /**
   * 将应用信息转为流
   *
   * @param env  环境
   * @param apps 应用列表
   * @return 配置业务信息流
   */
  private Stream<ConfigBO> makeStreamBy(final Env env, final List<App> apps) {
    return apps.parallelStream()
        .flatMap(app -> this.makeStreamBy(env, app.getOwnerName(), app.getAppId()));
  }

  /**
   * 将集群信息转为流
   *
   * @param envs 环境列表
   * @return 配置业务信息流
   */
  private Stream<ConfigBO> makeStreamBy(final Collection<Env> envs) {
    // get all apps
    final List<App> apps = appService.findAll();

    // 应用管理权限过滤
    final List<App> appsExistPermission =
        apps.stream().filter(app -> {
          try {
            return permissionValidator.isAppAdmin(app.getAppId());
          } catch (Exception e) {
            log.error("app = {}", app);
            log.error(app.getAppId());
          }
          return false;
        }).collect(Collectors.toList());
    return envs.parallelStream().flatMap(env -> this.makeStreamBy(env, appsExistPermission));
  }

  /**
   * 导出当前用户拥有的所有项目。{@link  PermissionValidator#isAppAdmin(java.lang.String)}
   * <p>
   *
   * @param outputStream 输出流，网络文件下载流到用户
   * @throws IOException 如果发生写入问题，抛出
   */
  public void exportAllTo(OutputStream outputStream) throws IOException {
    final List<Env> activeEnvs = portalSettings.getActiveEnvs();
    final Stream<ConfigBO> configBOStream = this.makeStreamBy(activeEnvs);
    writeAsZipOutputStream(configBOStream, outputStream);
  }
}
