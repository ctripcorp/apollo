package com.ctrip.apollo.portal.service;

import com.ctrip.apollo.core.dto.Config4PortalDTO;
import com.ctrip.apollo.portal.RestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConfigService {


    public Config4PortalDTO loadConfig(long appId, String env, long versionId) {
        return RestUtils.exchangeInGET(
            "http://localhost:8080/configs/" + appId + "/" + versionId, Config4PortalDTO.class);

    }

}
