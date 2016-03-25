package com.ctrip.apollo.portal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ctrip.apollo.portal.entity.Privilege;
import com.ctrip.apollo.portal.exception.NotFoundException;
import com.ctrip.apollo.portal.repository.PrivilegeRepository;

@Service
public class PrivilegeService {

  enum PrivilType {
    EDIT, REVIEW, RELEASE
  }

  @Autowired
  private PrivilegeRepository privilRepo;

  public Privilege addPrivilege(long appId, String name, PrivilType privilType) {
    Privilege privil = privilRepo.findByAppIdAndNameAndPrivilType(appId, name, privilType.name());
    if (privil == null) {
      privil = new Privilege();
      privil.setAppId(appId);
      privil.setPrivilType(privilType.name());
      privil.setName(name);
      privilRepo.save(privil);
    }
    return privil;
  }

  public boolean hasPrivilege(long appId, String name, PrivilType privilType) {
    Privilege privil = privilRepo.findByAppIdAndNameAndPrivilType(appId, name, privilType.name());
    return (privil != null) ? true : false;
  }

  public List<Privilege> listPrivileges(long appId) {
    return privilRepo.findByAppId(appId);
  }

  public void removePrivilege(long appId, String name, PrivilType privilType) {
    Privilege privil = privilRepo.findByAppIdAndNameAndPrivilType(appId, name, privilType.name());
    if (privil == null) {
      throw new NotFoundException();
    }
    privilRepo.delete(privil);
  }
}
