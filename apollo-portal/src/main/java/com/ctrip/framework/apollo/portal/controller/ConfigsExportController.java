package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * jian.tan
 */
@RestController
public class ConfigsExportController {

  private final NamespaceService namespaceService;

  public ConfigsExportController(
      final @Lazy NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

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
    res.setHeader("Content-Disposition", "attachment;filename=" + fileName);

    List<String> fileItems = namespaceBO.getItems().stream().map(itemBO -> {
      String key = itemBO.getItem().getKey();
      String value = itemBO.getItem().getValue();
      if (ConfigConsts.CONFIG_FILE_CONTENT_KEY.equals(key)) {
        return value;
      }

      if ("".equals(key)) {
        return Joiner.on("").join(itemBO.getItem().getKey(), itemBO.getItem().getValue());
      }

      return Joiner.on(" = ").join(itemBO.getItem().getKey(), itemBO.getItem().getValue());
    }).collect(Collectors.toList());

    try {
      ConfigToFileUtils.itemsToFile(res.getOutputStream(), fileItems);
    } catch (Exception e) {
      throw new ServiceException("export items failed:{}", e);
    }
  }
}
