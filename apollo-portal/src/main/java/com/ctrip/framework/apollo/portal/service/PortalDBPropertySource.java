package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.config.RefreshablePropertySource;
import com.ctrip.framework.apollo.portal.entity.po.ServerConfig;
import com.ctrip.framework.apollo.portal.repository.ServerConfigRepository;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 界面（门户）数据库属性源
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Slf4j
@Component
public class PortalDBPropertySource extends RefreshablePropertySource {


  @Autowired
  private ServerConfigRepository serverConfigRepository;

  /**
   * 构造界面（门户）数据库属性源对象
   *
   * @param name   名称
   * @param source 数据源
   */
  public PortalDBPropertySource(String name, Map<String, Object> source) {
    super(name, source);
  }

  /**
   * 构造界面（门户）数据库属性源对象
   */
  public PortalDBPropertySource() {
    super("DBConfig", Maps.newConcurrentMap());
  }

  @Override
  protected void refresh() {
    // 所有的配置
    Iterable<ServerConfig> dbConfigs = serverConfigRepository.findAll();

    // 将服务配置放入source当中
    for (ServerConfig config : dbConfigs) {
      String key = config.getKey();
      Object value = config.getValue();

      if (this.source.isEmpty()) {
        log.info("Load config from DB : {} = {}", key, value);
      } else if (!Objects.equals(this.source.get(key), value)) {
        log.info("Load config from DB : {} = {}. Old value = {}", key,
            value, this.source.get(key));
      }
      this.source.put(key, value);
    }
  }
}