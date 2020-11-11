package com.ctrip.framework.apollo.configservice.wrapper;

import com.ctrip.framework.apollo.core.dto.ApolloConfigNotification;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * DeferredResult 包装器，封装 DeferredResult 的公用方法
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class DeferredResultWrapper implements Comparable<DeferredResultWrapper> {

  /**
   * 未修改时的 ResponseEntity 响应，使用 302 状态码。
   */
  private static final ResponseEntity<List<ApolloConfigNotification>>
      NOT_MODIFIED_RESPONSE_LIST = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
  /**
   * 标准化和原始的 名称空间名称Map
   */
  private Map<String, String> normalizedNamespaceNameToOriginalNamespaceName;
  /**
   * 响应的 DeferredResult 对象
   */
  private DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> result;

  /**
   * 构造DeferredResultWrapper
   *
   * @param timeoutInMilli 超时时间(毫秒)
   */
  public DeferredResultWrapper(long timeoutInMilli) {
    result = new DeferredResult<>(timeoutInMilli, NOT_MODIFIED_RESPONSE_LIST);
  }

  /**
   * 记录归一化和原始的 Namespace 的名字的映射
   *
   * @param originalNamespaceName   原名称空间名称
   * @param normalizedNamespaceName 标准化的名称空间名称
   */
  public void recordNamespaceNameNormalizedResult(String originalNamespaceName,
      String normalizedNamespaceName) {
    if (normalizedNamespaceNameToOriginalNamespaceName == null) {
      normalizedNamespaceNameToOriginalNamespaceName = Maps.newHashMap();
    }
    // 添加到 `normalizedNamespaceNameToOriginalNamespaceName` 中，和参数的顺序，相反
    normalizedNamespaceNameToOriginalNamespaceName.put(normalizedNamespaceName,
        originalNamespaceName);
  }

  /**
   * 设置超时时间
   *
   * @param timeoutCallback 超时时间函数
   */
  public void onTimeout(Runnable timeoutCallback) {
    result.onTimeout(timeoutCallback);
  }

  /**
   * 响应的结果
   *
   * @param completionCallback 响应函数接口
   */
  public void onCompletion(Runnable completionCallback) {
    result.onCompletion(completionCallback);
  }

  /**
   * 设置结果（响应的 DeferredResult 对象）
   *
   * @param notification 配置通知
   */
  public void setResult(ApolloConfigNotification notification) {
    setResult(Lists.newArrayList(notification));
  }

  /**
   * 名称空间名称在客户端作Key，因此我们必须返回原始名称，而不是正确的名称
   */
  public void setResult(List<ApolloConfigNotification> notifications) {
    // 恢复被标准化的名称空间名称为原始的名称空间名称
    if (normalizedNamespaceNameToOriginalNamespaceName != null) {
      notifications.stream().filter(notification -> normalizedNamespaceNameToOriginalNamespaceName
          .containsKey(notification.getNamespaceName())).forEach(notification -> notification
          .setNamespaceName(normalizedNamespaceNameToOriginalNamespaceName.get(notification
              .getNamespaceName())));
    }
    // 设置结果，并使用 200 状态码
    result.setResult(new ResponseEntity<>(notifications, HttpStatus.OK));
  }

  /**
   * 获取结果（响应的 DeferredResult 对象）
   *
   * @return 结果（响应的 DeferredResult 对象）
   */
  public DeferredResult<ResponseEntity<List<ApolloConfigNotification>>> getResult() {
    return result;
  }

  @Override
  public int compareTo(@NonNull DeferredResultWrapper deferredResultWrapper) {
    return Integer.compare(this.hashCode(), deferredResultWrapper.hashCode());
  }
}
