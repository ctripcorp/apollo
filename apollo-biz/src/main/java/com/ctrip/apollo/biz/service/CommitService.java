package com.ctrip.apollo.biz.service;

import com.ctrip.apollo.biz.entity.Commit;
import com.ctrip.apollo.biz.repository.CommitRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommitService {

  @Autowired
  private CommitRepository commitRepository;

  public void save(Commit commit, String user){

    commit.setDataChangeCreatedBy(user);
    commit.setDataChangeCreatedTime(new Date());
    commitRepository.save(commit);
  }

}
