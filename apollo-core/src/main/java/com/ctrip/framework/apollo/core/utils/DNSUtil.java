package com.ctrip.framework.apollo.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * DNS 工具类
 */
public class DNSUtil {

  /**
   * 解析
   *
   * @param domainName 域名
   * @return 解析后的主机地址
   * @throws UnknownHostException 如果找不到{@code host}的IP地址，或者为全局IPv6地址指定了作用域标识,抛出
   */
  public static List<String> resolve(String domainName) throws UnknownHostException {
    List<String> result = new ArrayList<>();

    InetAddress[] addresses = InetAddress.getAllByName(domainName);
    if (addresses != null) {
      for (InetAddress addr : addresses) {
        result.add(addr.getHostAddress());
      }
    }

    return result;
  }

}
