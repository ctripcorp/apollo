package com.ctrip.framework.foundation.internals;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public enum NetworkInterfaceManager {
  INSTANCE;

  private InetAddress m_local;

  private InetAddress m_localHost;

  private NetworkInterfaceManager() {
    load();
  }

  public InetAddress findValidateIp(List<InetAddress> addresses) {
    InetAddress local = null;
    int maxWeight = -1;
    for (InetAddress address : addresses) {
      if (address instanceof Inet4Address) {
        int weight = 0;

        if (address.isSiteLocalAddress()) {
          weight += 8;
        }

        if (address.isLinkLocalAddress()) {
          weight += 4;
        }

        if (address.isLoopbackAddress()) {
          weight += 2;
        }

        // has host name
        // TODO fix performance issue when calling getHostName
        if (!Objects.equals(address.getHostName(), address.getHostAddress())) {
          weight += 1;
        }

        if (weight > maxWeight) {
          maxWeight = weight;
          local = address;
        }
      }
    }
    return local;
  }

  public String getLocalHostAddress() {
    InetAddress hostAddress = this.findFirstNonLoopbackAddress();
    return hostAddress != null ? hostAddress.getHostAddress() : m_local.getHostAddress();
  }

  public String getLocalHostName() {
    try {
      if (null == m_localHost) {
        m_localHost = InetAddress.getLocalHost();
      }
      return m_localHost.getHostName();
    } catch (UnknownHostException e) {
      return m_local.getHostName();
    }
  }

  private String getProperty(String name) {
    String value = null;

    value = System.getProperty(name);

    if (value == null) {
      value = System.getenv(name);
    }

    return value;
  }

  private void load() {
    String ip = getProperty("host.ip");

    if (ip != null) {
      try {
        m_local = InetAddress.getByName(ip);
        return;
      } catch (Exception e) {
        System.err.println(e);
        // ignore
      }
    }

    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      List<NetworkInterface> nis = interfaces == null ? Collections.<NetworkInterface>emptyList()
          : Collections.list(interfaces);
      List<InetAddress> addresses = new ArrayList<InetAddress>();
      InetAddress local = null;

      try {
        for (NetworkInterface ni : nis) {
          if (ni.isUp() && !ni.isLoopback()) {
            addresses.addAll(Collections.list(ni.getInetAddresses()));
          }
        }
        local = findValidateIp(addresses);
      } catch (Exception e) {
        // ignore
      }
      if (local != null) {
        m_local = local;
        return;
      }
    } catch (SocketException e) {
      // ignore it
    }

    m_local = InetAddress.getLoopbackAddress();
  }

  /**
   * ref:org.springframework.cloud.commons.util.InetUtils
   *
   * @return InetAddress
   */
  public InetAddress findFirstNonLoopbackAddress() {
    InetAddress result = null;
    try {
      int lowest = Integer.MAX_VALUE;
      for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
          nics.hasMoreElements(); ) {
        NetworkInterface ifc = nics.nextElement();
        if (ifc.isUp()) {
          // testing interface:  + ifc.getDisplayName());
          if (ifc.getIndex() < lowest || result == null) {
            lowest = ifc.getIndex();
          } else {
            continue;
          }

          for (Enumeration<InetAddress> addrs = ifc
              .getInetAddresses(); addrs.hasMoreElements(); ) {
            InetAddress address = addrs.nextElement();
            if (address instanceof Inet4Address
                && !address.isLoopbackAddress()) {
              //found non-loopback interface: +ifc.getDisplayName()
              result = address;
            }
          }
        }
      }
    } catch (IOException ex) {
      // ignore
    }

    if (result != null) {
      return result;
    }

    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      // ignore
    }
    return null;
  }
}
