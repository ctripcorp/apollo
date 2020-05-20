package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
      final String configText,
      final String format
  ) {
    final NamespaceDTO namespaceDTO = namespaceService
        .loadNamespaceBaseInfo(appId, Env.valueOf(env), clusterName, namespaceName);
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
   * @param standardFilename appId+cluster+namespace.format
   * @param configText config content
   */
  public void importOneConfigFromText(
      final String env,
      final String standardFilename,
      final String configText
  ) {
    final String appId = ConfigFileUtils.getAppId(standardFilename);
    final String clusterName = ConfigFileUtils.getClusterName(standardFilename);
    final String namespace = ConfigFileUtils.getNamespace(standardFilename);
    final String format = ConfigFileUtils.getFormat(standardFilename);
    this.importOneConfigFromFile(appId, env, clusterName, namespace, configText, format);
  }

  /**
   * @see ConfigsImportService#importOneConfigFromText(java.lang.String, java.lang.String, java.lang.String)
   */
  public void importOneConfigFromFile(
      final String env,
      final String standardFilename,
      final InputStream inputStream
  ) {
    final String configText;
    try(InputStream in = inputStream) {
      configText = ConfigToFileUtils.fileToString(in);
    } catch (IOException e) {
      throw new ServiceException("Read config file errors:{}", e);
    }
    this.importOneConfigFromText(env, standardFilename, configText);
  }

  /**
   * There are many files in a zip file.
   * @param env environment
   * @param inputStream input stream of zip file
   * @return key is filename, value is import result of this file
   * @throws IOException meet read exception
   */
  public Map<String, String> importConfigsFromZipFile(
      final String env,
      final InputStream inputStream
  ) throws IOException {
    final Map<String, String> map = new ConcurrentHashMap<>();
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    for (
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        null != zipEntry;
        zipEntry = zipInputStream.getNextEntry()
    ) {
      // handle path through file
      final File file = new File(zipEntry.getName());
      // get last
      final String standardFilename = file.getName();
      final String configText = ConfigToFileUtils.fileToString(zipInputStream);
      try {
        importOneConfigFromText(env, standardFilename, configText);
        map.put(standardFilename, "0");
      } catch (Exception e) {
        logger.error("", e);
        map.put(standardFilename, e.getMessage());
      }
    }
    return map;
  }

  /**
   * @param env environment
   * @param file file uploaded, maybe a zip file
   * @return import result. 0 if success
   */
  public Object importOneConfigFromFileQuiet(
      final String env,
      final MultipartFile file
  ) {
    final String originalFilename = file.getOriginalFilename();
    try {
      if (Objects.requireNonNull(file.getContentType()).endsWith("zip")) {
        // a compressed zip file
        return importConfigsFromZipFile(env, file.getInputStream());
      } else {
        importOneConfigFromFile(env, originalFilename, file.getInputStream());
      }
    } catch (Exception e) {
      logger.error("import " + originalFilename + " fail. ", e);
      return "fail. " + e.getMessage();
    }
    return "0";
  }

}
