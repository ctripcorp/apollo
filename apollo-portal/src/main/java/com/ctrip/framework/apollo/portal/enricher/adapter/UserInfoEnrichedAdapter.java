package com.ctrip.framework.apollo.portal.enricher.adapter;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface UserInfoEnrichedAdapter {

  /**
   * get user id from the object
   *
   * @return user id
   */
  String getFirstUserId();

  /**
   * set the user display name for the object
   *
   * @param userDisplayName user display name
   */
  void setFirstUserDisplayName(String userDisplayName);

  /**
   * get operator id from the object
   *
   * @return operator id
   */
  default String getSecondUserId() {
    return null;
  }

  /**
   * set the user display name for the object
   *
   * @param userDisplayName user display name
   */
  default void setSecondUserDisplayName(String userDisplayName) {
  }

  /**
   * get operator id from the object
   *
   * @return operator id
   */
  default String getThirdUserId() {
    return null;
  }

  /**
   * set the user display name for the object
   *
   * @param userDisplayName user display name
   */
  default void setThirdUserDisplayName(String userDisplayName) {
  }
}
