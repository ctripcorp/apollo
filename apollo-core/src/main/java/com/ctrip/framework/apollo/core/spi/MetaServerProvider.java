package com.ctrip.framework.apollo.core.spi;

import com.ctrip.framework.apollo.core.enums.Env;

/**
 * 元服务器提供者.
 *
 * @author Jason Song(song_s@ctrip.com)
 * @since 1.0.0
 */
public interface MetaServerProvider extends Ordered {

  /**
   * 获取指定的环境的Apollo元服务器地址，可以是url域或逗号分隔的ip地址，如http://1.2.3.4:8080、http://2.3.4.5:8080。
   * <p>在生产环境中，我们建议使用一个单一的域，例如http://config.xxx.com（由nginx等软件负载平衡器支持）而不是多个ip地址
   *
   * @param targetEnv 指定的环境
   * @return 与指定的环境匹配的Apollo元服务器地址
   */
  String getMetaServerAddress(Env targetEnv);
}
