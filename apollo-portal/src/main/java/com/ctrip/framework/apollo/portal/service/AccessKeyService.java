package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.AccessKeyDTO;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI.AccessKeyAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.tracer.Tracer;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccessKeyService {

  private final AdminServiceAPI.AccessKeyAPI accessKeyAPI;

  public AccessKeyService(AccessKeyAPI accessKeyAPI) {
    this.accessKeyAPI = accessKeyAPI;
  }

  /**
   * 通过appId和环境对象 查询访问密钥列表
   *
   * @param env   环境对象
   * @param appId 应用id
   * @return 访问密钥列表
   */
  public List<AccessKeyDTO> findByAppId(Env env, String appId) {
    return accessKeyAPI.findByAppId(env, appId);
  }

  /**
   * 指定应用下的指定环境添加密钥
   *
   * @param env       环境
   * @param accessKey 密钥对象
   * @return
   */
  public AccessKeyDTO createAccessKey(Env env, AccessKeyDTO accessKey) {
    AccessKeyDTO accessKeyDTO = accessKeyAPI.create(env, accessKey);
    Tracer.logEvent(TracerEventType.CREATE_ACCESS_KEY, accessKey.getAppId());
    return accessKeyDTO;
  }

  /**
   * 删除访问密钥
   *
   * @param env      环境
   * @param appId    应用id
   * @param id       访问密钥主键id
   * @param operator 操作者
   */
  public void deleteAccessKey(Env env, String appId, long id, String operator) {
    accessKeyAPI.delete(env, appId, id, operator);
  }

  /**
   * 开启访问密钥
   *
   * @param env      环境
   * @param appId    应用id
   * @param id       访问密钥id
   * @param operator 操作者
   */
  public void enable(Env env, String appId, long id, String operator) {
    accessKeyAPI.enable(env, appId, id, operator);
  }

  /**
   * 关闭访问密钥
   *
   * @param env      环境
   * @param appId    应用id
   * @param id       访问密钥主键id
   * @param operator 操作者
   */
  public void disable(Env env, String appId, long id, String operator) {
    accessKeyAPI.disable(env, appId, id, operator);
  }
}
