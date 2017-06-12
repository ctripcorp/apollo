package com.ctrip.framework.apollo.biz.entity;

import com.ctrip.framework.apollo.biz.utils.ReleaseMessageKeyGenerator;
import com.ctrip.framework.apollo.common.entity.BaseEntity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "Namespace")
@SQLDelete(sql = "Update Namespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class Namespace extends BaseEntity {

  @Column(name = "appId", nullable = false)
  private String appId;

  @Column(name = "ClusterName", nullable = false)
  private String clusterName;

  @Column(name = "NamespaceName", nullable = false)
  private String namespaceName;

  public Namespace(){

  }

  public Namespace(String appId, String clusterName, String namespaceName) {
    this.appId = appId;
    this.clusterName = clusterName;
    this.namespaceName = namespaceName;
  }

  public String getAppId() {
    return appId;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Namespace namespace = (Namespace) o;

    if (namespace.getId() == getId()) {
      return true;
    }
    if (appId != null ? !appId.equals(namespace.appId) : namespace.appId != null) {
      return false;
    }
    if (clusterName != null ? !clusterName.equals(namespace.clusterName) : namespace.clusterName != null) {
      return false;
    }
    return namespaceName != null ? namespaceName.equals(namespace.namespaceName) : namespace.namespaceName == null;
  }

  @Override
  public int hashCode() {
    int result = appId != null ? appId.hashCode() : 0;
    result = 31 * result + (clusterName != null ? clusterName.hashCode() : 0);
    result = 31 * result + (namespaceName != null ? namespaceName.hashCode() : 0);
    return result;
  }

  public String toString() {
    return toStringHelper().add("appId", appId).add("clusterName", clusterName)
        .add("namespaceName", namespaceName).toString();
  }
}
