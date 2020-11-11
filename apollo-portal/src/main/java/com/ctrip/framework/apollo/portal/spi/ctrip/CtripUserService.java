package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 携程用户服务
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class CtripUserService implements UserService {

  private RestTemplate restTemplate;
  /**
   * 搜索用户信息匹配的字段字符串
   */
  private List<String> searchUserMatchFields;
  /**
   * 响应类型
   */
  private ParameterizedTypeReference<Map<String, List<UserServiceResponse>>> responseType;
  /**
   * 界面（门户）配置
   */
  private PortalConfig portalConfig;

  public CtripUserService(PortalConfig portalConfig) {
    this.portalConfig = portalConfig;
    this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    this.searchUserMatchFields =
        Lists.newArrayList("empcode", "empaccount", "displayname", "c_name", "pinyin");
    this.responseType = new ParameterizedTypeReference<Map<String, List<UserServiceResponse>>>() {
    };
  }

  private ClientHttpRequestFactory clientHttpRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(portalConfig.connectTimeout());
    factory.setReadTimeout(portalConfig.readTimeout());

    return factory;
  }

  @Override
  public List<UserInfo> searchUsers(String keyword, int offset, int limit) {
    // 组装请求
    UserServiceRequest request = assembleSearchUserRequest(keyword, offset, limit);

    //服务调用
    HttpEntity<UserServiceRequest> entity = new HttpEntity<>(request);
    ResponseEntity<Map<String, List<UserServiceResponse>>> response =
        restTemplate.exchange(portalConfig.userServiceUrl(), HttpMethod.POST, entity, responseType);

    if (!response.getBody().containsKey("result")) {
      return Collections.emptyList();
    }

    // 结果转换
    List<UserInfo> result = Lists.newArrayList();
    result.addAll(response.getBody().get("result").stream().
        map(this::transformUserServiceResponseToUserInfo).collect(Collectors.toList()));

    return result;
  }

  @Override
  public UserInfo findByUserId(String userId) {
    List<UserInfo> userInfoList = this.findByUserIds(Lists.newArrayList(userId));
    if (CollectionUtils.isEmpty(userInfoList)) {
      return null;
    }
    return userInfoList.get(0);
  }

  @Override
  public List<UserInfo> findByUserIds(List<String> userIds) {
    // 组装请求
    UserServiceRequest request = assembleFindUserRequest(Lists.newArrayList(userIds));
    // 服务调用
    HttpEntity<UserServiceRequest> entity = new HttpEntity<>(request);
    ResponseEntity<Map<String, List<UserServiceResponse>>> response =
        restTemplate.exchange(portalConfig.userServiceUrl(), HttpMethod.POST, entity, responseType);

    if (!response.getBody().containsKey("result")) {
      return Collections.emptyList();
    }
    // 结果转换
    List<UserInfo> result = Lists.newArrayList();
    result.addAll(response.getBody().get("result").stream()
        .map(this::transformUserServiceResponseToUserInfo).collect(Collectors.toList()));
    return result;
  }

  /**
   * 将服务响应信息转换为用户信息
   *
   * @param userServiceResponse 服务响应信息
   * @return 用户信息
   */
  private UserInfo transformUserServiceResponseToUserInfo(UserServiceResponse userServiceResponse) {
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(userServiceResponse.getEmpaccount());
    userInfo.setName(userServiceResponse.getDisplayname());
    userInfo.setEmail(userServiceResponse.getEmail());
    return userInfo;
  }

  /**
   * 组装搜索用户请求
   *
   * @param keyword 密码
   * @param offset  偏移量
   * @param limit   限制量
   * @return 用户服务请求实体
   */
  UserServiceRequest assembleSearchUserRequest(String keyword, int offset, int limit) {
    Map<String, Object> query = Maps.newHashMap();
    Map<String, Object> multiMatchMap = Maps.newHashMap();
    multiMatchMap.put("fields", searchUserMatchFields);
    multiMatchMap.put("operator", "and");
    multiMatchMap.put("query", keyword);
    multiMatchMap.put("type", "best_fields");
    query.put("multi_match", multiMatchMap);

    return assembleUserServiceRequest(query, offset, limit);
  }

  /**
   * 组装查询用户请求
   *
   * @param userIds 用户id列表
   * @return 用户服务请求实体
   */
  UserServiceRequest assembleFindUserRequest(List<String> userIds) {
    Map<String, Object> query = ImmutableMap.of("filtered", ImmutableMap
        .of("filter", ImmutableMap.of("terms", ImmutableMap.of("empaccount", userIds))));
    return assembleUserServiceRequest(query, 0, userIds.size());
  }

  /**
   * 组装用户服务请求
   *
   * @param query  组装的请求数据
   * @param offset 偏移量
   * @param limit  限制量
   * @return 组装好的用户服务请求
   */
  private UserServiceRequest assembleUserServiceRequest(Map<String, Object> query, int offset,
      int limit) {
    UserServiceRequest request = new UserServiceRequest();
    request.setAccessToken(portalConfig.userServiceAccessToken());

    UserServiceRequestBody requestBody = new UserServiceRequestBody();
    requestBody.setIndexAlias("itdb_emloyee");
    requestBody.setType("emloyee");
    request.setRequestBody(requestBody);

    Map<String, Object> queryJson = Maps.newHashMap();
    requestBody.setQueryJson(queryJson);

    queryJson.put("query", query);

    queryJson.put("from", offset);
    queryJson.put("size", limit);

    return request;
  }

  /**
   * 用户服务请求实体
   */
  @Data
  static class UserServiceRequest {

    /**
     * 服务访问token
     */
    private String accessToken;
    /**
     * 用户服务请求Body实体
     */
    private UserServiceRequestBody requestBody;
  }

  /**
   * 用户服务请求Body实体
   */
  @Data
  static class UserServiceRequestBody {

    /**
     * 下标别名
     */
    private String indexAlias;
    /**
     * 类型
     */
    private String type;
    /**
     * 查询的json
     */
    private Map<String, Object> queryJson;
  }

  /**
   * 用户服务响应类
   */
  @Data
  static class UserServiceResponse {

    /**
     * 雇员帐户(用户id)
     */
    private String empaccount;
    /**
     * 显示名称
     */
    private String displayname;
    /**
     * 用户邮箱
     */
    private String email;
  }
}
