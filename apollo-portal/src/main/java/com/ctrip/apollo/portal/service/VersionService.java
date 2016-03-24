package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.core.dto.VersionDTO;
import com.ctrip.apollo.portal.RestUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionService {

    public List<VersionDTO> findVersionsByApp(long appId, String env){
        return RestUtils.exchangeInGET("http://localhost:8080/version/" + appId, List.class);
    }
}
