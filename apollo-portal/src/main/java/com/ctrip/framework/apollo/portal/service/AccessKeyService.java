package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.AccessKeyDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.AccessKeyAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccessKeyService {

  private final AdminServiceAPI.AccessKeyAPI accessKeyAPI;

  public AccessKeyService(AccessKeyAPI accessKeyAPI) {
    this.accessKeyAPI = accessKeyAPI;
  }

  public List<AccessKeyDTO> findByAppId(Env env, String appId) {
    return accessKeyAPI.findByAppId(env, appId);
  }

  public AccessKeyDTO createAccessKey(Env env, AccessKeyDTO accessKey) {
    AccessKeyDTO accessKeyDTO = accessKeyAPI.create(env, accessKey);
    Tracer.logEvent(TracerEventType.CREATE_ACCESS_KEY, accessKey.getAppId());
    return accessKeyDTO;
  }

  public void deleteAccessKey(Env env, String appId, long id, String operator) {
    accessKeyAPI.delete(env, appId, id, operator);
  }

  public void enable(Env env, String appId, long id, String operator) {
    accessKeyAPI.enable(env, appId, id, operator);
  }

  public void disable(Env env, String appId, long id, String operator) {
    accessKeyAPI.disable(env, appId, id, operator);
  }
}
