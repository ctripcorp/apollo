package com.ctrip.framework.apollo.common.utils;

import com.ctrip.framework.apollo.core.utils.ByteUtil;
import com.ctrip.framework.apollo.core.utils.MachineUtil;
import com.google.common.base.Joiner;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * 唯一key生成器
 */
public class UniqueKeyGenerator {

  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat
      .getInstance("yyyyMMddHHmmss");

  private static final AtomicInteger counter = new AtomicInteger(new SecureRandom().nextInt());
  private static final Joiner KEY_JOINER = Joiner.on("-");

  /**
   * 生成key
   *
   * @param args 参数数组
   * @return 生成后的key
   */
  public static String generate(Object... args) {
    // id字符串16进制
    String hexIdString =
        ByteUtil.toHexString(toByteArray(Objects.hash(args), MachineUtil.getMachineIdentifier(),
            counter.incrementAndGet()));
    // 时间拼接16进制
    return KEY_JOINER.join(TIMESTAMP_FORMAT.format(new Date()), hexIdString);

  }

  /**
   * 转换为字符数组，Concat机器id、counter和key-to-byte数组只检索id和counter的低3个字节和keyHashCode的2个字节
   */
  protected static byte[] toByteArray(int keyHashCode, int machineIdentifier, int counter) {
    byte[] bytes = new byte[8];
    bytes[0] = ByteUtil.int1(keyHashCode);
    bytes[1] = ByteUtil.int0(keyHashCode);
    bytes[2] = ByteUtil.int2(machineIdentifier);
    bytes[3] = ByteUtil.int1(machineIdentifier);
    bytes[4] = ByteUtil.int0(machineIdentifier);
    bytes[5] = ByteUtil.int2(counter);
    bytes[6] = ByteUtil.int1(counter);
    bytes[7] = ByteUtil.int0(counter);
    return bytes;
  }


}
