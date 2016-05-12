package com.ctrip.apollo.common.http;

import com.ctrip.apollo.core.exception.ServiceException;

import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * 一个Response中包含多个ResponseEntity,每个ResponseEntity都包含完整的Response信息
 */
public class MultiResponseEntity<T> {

  private HttpStatus httpCode;

  private List<RichResponseEntity<T>> entities = new LinkedList<>();


  private MultiResponseEntity() {

  }

  private MultiResponseEntity(HttpStatus statusCode) {
    this.httpCode = statusCode;
  }

  public static <T> MultiResponseEntity<T> instance() {
    return new MultiResponseEntity<>();
  }

  public static <T> MultiResponseEntity<T> ok() {
    return new MultiResponseEntity<>(HttpStatus.OK);
  }

  public void addResponseEntity(RichResponseEntity<T> responseEntity) {
    if (responseEntity == null){
      throw new IllegalArgumentException("sub response entity can not be null");
    }
    entities.add(responseEntity);
  }

  /**
   * 整个Response的状态码
   * @return HttpStatus.OK responseEntities包含2xx的ResponseEntity
   * @throws ServiceException responseEntities不包含一个2xx的ResponseEntity
   */
  public HttpStatus statusCode() {
    StringBuilder interServiceErrorMsg = new StringBuilder();
    if (httpCode != null) {
      return httpCode;
    } else {
      for (RichResponseEntity responseEntity : entities) {
        HttpStatus status = responseEntity.statusCode();
        if (status.is2xxSuccessful()) {
          return HttpStatus.OK;
        } else if (status.is5xxServerError()) {
          Object msg = responseEntity.getMessage();
          if (msg != null) {
            interServiceErrorMsg.append(msg).append("\n");
          }
        }
      }
    }
    //全部ResponseEntity都异常,则抛异常
    throw new ServiceException(interServiceErrorMsg.toString());
  }

  public int getCode() {
    return statusCode().value();
  }

  public List<RichResponseEntity<T>> getEntities() {
    return entities;
  }

}
