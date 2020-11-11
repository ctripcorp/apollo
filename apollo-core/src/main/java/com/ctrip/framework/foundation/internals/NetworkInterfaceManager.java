package com.ctrip.framework.foundation.internals;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * 网络接口管理器枚举
 * <p>
 * 关于 {@code findValidateIp()} -> https://github.com/ctripcorp/apollo/issues/1457
 */
public enum NetworkInterfaceManager {
  INSTANCE;
  /**
   * 主机地址，即ip.默认为本地的环回地址
   */
  private InetAddress local;
  /**
   * 主机名
   */
  private InetAddress localHost;

  NetworkInterfaceManager() {
    load();
  }

  /**
   * 找到权重最大的ip
   *
   * @param addresses 地址列表
   * @return 权重最大的ip
   */
  public InetAddress findValidateIp(List<InetAddress> addresses) {
    InetAddress local = null;
    int maxWeight = -1;
    for (InetAddress address : addresses) {
      //地址为IP4的情况
      if (address instanceof Inet4Address) {
        int weight = 0;
        // SiteLocalAddress权重 > LinkLocalAddress权重 > LoopBackAddress权重
        // 当IP地址是地区本地地址（SiteLocalAddress）时
        if (address.isSiteLocalAddress()) {
          weight += 8;
        }
        // 当IP地址是本地连接地址（LinkLocalAddress）时
        if (address.isLinkLocalAddress()) {
          weight += 4;
        }

        // 当IP地址是loopback地址时
        if (address.isLoopbackAddress()) {
          weight += 2;
        }

        /**
         * 下面的逻辑被删除，因为我们将根据索引asc对网络接口进行排序，以确定网络接口之间的优先级， https://github.com/ctripcorp/apollo/pull/1986
         */
        // has host name
        /*
        if (!Objects.equals(address.getHostName(), address.getHostAddress())) {
          weight += 1;
        }
        */

        if (weight > maxWeight) {
          maxWeight = weight;
          local = address;
        }
      }
    }
    return local;
  }

  /**
   * 获取主机地址，即ip.
   *
   * @return 主机地址，即ip
   */
  public String getLocalHostAddress() {
    return local.getHostAddress();
  }

  /**
   * 获取主机名.
   *
   * @return 主机名
   */
  public String getLocalHostName() {
    try {
      if (null == localHost) {
        localHost = InetAddress.getLocalHost();
      }
      return localHost.getHostName();
    } catch (UnknownHostException e) {
      return local.getHostName();
    }
  }

  /**
   * 返回具有给定名称的属性值
   *
   * @param name 给定的名称
   * @return 具有给定名称的属性值
   */
  private String getProperty(String name) {
    String value;

    value = System.getProperty(name);

    if (value == null) {
      value = System.getenv(name);
    }

    return value;
  }

  /**
   * 加载主机地址(IP)或主机名
   */
  private void load() {
    String ip = getProperty("host.ip");

    if (ip != null) {
      try {
        //设置为网络ip的InetAddress实例
        local = InetAddress.getByName(ip);
        return;
      } catch (Exception e) {
        System.err.println(e);
        // ignore
      }
    }

    try {
      // 获得本机的所有网络接口
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      List<NetworkInterface> nis = interfaces == null ? Collections.<NetworkInterface>emptyList()
          : Collections.list(interfaces);
      //对网络接口根据索引进行升序排序
      Collections.sort(nis, new Comparator<NetworkInterface>() {
        @Override
        public int compare(NetworkInterface nis1, NetworkInterface nis2) {
          return Integer.compare(nis1.getIndex(), nis2.getIndex());
        }
      });
      List<InetAddress> addresses = new ArrayList<>();
      InetAddress local = null;

      try {
        for (NetworkInterface ni : nis) {
          //已经开启并运行并且不是回调的网络接口添加到地址列表中
          if (ni.isUp() && !ni.isLoopback()) {
            addresses.addAll(Collections.list(ni.getInetAddresses()));
          }
        }
        local = findValidateIp(addresses);
      } catch (Exception e) {
        // ignore
      }
      if (local != null) {
        this.local = local;
        return;
      }
    } catch (SocketException e) {
      // ignore it
    }
    // 返回仅本地的环回地址
    local = InetAddress.getLoopbackAddress();
  }
}
