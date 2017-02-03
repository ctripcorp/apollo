package com.ctrip.framework.apollo.common.constants;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ReleaseOperationContext {
  String SOURCE_BRANCH = "sourceBranch";
  String RULES = "rules";
  String OLD_RULES = "oldRules";
  String BASE_RELEASE_ID = "baseReleaseId";
  String IS_EMERGENCY_PUBLISH = "isEmergencyPublish";
}
