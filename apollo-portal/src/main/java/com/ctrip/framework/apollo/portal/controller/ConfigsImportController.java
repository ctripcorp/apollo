package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.service.ConfigsImportService;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import java.io.IOException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 配置导入 Controller, 从文件中导入配置.第一个版本：从{@link ConfigsExportController}中移动代码
 *
 * @author wxq
 */
@RestController
public class ConfigsImportController {

  private final ConfigsImportService configsImportService;

  public ConfigsImportController(final ConfigsImportService configsImportService) {
    this.configsImportService = configsImportService;
  }

  /**
   * 导入配置文件， 从旧的{@link ConfigsExportController}拷贝.
   *
   * @param file          Yml文件名必须以{@code .Yml}结尾。属性文件名必须以{@code.Properties}结尾等。
   * @param appId         应用id
   * @param env           环境
   * @param clusterName   集群名称
   * @param namespaceName 名称空间名称
   * @throws IOException 如果出现访问错误（如果临时存储失败）
   */
  @PreAuthorize(value = "@permissionValidator.hasModifyNamespacePermission(#appId, #namespaceName, #env)")
  @PostMapping("/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/items/import")
  public void importConfigFile(@PathVariable String appId, @PathVariable String env,
      @PathVariable String clusterName, @PathVariable String namespaceName,
      @RequestParam("file") MultipartFile file) throws IOException {
    // 检查文件
    ConfigFileUtils.check(file);
    // 获取文件格式类型
    final String format = ConfigFileUtils.getFormat(file.getOriginalFilename());
    // 标准化文件名称
    final String standardFilename = ConfigFileUtils.toFilename(appId, clusterName, namespaceName,
        ConfigFileFormat.fromString(format));
    //从文件中导入配置
    configsImportService.importOneConfigFromFile(env, standardFilename, file.getInputStream());
  }
}
