package com.ctrip.framework.apollo.openapi.client.service;

import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 开放API服务 抽象类
 */
abstract class AbstractOpenApiService {

  /**
   * 路径转义器
   */
  private static final Escaper pathEscaper = UrlEscapers.urlPathSegmentEscaper();
  /**
   * 查询参数转义器
   */
  private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();
  /**
   * 基础URl
   */
  private final String baseUrl;
  /**
   * 客户端
   */
  protected final CloseableHttpClient client;
  protected final Gson gson;

  /**
   * 构造AbstractOpenApiService
   *
   * @param client  客户端
   * @param baseUrl 基URl
   * @param gson    json对象
   */
  AbstractOpenApiService(CloseableHttpClient client, String baseUrl, Gson gson) {
    this.client = client;
    this.baseUrl = baseUrl;
    this.gson = gson;
  }

  /**
   * Get请求方式
   *
   * @param path 路径
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  protected CloseableHttpResponse get(String path) throws IOException {
    HttpGet get = new HttpGet(String.format("%s/%s", baseUrl, path));
    return execute(get);
  }

  /**
   * Post请求方式
   *
   * @param path   路径
   * @param entity 实体
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  protected CloseableHttpResponse post(String path, Object entity) throws IOException {
    HttpPost post = new HttpPost(String.format("%s/%s", baseUrl, path));
    return execute(post, entity);
  }

  /**
   * PUT请求方式
   *
   * @param path   路径
   * @param entity 实体
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  protected CloseableHttpResponse put(String path, Object entity) throws IOException {
    HttpPut put = new HttpPut(String.format("%s/%s", baseUrl, path));
    return execute(put, entity);
  }

  /**
   * Delete请求方式
   *
   * @param path 路径
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  protected CloseableHttpResponse delete(String path) throws IOException {
    HttpDelete delete = new HttpDelete(String.format("%s/%s", baseUrl, path));
    return execute(delete);
  }

  /**
   * 路径转义
   *
   * @param path 路径
   * @return 转义后的路径
   */
  protected String escapePath(String path) {
    return pathEscaper.escape(path);
  }

  /**
   * 参数转义
   *
   * @param param 待转义的参数
   * @return 转义后的路径
   */
  protected String escapeParam(String param) {
    return queryParamEscaper.escape(param);
  }

  /**
   * 调用远程api
   *
   * @param requestBase 请求的对象
   * @param entity      实体
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  private CloseableHttpResponse execute(HttpEntityEnclosingRequestBase requestBase, Object entity)
      throws IOException {
    requestBase.setEntity(new StringEntity(gson.toJson(entity), ContentType.APPLICATION_JSON));
    return execute(requestBase);
  }

  /**
   * 调用远程api
   *
   * @param request 请求的对象
   * @return 响应对象
   * @throws IOException 以防出现问题或连接被中止,抛出
   */
  private CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
    CloseableHttpResponse response = client.execute(request);
    checkHttpResponseStatus(response);
    return response;
  }


  /**
   * 检查http响应状态
   *
   * @param response 响应状态
   */
  private void checkHttpResponseStatus(HttpResponse response) {
    // 正常状态
    if (response.getStatusLine().getStatusCode() == 200) {
      return;
    }

    // 其它状态
    StatusLine status = response.getStatusLine();
    String message = "";
    try {
      message = EntityUtils.toString(response.getEntity());
    } catch (IOException e) {
      //ignore
    }

    throw new ApolloOpenApiException(status.getStatusCode(), status.getReasonPhrase(), message);
  }

  /**
   * 检查不能为空
   *
   * @param value 值
   * @param name  名称
   */
  protected void checkNotEmpty(String value, String name) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(value),
        name + " should not be null or empty");
  }
}
