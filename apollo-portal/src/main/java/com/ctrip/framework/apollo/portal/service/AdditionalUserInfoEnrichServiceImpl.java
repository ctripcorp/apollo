package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import com.ctrip.framework.apollo.portal.enricher.AdditionalUserInfoEnricher;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.UserService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
@Service
public class AdditionalUserInfoEnrichServiceImpl implements AdditionalUserInfoEnrichService {

  private final UserService userService;

  private final List<AdditionalUserInfoEnricher> enricherList;

  public AdditionalUserInfoEnrichServiceImpl(
      UserService userService,
      List<AdditionalUserInfoEnricher> enricherList) {
    this.userService = userService;
    this.enricherList = enricherList;
  }

  @Override
  public void enrichAdditionalUserInfo(List<? extends BaseDTO> dtoList) {
    if (CollectionUtils.isEmpty(dtoList)) {
      return;
    }
    if (CollectionUtils.isEmpty(this.enricherList)) {
      return;
    }
    Set<String> operatorIdSet = this.extractOperatorId(dtoList);
    if (CollectionUtils.isEmpty(operatorIdSet)) {
      return;
    }
    List<UserInfo> userInfoList = this.userService.findByUserIds(new ArrayList<>(operatorIdSet));
    if (CollectionUtils.isEmpty(userInfoList)) {
      return;
    }
    Map<String, UserInfo> userInfoMap = userInfoList.stream()
        .collect(Collectors.toMap(UserInfo::getUserId, Function.identity()));
    for (BaseDTO dto : dtoList) {
      if (dto == null) {
        continue;
      }
      for (AdditionalUserInfoEnricher enricher : this.enricherList) {
        enricher.enrichAdditionalUserInfo(dto, userInfoMap);
      }
    }
  }

  private Set<String> extractOperatorId(List<? extends BaseDTO> dtoList) {
    Set<String> operatorIdSet = new HashSet<>();
    for (BaseDTO dto : dtoList) {
      if (StringUtils.hasText(dto.getDataChangeCreatedBy())) {
        operatorIdSet.add(dto.getDataChangeCreatedBy());
      }
      if (StringUtils.hasText(dto.getDataChangeLastModifiedBy())) {
        operatorIdSet.add(dto.getDataChangeLastModifiedBy());
      }
    }
    return operatorIdSet;
  }
}
