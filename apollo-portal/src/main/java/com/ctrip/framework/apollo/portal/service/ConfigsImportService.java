package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 配置导入 Service
 *
 * @author wxq
 */
@Service
public class ConfigsImportService {

  private final ItemService itemService;

  private final NamespaceService namespaceService;

  private final PermissionValidator permissionValidator;

  public ConfigsImportService(final ItemService itemService,
      final @Lazy NamespaceService namespaceService, PermissionValidator permissionValidator) {
    this.itemService = itemService;
    this.namespaceService = namespaceService;
    this.permissionValidator = permissionValidator;
  }

  /**
   * 导入配置，从 {@link com.ctrip.framework.apollo.portal.controller.ConfigsImportController}移过来的
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param namespaceId   名称空间id
   * @param format        称空间格式（后缀）类型
   * @param configText    配置项文本
   */
  private void importConfig(final String appId, final String env, final String clusterName,
      final String namespaceName, final long namespaceId, final String format,
      final String configText) {
    // 设置名称空间文本 Model
    final NamespaceTextModel model = new NamespaceTextModel();

    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceId);
    model.setFormat(format);
    model.setConfigText(configText);

    // 更新配置项
    itemService.updateConfigItemByText(model);
  }

  /**
   * 从文本中导入一个配置
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param format        称空间格式（后缀）类型
   * @param configText    配置项文本
   */

  private void importOneConfigFromFile(final String appId, final String env,
      final String clusterName, final String namespaceName, final String configText,
      final String format) {
    // 名称空间信息
    final NamespaceDTO namespaceDTO = namespaceService.loadNamespaceBaseInfo(appId,
        Env.valueOf(env), clusterName, namespaceName);
    // 导入配置
    this.importConfig(appId, env, clusterName, namespaceName, namespaceDTO.getId(), format,
        configText);
  }

  /**
   * 导入配置文件。配置文件的名称必须是特殊的，如appId+cluster+namespace.格式,示例:
   * <pre>
   *   123456+default+application.properties (appId = 123456, cluster = default, namespace = application, format = properties)
   *   654321+north+password.yml (appId = 654321, cluster = north, namespace = password, format = yml)
   * </pre>
   * 因此，我们可以从文件名中获取appId、集群、名称空间、格式等信息。
   *
   * @param env              环境
   * @param standardFilename 标准化文件名称，appId+cluster+namespace.format
   * @param configText       配置内容
   */
  private void importOneConfigFromText(final String env, final String standardFilename,
      final String configText) {
    // 从标准化的文件名称中获取基本信息
    final String appId = ConfigFileUtils.getAppId(standardFilename);
    final String clusterName = ConfigFileUtils.getClusterName(standardFilename);
    final String namespace = ConfigFileUtils.getNamespace(standardFilename);
    final String format = ConfigFileUtils.getFormat(standardFilename);
    // 导入配置
    this.importOneConfigFromFile(appId, env, clusterName, namespace, configText, format);
  }

  /**
   * 从文件中导入配置
   *
   * @param env              环境
   * @param standardFilename 标准化文件名称
   * @param inputStream      输入流
   * @throws AccessControlException if has no modify namespace permission
   * @see ConfigsImportService#importOneConfigFromText(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void importOneConfigFromFile(final String env, final String standardFilename,
      final InputStream inputStream) {
    final String configText;
    // 获取文件内容
    try (InputStream in = inputStream) {
      configText = ConfigToFileUtils.fileToString(in);
    } catch (IOException e) {
      throw new ServiceException("Read config file errors:{}", e);
    }
    // 从文本中导入配置
    this.importOneConfigFromText(env, standardFilename, configText);
  }
}
