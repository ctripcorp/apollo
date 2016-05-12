package com.ctrip.apollo.common.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 增强ResponseEntity,使得ResponseEntity可以附加message信息
 * @param <T>
 */
public class RichResponseEntity<T>{

  private Object message;
  /**
   * 被封装的ResponseEntity
   */
  private ResponseEntity<T> entity;

  public static <T> RichResponseEntity<T> ok(T body){
    RichResponseEntity<T> richResponseEntity = new RichResponseEntity<>();
    richResponseEntity.setMessage(HttpStatus.OK.getReasonPhrase());
    richResponseEntity.setEntity(ResponseEntity.ok(body));
    return richResponseEntity;
  }

  public static <T> RichResponseEntity<T> error(HttpStatus httpCode, Object message){
    RichResponseEntity<T> richResponseEntity = new RichResponseEntity<>();
    richResponseEntity.setMessage(message);

    ResponseEntity entity = ResponseEntity.status(httpCode).build();
    richResponseEntity.setEntity(entity);

    return richResponseEntity;
  }

  public Object getMessage() {
    return message;
  }

  public void setMessage(Object message) {
    this.message = message;
  }


  public void setEntity(ResponseEntity<T> entity) {
    this.entity = entity;
  }

  public HttpStatus statusCode(){
    return entity.getStatusCode();
  }

  public int getCode() {
    return entity.getStatusCode().value();
  }

  public T getBody() {
    return entity.getBody();
  }
}
