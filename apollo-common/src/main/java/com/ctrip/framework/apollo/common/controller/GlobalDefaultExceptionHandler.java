package com.ctrip.framework.apollo.common.controller;

import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.WARN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.ctrip.framework.apollo.common.exception.AbstractApolloHttpException;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 全局默认异常处理器
 */
@Slf4j
@ControllerAdvice
public class GlobalDefaultExceptionHandler {

  private Gson gson = new Gson();
  private static Type mapType = new TypeToken<Map<String, Object>>() {
  }.getType();

  /**
   * 处理系统内置的Exception
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<Map<String, Object>> exception(HttpServletRequest request, Throwable ex) {
    return handleError(request, INTERNAL_SERVER_ERROR, ex);
  }

  /**
   * 处理http请求方法不支持异常，即错误的请求
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class})
  public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request,
      ServletException ex) {
    return handleError(request, BAD_REQUEST, ex, WARN);
  }

  /**
   * 处理调用http状态码异常
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */
  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<Map<String, Object>> restTemplateException(HttpServletRequest request,
      HttpStatusCodeException ex) {
    return handleError(request, ex.getStatusCode(), ex);
  }

  /**
   * 处理拒绝访问异常
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> accessDeny(HttpServletRequest request,
      AccessDeniedException ex) {
    return handleError(request, FORBIDDEN, ex);
  }

  //处理自定义Exception

  /**
   * 处理http请求方法不支持异常，即错误的请求
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */

  @ExceptionHandler({AbstractApolloHttpException.class})
  public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request,
      AbstractApolloHttpException ex) {
    return handleError(request, ex.getHttpStatus(), ex);
  }

  /**
   * 处理方法参数未通过验证异常
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException ex) {
    final Optional<ObjectError> firstError = ex.getBindingResult().getAllErrors().stream()
        .findFirst();
    if (firstError.isPresent()) {
      final String firstErrorMessage = firstError.get().getDefaultMessage();
      return handleError(request, BAD_REQUEST, new BadRequestException(firstErrorMessage));
    }
    return handleError(request, BAD_REQUEST, ex);
  }

  /**
   * 处理处理约束冲突异常，即参数验证时注解冲突
   *
   * @param request 请求对象
   * @param ex      异常实例
   * @return 错误响应实例
   */

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
      HttpServletRequest request, ConstraintViolationException ex) {
    return handleError(request, BAD_REQUEST, new BadRequestException(ex.getMessage()));
  }

  /**
   * 错误处理
   *
   * @param request 请求实例
   * @param status  状态码对象
   * @param ex      异常
   * @return 错误响应实例
   */
  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
      HttpStatus status, Throwable ex) {
    return handleError(request, status, ex, ERROR);
  }

  /**
   * 错误处理
   *
   * @param request  请求实例
   * @param status   状态码对象
   * @param ex       异常
   * @param logLevel 日志等级
   * @return 错误响应实例
   */
  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
      HttpStatus status, Throwable ex, Level logLevel) {
    // 异常的message
    String message = ex.getMessage();
    // 打印日志
    printLog(message, ex, logLevel);

    // 错误属性
    Map<String, Object> errorAttributes = new HashMap<>();
    // 是否已处理错误处理
    boolean errorHandled = false;

    // http状态码异常
    if (ex instanceof HttpStatusCodeException) {
      try {
        // 如果原始错误信息是从apollo程序（例如admin service）抛出的，请尝试提取它
        // 错误属性
        errorAttributes = gson.fromJson(((HttpStatusCodeException) ex).getResponseBodyAsString(),
            mapType);
        // 状态
        status = ((HttpStatusCodeException) ex).getStatusCode();
        // 标记状态
        errorHandled = true;
      } catch (Throwable th) {
        //ignore
      }
    }
    // 错误未处理，构建错误属性
    if (!errorHandled) {
      errorAttributes.put("status", status.value());
      errorAttributes.put("message", message);
      errorAttributes.put("timestamp",
          LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      errorAttributes.put("exception", ex.getClass().getName());

    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    return new ResponseEntity<>(errorAttributes, headers, status);
  }

  //打印日志, 其中logLevel为日志级别: ERROR/WARN/DEBUG/INFO/TRACE

  /**
   * 打印日志
   *
   * @param message  异常message
   * @param ex       异常
   * @param logLevel 日志等级
   */
  private void printLog(String message, Throwable ex, Level logLevel) {
    switch (logLevel) {
      case ERROR:
        log.error(message, ex);
        break;
      case WARN:
        log.warn(message, ex);
        break;
      case DEBUG:
        log.debug(message, ex);
        break;
      case INFO:
        log.info(message, ex);
        break;
      case TRACE:
        log.trace(message, ex);
        break;
    }

    Tracer.logError(ex);
  }

}
