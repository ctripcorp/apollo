package com.ctrip.framework.apollo.common.aop;

import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Auther: jiangcaijun
 * @Date: 2018/4/16 15:58
 * @Description:
 * @Component：注册到Spring容器，必须加入这个注解
 * @Aspect // 该注解标示该类为切面类，切面是由通知和切点组成的。
 */
@Component
@Aspect
public class ExceptionReturnAspect {

    private static Logger logger = LoggerFactory.getLogger(ExceptionReturnAspect.class);

    @Autowired
    private HttpServletRequest request;

    @Pointcut("execution(public * com.ctrip.framework.apollo.*.controller..*.*(..))")
    public void exceptionReturnAspect() {
    }

    private static final String URL = "url";
    private static final String REQUEST_PARAM = "request_param";
    private static final String IP = "ip";
    private static final String ERROR_MSG = "error_msg";

    @AfterThrowing(throwing="exception", pointcut = "exceptionReturnAspect()")
    public void afterThrowingAdvice(JoinPoint joinPoint, RuntimeException exception){
        JSONObject result = new JSONObject();
        result.put(URL, request.getRequestURI());
        result.put(REQUEST_PARAM, getInputParams());
        result.put(IP, getIpAddr(request));
        result.put(ERROR_MSG, exception.getMessage());
        logger.error(result.toJSONString());
    }

    /**
     * 获取请求参数，eg：param:courier_id, silence_courier, travel_way;courier_ids:2600119, 2600120;
     * @return
     */
    private JSONObject getInputParams() {
        JSONObject jsonObject = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    jsonObject.put(paramName, paramValue);
                }
            }
        }
        return jsonObject;
    }
    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}