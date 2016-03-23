package com.ctrip.apollo.client.loader;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.ctrip.apollo.client.env.ClientEnvironment;
import com.ctrip.apollo.core.serivce.ApolloService;

public class ConfigServiceLocater {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServiceLocater.class);

  private RestTemplate restTemplate = new RestTemplate();

  private List<ApolloService> serviceCaches = new ArrayList<>();

  public List<ApolloService> getConfigServices() {
    ClientEnvironment env = ClientEnvironment.getInstance();
    String domainName = env.getMetaServerDomainName();
    String url = domainName + "/config";
    try {
      ApolloService[] services = restTemplate.getForObject(new URI(url), ApolloService[].class);
      if (services != null && services.length > 0) {
        serviceCaches.clear();
        for (ApolloService service : services) {
          serviceCaches.add(service);
        }
      }
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }
    return serviceCaches;
  }
}
