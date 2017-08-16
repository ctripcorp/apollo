package com.ctrip.framework.apollo.common.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author lepdou 2017-03-10
 */
public class ServerConfigRefreshedEvent extends ApplicationEvent {

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public ServerConfigRefreshedEvent(Object source) {
    super(source);
  }

}
