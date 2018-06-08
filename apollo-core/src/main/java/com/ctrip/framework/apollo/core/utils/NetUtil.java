package com.ctrip.framework.apollo.core.utils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gl49 on 2018/6/8.
 */
public class NetUtil {
    public static int getUrlStatus(String address){
        int statusCode = 0;
        try {
            URL urlObj = new URL(address);
            HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
            oc.setUseCaches(false);
            oc.setConnectTimeout(5000);
            statusCode = oc.getResponseCode();
            if (200 == statusCode) {
                return statusCode;
            }
        } catch (Exception ignore) {
        }
        return statusCode;
    }

    public static boolean checkUrl(String address){
        int status = getUrlStatus(address);
        if( 0 == status ){ //异常状态重试一次
            status = getUrlStatus(address);
        }
        if( 200 == status ){
            return true;
        }
        return false;
    }

    public static String getValidAddress(String metaAddress) {
        String validAddress = null;
        String[] addressArr = changeAddressArr(metaAddress);
        for(String address : addressArr){
            if(NetUtil.checkUrl(address)){
                validAddress = address;
                break;
            }
        }
        if(null == validAddress){
            throw new RuntimeException("invalid meta address, please check $env_meta config!");
        }
        return validAddress;
    }

    private static String[] changeAddressArr(String address) {
        String[] addressArr =  address.split(",");
        return addressArr;
    }
}
