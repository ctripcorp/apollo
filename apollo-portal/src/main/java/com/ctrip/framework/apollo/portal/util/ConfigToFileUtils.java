package com.ctrip.framework.apollo.portal.util;

import com.ctrip.framework.apollo.common.dto.ItemDTO;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * jian.tan
 */
public class ConfigToFileUtils {

  public static void itemsToFile(OutputStream os, List<String> items) {
    try {
      PrintWriter printWriter = new PrintWriter(os);
      items.forEach(printWriter::println);
      printWriter.close();
    } catch (Exception e) {
      throw e;
    }
  }

  public static String fileToString(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
  }

  public static void downloadNamespace(OutputStream outputStream, List<ItemDTO> items, boolean isProperties) throws IOException {
    String content;
    if (isProperties) {
      StringBuilder stringBuilder = new StringBuilder();
      items.sort(Comparator.comparingInt(ItemDTO::getLineNum));
      for (ItemDTO item : items) {
        if (item.getComment() != null) {
          stringBuilder.append("# ").append(item.getComment()).append("\r\n");
        }
        String line = item.getKey() + "=" + (item.getValue() == null ? "" : item.getValue());
        stringBuilder.append(line).append("\r\n");
      }
      content = stringBuilder.toString();
    } else {
      // exclude properties config,other's config can get value from item
      content = items.get(0).getValue();
    }
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(UTF_8));
    IOUtils.copy(byteArrayInputStream, outputStream);
  }


  public static boolean isPropertiesNamespace(String namespaceName) {
    String[] split  = namespaceName.split("\\.");
    String   suffix = split[split.length - 1];
    return !"properties".equalsIgnoreCase(suffix);
  }

}
