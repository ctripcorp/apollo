package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.enricher.adapter.UserInfoEnrichedAdapter;
import java.util.List;
import java.util.function.Function;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public interface AdditionalUserInfoEnrichService {

  /**
   * enrich the additional user info for the object list
   *
   * @param list   object with user id
   * @param mapper map the object in the list to {@link UserInfoEnrichedAdapter}
   */
  <T> void enrichAdditionalUserInfo(List<? extends T> list,
      Function<? super T, ? extends UserInfoEnrichedAdapter> mapper);
}
