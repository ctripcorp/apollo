package com.ctrip.framework.apollo.core.utils;

import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
public class MachineUtil {

  /**
   * 机器码标识
   */
  private static final int MACHINE_IDENTIFIER = createMachineIdentifier();

  public static int getMachineIdentifier() {
    return MACHINE_IDENTIFIER;
  }

  /**
   * 从mac地址获取机器标识符
   *
   * @see <a href=https://github.com/mongodb/mongo-java-driver/blob/master/bson/src/main/org/bson/types/ObjectId.java>ObjectId.java</a>
   */
  private static int createMachineIdentifier() {
    // build a 2-byte machine piece based on NICs info
    //机器码
    int machinePiece;
    try {
      StringBuilder sb = new StringBuilder();
      // 返回机器所有的网络接口，包含物理ip和虚拟ip
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

      if (e != null) {
        //遍历网络接口
        while (e.hasMoreElements()) {
          // 网络接口信息
          NetworkInterface ni = e.nextElement();
          sb.append(ni.toString());
          //获取机器mac地址
          byte[] mac = ni.getHardwareAddress();
          if (mac != null) {
            ByteBuffer bb = ByteBuffer.wrap(mac);
            try {
              sb.append(bb.getChar());
              sb.append(bb.getChar());
              sb.append(bb.getChar());
            } catch (BufferUnderflowException shortHardwareAddressException) { //NOPMD
              // mac with less than 6 bytes. continue
            }
          }
        }
      }
      // 最后将机器信息字符串的hashcode作为机器码返回
      machinePiece = sb.toString().hashCode();
    } catch (Throwable ex) {
      // exception sometimes happens with IBM JVM, use random
      // 如果异常则使用随机数当作机器码
      machinePiece = (new SecureRandom().nextInt());
      log.warn(
          "Failed to get machine identifier from network interface, using random number instead",
          ex);
    }
    return machinePiece;
  }
}
