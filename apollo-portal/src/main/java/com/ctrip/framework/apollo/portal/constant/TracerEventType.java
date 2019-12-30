package com.ctrip.framework.apollo.portal.constant;

public interface TracerEventType {

  String RELEASE_NAMESPACE = "Namespace.Release";

  String MODIFY_NAMESPACE_BY_TEXT = "Namespace.Modify.Text";

  String MODIFY_NAMESPACE = "Namespace.Modify";

  String SYNC_NAMESPACE = "Namespace.Sync";

  String CREATE_APP = "App.Create";

  String CREATE_CLUSTER = "Cluster.Create";

  String CREATE_ACCESS_KEY = "AccessKey.Create";

  String CREATE_NAMESPACE = "Namespace.Create";

  String API_RETRY = "API.Retry";

  String USER_ACCESS = "User.Access";

  String CREATE_GRAY_RELEASE = "GrayRelease.Create";

  String DELETE_GRAY_RELEASE = "GrayRelease.Delete";

  String MERGE_GRAY_RELEASE = "GrayRelease.Merge";

  String UPDATE_GRAY_RELEASE_RULE = "GrayReleaseRule.Update";
}
