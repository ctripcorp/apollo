package com.ctrip.framework.apollo.metaservice.service;

import com.ctrip.framework.apollo.biz.config.BizConfig;
import com.ctrip.framework.apollo.core.ServiceNameConsts;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 这是一个简单的实现，它跳过任何服务发现，只返回配置的内容
 *
 * <ul>
 *   <li>getServiceInstances("apollo-configservice") returns ${apollo.config-service.url}</li>
 *   <li>getServiceInstances("apollo-adminservice") returns ${apollo.admin-service.url}</li>
 * </ul>
 *
 * @author smilesnake
 */
@Service
@Profile({"kubernetes"})
public class KubernetesDiscoveryServiceImpl implements DiscoveryService {

  /**
   * 逗号分割器
   */
  private static final Splitter COMMA_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
  /**
   * 服务id转配置名称Map<服务名称,配置名称></>
   */
  private static final Map<String, String> SERVICE_ID_TO_CONFIG_NAME = ImmutableMap
      .of(ServiceNameConsts.APOLLO_CONFIG_SERVICE, "apollo.config-service.url",
          ServiceNameConsts.APOLLO_ADMIN_SERVICE, "apollo.admin-service.url");
  /**
   * 业务配置.
   */
  private final BizConfig bizConfig;

  public KubernetesDiscoveryServiceImpl(final BizConfig bizConfig) {
    this.bizConfig = bizConfig;
  }

  @Override
  public List<ServiceDTO> getServiceInstances(String serviceId) {
    //配置名称
    String configName = SERVICE_ID_TO_CONFIG_NAME.get(serviceId);
    if (configName == null) {
      return Collections.emptyList();
    }
    // 返回组装好的服务配置信息
    return assembleServiceDTO(serviceId, bizConfig.getValue(configName));
  }

  /**
   * 组装服务信息
   *
   * @param serviceId 服务名称
   * @param directUrl url目录，逗号分割
   * @return 服务信息列表
   */
  private List<ServiceDTO> assembleServiceDTO(String serviceId, String directUrl) {
    if (StringUtils.isBlank(directUrl)) {
      return Collections.emptyList();
    }
    List<ServiceDTO> serviceDTOList = Lists.newLinkedList();
    COMMA_SPLITTER.split(directUrl).forEach(url -> {
      ServiceDTO service = new ServiceDTO();
      service.setAppName(serviceId);
      service.setInstanceId(String.format("%s:%s", serviceId, url));
      service.setHomepageUrl(url);
      serviceDTOList.add(service);
    });

    return serviceDTOList;
  }
}
