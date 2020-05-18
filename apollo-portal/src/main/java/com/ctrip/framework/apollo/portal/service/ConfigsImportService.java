package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.ctrip.framework.apollo.portal.util.MultipartFileUtils;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxq
 */
@Service
public class ConfigsImportService {

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
      MultipartFile file
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
}
