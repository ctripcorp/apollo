package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.entity.bo.NamespaceBO;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.service.ConfigsExportService;
import com.ctrip.framework.apollo.portal.service.NamespaceService;
import com.ctrip.framework.apollo.portal.util.NamespaceBOUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置导出 Controller
 *
 * @author jian.tan
 */
@RestController
public class ConfigsExportController {

  private static final Logger logger = LoggerFactory.getLogger(ConfigsExportController.class);

  private final ConfigsExportService configsExportService;

  private final NamespaceService namespaceService;

  public ConfigsExportController(
      final ConfigsExportService configsExportService,
      final @Lazy NamespaceService namespaceService
  ) {
    this.configsExportService = configsExportService;
    this.namespaceService = namespaceService;
  }

  /**
   * 将一个配置导出为文件。保持兼容性。文件名如：
   * <pre>
   *   application.properties
   *   application.yml
   *   application.json
   * </pre>
   *
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @param res           响应对象
   */
  @PreAuthorize(value = "!@permissionValidator.shouldHideConfigToCurrentUser(#appId, #env, #namespaceName)")
  @GetMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/export")
  public void exportItems(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      HttpServletResponse res) {
    List<String> fileNameSplit = Splitter.on(".").splitToList(namespaceName);

    // 文件名称
    String fileName = fileNameSplit.size() <= 1 ? Joiner.on(".")
        .join(namespaceName, ConfigFileFormat.Properties.getValue()) : namespaceName;
    NamespaceBO namespaceBO = namespaceService.loadNamespaceBO(appId, Env.valueOf
        (env), clusterName, namespaceName);

    // 生成文件
    res.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
    // 文件内容
    final String configFileContent = NamespaceBOUtils.convert2configFileContent(namespaceBO);
    try {
      // 写入内容至网络
      res.getOutputStream().write(configFileContent.getBytes());
    } catch (Exception e) {
      throw new ServiceException("export items failed:{}", e);
    }
  }

  /**
   * 导出压缩文件中的所有配置。只导出当前存在的命名空间，读取导出压缩文件中的所有配置。权限检查服务
   *
   * @param request  请求对象
   * @param response 响应对象
   * @throws IOException 如果发生输入或输出异常
   */
  @GetMapping("/export")
  public void exportAll(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // 文件名必须包含时间信息
    final String filename =
        "apollo_config_export_" + DateFormatUtils.format(new Date(), "yyyy_MMdd_HH_mm_ss") + ".zip";
    // 记录谁下载了配置
    logger.info("Download configs, remote addr [{}], remote host [{}]. Filename is [{}]",
        request.getRemoteAddr(), request.getRemoteHost(), filename);
    // 设置下载的文件名
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);
    // 导出
    try (OutputStream outputStream = response.getOutputStream()) {
      configsExportService.exportAllTo(outputStream);
    }
  }

}
