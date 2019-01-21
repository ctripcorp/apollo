package com.ctrip.framework.apollo.common.datasource;

import com.ctrip.framework.apollo.tracer.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Component
@Conditional(TitanCondition.class)
public class TitanEntityManager {

  private final TitanSettings settings;

  public TitanEntityManager(final TitanSettings settings) {
    this.settings = settings;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public DataSource datasource() throws Exception {
    Class clazz = Class.forName("com.ctrip.datasource.configure.DalDataSourceFactory");
    Object obj = clazz.newInstance();
    Method method = clazz.getMethod("createDataSource", new Class[] {String.class, String.class});
    DataSource ds = ((DataSource) method.invoke(obj,
        new Object[] {settings.getTitanDbname(), settings.getTitanUrl()}));
    Tracer.logEvent("Apollo.Datasource.Titan", settings.getTitanDbname());
    return ds;
  }

}
