package com.ctrip.apollo.portal;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class RestUtils {
    private static RestTemplate restTemplate = new RestTemplate();

    public static <T> T exchangeInGET(String url, Class<T> responseType) {
        ResponseEntity<T> response =
            restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Void>((Void) null), responseType);
        return response.getBody();
    }
}
