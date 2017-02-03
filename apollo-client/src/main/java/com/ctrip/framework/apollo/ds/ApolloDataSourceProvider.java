package com.ctrip.framework.apollo.ds;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.foundation.Foundation;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.datasource.DataSourceProvider;
import org.unidal.dal.jdbc.datasource.model.entity.DataSourcesDef;
import org.unidal.dal.jdbc.datasource.model.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;

/**
 * Data source provider based on Apollo configuration service.
 * <p>
 *
 * Use following component definition to replace default
 * <code>DataSourceProvider</code>:
 * <p>
 * <code><pre>
 *   public List<Component> defineComponents() {
 *      List<Component> all = new ArrayList<>();
 * 
 *      all.add(A(ApolloDataSourceProvider.class));
 * 
 *      return all;
 *   }
 * </pre></code>
 * 
 * <b>WARNING:</b> all defined <code>DataSourceProvider</code> components will
 * be taken affect. DO NOT define unused <code>DataSourceProvider</code>
 * component.
 */
@Named(type = DataSourceProvider.class, value = "apollo")
public class ApolloDataSourceProvider implements DataSourceProvider, LogEnabled {
   private Logger m_logger;

   private DataSourcesDef m_def;

   @Override
   public DataSourcesDef defineDatasources() {
      if (m_def == null) {
         ConfigFile file = ConfigService.getConfigFile("datasources", ConfigFileFormat.XML);
         String appId = Foundation.app().getAppId();
         String envType = Foundation.server().getEnvType();

         if (file != null && file.hasContent()) {
            String content = file.getContent();

            m_logger.info(String.format("Found datasources.xml from Apollo(env=%s, app.id=%s)!", envType, appId));

            try {
               m_def = DefaultSaxParser.parse(content);
            } catch (Exception e) {
               throw new IllegalStateException(String.format("Error when parsing datasources.xml from Apollo(env=%s, app.id=%s)!", envType, appId), e);
            }
         } else {
            m_logger.warn(String.format("Can't get datasources.xml from Apollo(env=%s, app.id=%s)!", envType, appId));
            m_def = new DataSourcesDef();
         }
      }

      return m_def;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }
}
