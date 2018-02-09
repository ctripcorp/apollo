package com.ctrip.framework.apollo.demo.spring.common.refresh;

import com.ctrip.framework.apollo.demo.spring.common.bean.AnnotatedBean;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * To refresh the config bean when config is changed
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class ApolloRefreshConfig {
  private static final Logger logger = LoggerFactory.getLogger(ApolloRefreshConfig.class);

  @Autowired
  private RefreshScope refreshScope;

  @Autowired
  private AnnotatedBean annotatedBean;

  @ApolloConfigChangeListener({"application", "FX.apollo"})
  private void onChange(ConfigChangeEvent changeEvent) {
    if (changeEvent.isChanged("timeout") || changeEvent.isChanged("batch")
        || changeEvent.isChanged("objectList")) {
      logger.info("before refresh {}", annotatedBean.toString());
      //could also call refreshScope.refreshAll();
      refreshScope.refresh("annotatedBean");
      logger.info("after refresh {}", annotatedBean.toString());
    }
  }
}
