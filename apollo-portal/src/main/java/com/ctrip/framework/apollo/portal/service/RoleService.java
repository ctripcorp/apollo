package com.ctrip.framework.apollo.portal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctrip.framework.apollo.portal.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;
	
	public List<String> findIdsByRoleName(String appId, String namespaceName){
		return roleRepository.findIdsByRoleName(appId, namespaceName);
	}
	
	@Transactional
	public int batchDeleteRole(List<String> ids, String operator) {
	    return roleRepository.batchDeleteRole(ids,operator);
	}
	
	@Transactional
	public int batchDeleteUserRole(List<String> ids, String operator) {
	    return roleRepository.batchDeleteUserRole(ids,operator);
	}

	@Transactional
	public int batchDeleteConsumerRole(List<String> ids, String operator) {
	    return roleRepository.batchDeleteConsumerRole(ids,operator);
	}
}
