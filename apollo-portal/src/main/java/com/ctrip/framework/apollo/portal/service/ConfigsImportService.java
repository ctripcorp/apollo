package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.ctrip.framework.apollo.portal.util.MultipartFileUtils;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxq
 */
@Service
public class ConfigsImportService {

  private final static Logger logger = LoggerFactory.getLogger(ConfigsImportService.class);

  private final ItemService itemService;

  private final NamespaceService namespaceService;

  public ConfigsImportService(
      final ItemService itemService,
      NamespaceService namespaceService) {
    this.itemService = itemService;
    this.namespaceService = namespaceService;
  }

  /**
   * move from {@link com.ctrip.framework.apollo.portal.controller.ConfigsImportController}
   */
  public void importConfig(
      final String appId,
      final String env,
      final String clusterName,
      final String namespaceName,
      final long namespaceId,
      final String format,
      final String configText
  ) {
    final NamespaceTextModel model = new NamespaceTextModel();

    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceId);
    model.setFormat(format);
    model.setConfigText(configText);

    itemService.updateConfigItemByText(model);
  }

  /**
   * import one config from file
   */
  public void importOneConfigFromFile(
      final String appId,
      final String env,
      final String clusterName,
      final String namespaceName,
      final MultipartFile file
  ) {
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

    this.importConfig(appId, env, clusterName, namespaceName, namespaceDTO.getId(), format, configText);
  }

  /**
   * import a config file.
   * the name of config file must be special like
   * appId+cluster+namespace.format
   * Example:
   * <pre>
   *   123456+default+application.properties (appId is 123456, cluster is default, namespace is application, format is properties)
   *   654321+north+password.yml (appId is 654321, cluster is north, namespace is password, format is yml)
   * </pre>
   * so we can get the information of appId, cluster, namespace, format from the file name.
   * @param env environment
   * @param file appId+cluster+namespace.format
   * @throws BadRequestException if file not valid
   */
  public void importOneConfigFromFile(
      final String env,
      final MultipartFile file
  ) {
    MultipartFileUtils.check(file);
    final String originalFilename = file.getOriginalFilename();
    final String appId = MultipartFileUtils.getAppId(originalFilename);
    final String clusterName = MultipartFileUtils.getClusterName(originalFilename);
    final String namespace = MultipartFileUtils.getNamespace(originalFilename);
    this.importOneConfigFromFile(appId, env, clusterName, namespace, file);
  }

  public String importOneConfigFromFileQuiet(
      final String env,
      final MultipartFile file
  ) {
    final String originalFilename = file.getOriginalFilename();
    try {
      importOneConfigFromFile(env, file);
    } catch (Exception e) {
      logger.error("import " + originalFilename + " fail. ", e);
      return "import " + originalFilename + " fail. " + e.getMessage();
    }
    return "import " + originalFilename + " success. ";
  }

}
