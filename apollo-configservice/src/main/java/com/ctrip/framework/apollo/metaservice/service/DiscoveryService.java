package com.ctrip.framework.apollo.metaservice.service;

import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import java.util.List;

/**
 * 服务发现
 */
public interface DiscoveryService {

  /**
   * 获取服务实例列表
   *
   * @param serviceId 服务id
   * @return 指定服务id的服务实例列表，如果没有可用的服务实例，则为空列表
   */
  List<ServiceDTO> getServiceInstances(String serviceId);
}
