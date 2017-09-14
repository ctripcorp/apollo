package com.ctrip.framework.apollo.portal.spi.ldap.controller;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.spi.ldap.LDAPUtil;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {



    @RequestMapping(value = "/doLogin", method = RequestMethod.POST )
    @ResponseBody
    public JsonObject login(@RequestParam String userName, @RequestParam String password,
                         HttpServletRequest request) {
        JsonObject message = new JsonObject();
        try {
            UserInfo currentUser = LDAPUtil.login(userName, password);
            request.getSession().setAttribute("currentUser", currentUser);
            message.addProperty("code", 200);
            message.addProperty("message", "success");
        } catch (Exception e) {
            message.addProperty("code", 100);
            message.addProperty("message", e.getMessage());
        }
        return message;
    }

    @RequestMapping("/toLogin")
    public ModelAndView toLogin() {
        return new ModelAndView("login.html");
    }


}
