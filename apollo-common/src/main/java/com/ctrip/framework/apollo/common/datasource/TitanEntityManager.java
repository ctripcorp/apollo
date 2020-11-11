package com.ctrip.framework.apollo.common.datasource;

import com.ctrip.framework.apollo.tracer.Tracer;
import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Titan数据实体管理器
 */
@Component
@Conditional(TitanCondition.class)
public class TitanEntityManager {

  private final TitanSettings settings;

  public TitanEntityManager(final TitanSettings settings) {
    this.settings = settings;
  }

  /**
   * 获取Titan数据库数据源
   *
   * @return Titan数据库数据源
   * @throws Exception 如果发生错误，抛出
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public DataSource datasource() throws Exception {
    // 获取对象实例
    Class clazz = Class.forName("com.ctrip.datasource.configure.DalDataSourceFactory");
    Object obj = clazz.newInstance();
    // 创建数据源
    Method method = clazz.getMethod("createDataSource", new Class[]{String.class, String.class});
    DataSource ds = ((DataSource) method.invoke(obj,
        new Object[]{settings.getTitanDbname(), settings.getTitanUrl()}));
    Tracer.logEvent("Apollo.Datasource.Titan", settings.getTitanDbname());
    return ds;
  }

}
