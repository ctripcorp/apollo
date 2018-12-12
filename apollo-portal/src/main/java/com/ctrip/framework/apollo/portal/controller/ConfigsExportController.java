package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.service.ItemService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * jian.tan
 */
@RestController
public class ConfigsExportController {

  @Autowired
  private ItemService configService;

  @Autowired
  private NamespaceService namespaceService;

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import", method = RequestMethod.POST)
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam Integer namespaceId,
      @RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("The file is empty.");
    }

    NamespaceDTO namespaceDTO = namespaceService
        .loadNamespaceBaseInfo(appId, Env.fromString(env), clusterName, namespaceName);

    if (Objects.isNull(namespaceDTO)) {
      throw new BadRequestException(String.format("Namespace: {} not exist.", namespaceName));
    }

    NamespaceTextModel model = new NamespaceTextModel();
    String fileName = file.getOriginalFilename();
    List<String> fileNameSplit = Splitter.on(".").splitToList(fileName);
    //String[] fileNameSplit = fileName.split(".");
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }

    String format = fileNameSplit.get(fileNameSplit.size() - 1);
    model.setFormat(format);
    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceId);
    String configText = "";
    try {
      configText = ConfigToFileUtils.fileToString(file.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    model.setConfigText(configText);

    configService.updateConfigItemByText(model);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/export", method = RequestMethod.GET)
  public void exportItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam(defaultValue = "lineNum") String orderBy,
      HttpServletResponse res) {
    List<String> fileNameSplit = Splitter.on(".").splitToList(namespaceName);

    String fileName = fileNameSplit.size() <= 1 ? Joiner.on(".")
        .join(namespaceName, ConfigFileFormat.Properties) : namespaceName;
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
