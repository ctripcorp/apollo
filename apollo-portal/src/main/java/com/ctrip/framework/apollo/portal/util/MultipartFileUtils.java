package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.portal.controller.ConfigsImportController;
import com.google.common.base.Splitter;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * First version: move from {@link ConfigsImportController#importConfigFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}
 * @author wxq
 */
public class MultipartFileUtils {

  public static void check(MultipartFile file) {
    checkEmpty(file);
    final String originalFilename = file.getOriginalFilename();
    checkFormat(originalFilename);
  }

  /**
   * @throws BadRequestException if file is empty
   */
  static void checkEmpty(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("The file is empty.");
    }
  }

  /**
   * @throws BadRequestException if file's format is invalid
   */
  static void checkFormat(final String originalFilename) {
    final List<String> fileNameSplit = Splitter.on(".").splitToList(originalFilename);
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }
  }

  /**
   * "application+default+application.properties" -> "properties"
   * "application+default+application.yml" -> "yml"
   * @throws BadRequestException if file's format is invalid
   */
  public static String getFormat(final String originalFilename) {
    final List<String> fileNameSplit = Splitter.on(".").splitToList(originalFilename);
    if (fileNameSplit.size() <= 1) {
      throw new BadRequestException("The file format is invalid.");
    }
    return fileNameSplit.get(fileNameSplit.size() - 1);
  }
}
