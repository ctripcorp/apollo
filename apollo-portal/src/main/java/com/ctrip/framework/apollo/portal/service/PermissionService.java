package com.ctrip.framework.apollo.portal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.portal.repository.PermissionRepository;

@Service
public class PermissionService {

	@Autowired
	private PermissionRepository permissionRepository;
	
	public List<String> findIdsByTargetId(String appId, String namespaceName){
		return permissionRepository.findListByTargetId(appId, namespaceName);
	}
	
	@Transactional
	public int batchDelete(List<String> ids, String operator) {
	    return permissionRepository.batchDelete(ids,operator);
	}
}
