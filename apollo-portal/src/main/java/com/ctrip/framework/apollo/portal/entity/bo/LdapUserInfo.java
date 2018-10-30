package com.ctrip.framework.apollo.portal.entity.bo;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

/**
 * @author xm.lin xm.lin@anxincloud.com
 * @Description
 * @date 18-8-9 下午4:43
 */
@Entry(base = "cn=Manager",objectClasses = {"inetOrgPerson"})
public class LdapUserInfo {

    @Id
    private Name id;
    @Attribute(name = "cn")
    private String username;
    @Attribute(name = "sn")
    private String realName;
    @Attribute(name = "userPassword")
    private String userPassword;
    @Attribute(name = "mail")
    private String mail;

    public Name getId() {
        return id;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
