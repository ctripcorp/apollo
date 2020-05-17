package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Lazy;
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

  private final ItemService configService;

  private final NamespaceService namespaceService;

  public ConfigsImportController(
      final ItemService configService,
      final @Lazy NamespaceService namespaceService) {
    this.configService = configService;
    this.namespaceService = namespaceService;
  }

  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import")
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("The file is empty.");
    }

    NamespaceDTO namespaceDTO = namespaceService
        .loadNamespaceBaseInfo(appId, Env.fromString(env), clusterName, namespaceName);

    if (Objects.isNull(namespaceDTO)) {
      throw new BadRequestException(String.format("Namespace: %s not exist.", namespaceName));
    }

    NamespaceTextModel model = new NamespaceTextModel();
    List<String> fileNameSplit = Splitter.on(".").splitToList(file.getOriginalFilename());
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }

    String format = fileNameSplit.get(fileNameSplit.size() - 1);
    model.setFormat(format);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceDTO.getId());
    String configText;
    try(InputStream in = file.getInputStream()){
      configText = ConfigToFileUtils.fileToString(in);
    }catch (IOException e) {
      throw new ServiceException("Read config file errors:{}", e);
    }
    model.setConfigText(configText);

    configService.updateConfigItemByText(model);
  }

}
