package com.ctrip.framework.apollo.common.utils;

import com.ctrip.framework.apollo.common.dto.BaseDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author vdisk <vdisk@foxmail.com>
 */
public class PreferredUsernameUtil {

  /**
   * enrich the preferred username for the dto list
   *
   * @param dtoList    dto with operator id
   * @param repository preferred username repository (operatorIdList -> preferredUsernameMap)
   */
  public static void enrichPreferredUserName(List<? extends BaseDTO> dtoList,
      Function<List<String>, Map<String, String>> repository) {
    if (CollectionUtils.isEmpty(dtoList)) {
      return;
    }
    Set<String> operatorIdSet = PreferredUsernameUtil.extractOperatorId(dtoList);
    if (CollectionUtils.isEmpty(operatorIdSet)) {
      return;
    }
    // userId - preferredUsername
    Map<String, String> preferredUsernameMap = repository.apply(new ArrayList<>(operatorIdSet));
    if (CollectionUtils.isEmpty(preferredUsernameMap)) {
      return;
    }
    for (BaseDTO dto : dtoList) {
      PreferredUsernameUtil.setPreferredUsername(dto, preferredUsernameMap);
    }
  }

  /**
   * extract operator id from the dto list
   *
   * @param dtoList dto list with operator id
   * @return operator id set
   */
  public static Set<String> extractOperatorId(List<? extends BaseDTO> dtoList) {
    if (CollectionUtils.isEmpty(dtoList)) {
      return Collections.emptySet();
    }
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

  /**
   * extract operator id from the dto
   *
   * @param dto dto with operator id
   * @return operator id set
   */
  public static Set<String> extractOperatorId(BaseDTO dto) {
    Set<String> operatorIdSet = new HashSet<>();
    if (StringUtils.hasText(dto.getDataChangeCreatedBy())) {
      operatorIdSet.add(dto.getDataChangeCreatedBy());
    }
    if (StringUtils.hasText(dto.getDataChangeLastModifiedBy())) {
      operatorIdSet.add(dto.getDataChangeLastModifiedBy());
    }
    return operatorIdSet;
  }

  /**
   * set the preferred username
   *
   * @param dto                  dto with operator id
   * @param preferredUsernameMap (userId - preferredUsername) prepared preferred username map
   */
  public static void setPreferredUsername(BaseDTO dto,
      Map<String, String> preferredUsernameMap) {
    if (dto == null) {
      return;
    }
    if (CollectionUtils.isEmpty(preferredUsernameMap)) {
      return;
    }
    if (StringUtils.hasText(dto.getDataChangeCreatedBy())) {
      String preferredUsername = preferredUsernameMap.get(dto.getDataChangeCreatedBy());
      if (StringUtils.hasText(preferredUsername)) {
        dto.setDataChangeCreatedByPreferredUsername(preferredUsername);
      }
    }
    if (StringUtils.hasText(dto.getDataChangeLastModifiedBy())) {
      String preferredUsername = preferredUsernameMap.get(dto.getDataChangeLastModifiedBy());
      if (StringUtils.hasText(preferredUsername)) {
        dto.setDataChangeLastModifiedByPreferredUsername(preferredUsername);
      }
    }
  }
}
