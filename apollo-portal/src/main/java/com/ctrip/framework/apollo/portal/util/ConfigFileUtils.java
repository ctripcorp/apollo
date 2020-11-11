package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.controller.ConfigsImportController;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.google.common.base.Splitter;
import java.io.File;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 配置文件工具类，第一个版本:移自{@link ConfigsImportController#importConfigFile(java.lang.String,
 * java.lang.String, java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}
 *
 * @author wxq
 */
public class ConfigFileUtils {

  /**
   * 对文件进行检查
   *
   * @param file 多文件对象
   */
  public static void check(MultipartFile file) {
    checkEmpty(file);
    final String originalFilename = file.getOriginalFilename();
    checkFormat(originalFilename);
  }

  /**
   * 检查是否为空列表
   *
   * @param file 多文件对象
   * @throws BadRequestException if file is empty
   */
  static void checkEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("The file is empty. " + file.getOriginalFilename());
    }
  }

  /**
   * 检查格式
   *
   * @param originalFilename 原始的文件名称
   * @throws BadRequestException if file's format is invalid
   */
  static void checkFormat(final String originalFilename) {
    final List<String> fileNameSplit = Splitter.on(".").splitToList(originalFilename);
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }

    for (String s : fileNameSplit) {
      if (StringUtils.isBlank(s)) {
        throw new BadRequestException("The file format is invalid.");
      }
    }
  }

  /**
   * 将文件名切隔为3部分
   *
   * @param originalFilename 原始的文件名称
   * @return 文件名切隔后的3部分字符串数组列表
   */
  static String[] getThreePart(final String originalFilename) {
    return originalFilename.split("[+]");
  }

  /**
   * 检查文件名称是否为指定格式
   *
   * @throws BadRequestException 如果文件名称不能按“+”符号分为3部分，抛出
   */
  static void checkThreePart(final String originalFilename) {
    String[] parts = getThreePart(originalFilename);
    if (3 != parts.length) {
      throw new BadRequestException("file name [" + originalFilename + "] not valid");
    }
  }

  /**
   * 获取文件格式字符串
   * <pre>
   *  "application+default+application.properties" -> "properties"
   *  "application+default+application.yml" -> "yml"
   * </pre>
   *
   * @param originalFilename 原始的文件名称
   * @return 文件格式字符串
   * @throws BadRequestException 如果文件的格式无效，抛出
   */
  public static String getFormat(final String originalFilename) {
    // 用点切割，找最后一个
    final List<String> fileNameSplit = Splitter.on(".").splitToList(originalFilename);
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }
    return fileNameSplit.get(fileNameSplit.size() - 1);
  }

  /**
   * 获取应用id信息
   * <pre>
   *  "123+default+application.properties" -> "123"
   *  "abc+default+application.yml" -> "abc"
   *  "666+default+application.json" -> "666"
   * </pre>
   *
   * @param originalFilename 原始的文件名称
   * @return 应用id字符串
   * @throws BadRequestException 如果文件名称无效，抛出
   */
  public static String getAppId(final String originalFilename) {
    checkThreePart(originalFilename);
    return getThreePart(originalFilename)[0];
  }

  /**
   * 获取集群名称
   *
   * @param originalFilename 原始的文件名称
   * @return 集群名称字符串
   */
  public static String getClusterName(final String originalFilename) {
    // 分成三部分后，下标为1的是名称空间
    checkThreePart(originalFilename);
    return getThreePart(originalFilename)[1];
  }

  /**
   * 获取名称空间名称
   * <pre>
   *  "application+default+application.properties" -> "application"
   *  "application+default+application.yml" -> "application.yml"
   *  "application+default+application.json" -> "application.json"
   *  "application+default+application.333.yml" -> "application.333.yml"
   * </pre>
   *
   * @param originalFilename 原始的文件名称
   * @return 名称空间字符串
   * @throws BadRequestException 如果文件名称无效，抛出
   */
  public static String getNamespace(final String originalFilename) {
    checkThreePart(originalFilename);
    final String[] threeParts = getThreePart(originalFilename);
    final String suffix = threeParts[2];
    // 校验后缀
    if (!suffix.contains(".")) {
      throw new BadRequestException(originalFilename + " namespace and format is invalid!");
    }
    final int lastDotIndex = suffix.lastIndexOf(".");
    // 截取名称空间字符串
    final String namespace = suffix.substring(0, lastDotIndex);
    // 格式化后最后一个字符'.'即文件格式
    final String format = suffix.substring(lastDotIndex + 1);
    if (!ConfigFileFormat.isValidFormat(format)) {
      throw new BadRequestException(originalFilename + " format is invalid!");
    }
    // 返回名称空间字符串
    ConfigFileFormat configFileFormat = ConfigFileFormat.fromString(format);
    if (configFileFormat.equals(ConfigFileFormat.Properties)) {
      return namespace;
    } else {
      // compatibility of other format
      return namespace + "." + format;
    }
  }

  /**
   * 转换为文件名称
   * <pre>
   *   appId    cluster   namespace       return
   *   666      default   application     666+default+application.properties
   *   123      none      action.yml      123+none+action.yml
   * </pre>
   *
   * @param appId            应用id
   * @param clusterName      集群名称
   * @param namespace        名称空间名称
   * @param configFileFormat 配置文件格式
   * @return 文件名称字符串
   */
  public static String toFilename(final String appId, final String clusterName,
      final String namespace, final ConfigFileFormat configFileFormat) {
    final String suffix;
    if (ConfigFileFormat.Properties.equals(configFileFormat)) {
      suffix = "." + ConfigFileFormat.Properties.getValue();
    } else {
      suffix = "";
    }
    return appId + "+" + clusterName + "+" + namespace + suffix;
  }

  /**
   * 转换为文件路径字符串（file path = ownerName/appId/env/configFilename）
   *
   * @param ownerName      所有人名称
   * @param appId          应用id
   * @param env            环境
   * @param configFilename 配置文件名称
   * @return 转换后的压缩文件中文件路径字符串
   */
  public static String toFilePath(final String ownerName, final String appId, final Env env,
      final String configFilename) {
    return String.join(File.separator, ownerName, appId, env.getName(), configFilename);
  }
}
