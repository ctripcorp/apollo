package com.ctrip.framework.apollo.portal.spi.ldap;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.unboundid.ldap.sdk.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evan on 2017/2/20.
 * <p>
 * ldap 用户系统打通
 */
public class LDAPUtil {

    // 当前配置信息
    private static final String LDAP_HOST = "";
    private static final String LDAP_BIND_DN = "";
    private static final String LDAP_PASSWORD = "";
    private static final int LDAP_PORT = 389;

    // 邮箱后缀
    public static final String EMAIL_SUFFIX = "";

    /**
     * 获取ldap连接.
     * @return
     * @throws Exception
     */
    public static LDAPConnection getConnection() throws Exception {
        return new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_BIND_DN, LDAP_PASSWORD);
    }

    /**
     * 用户登陆
     *
     * @return 用户信息
     */
    public static UserInfo login(String userName, String password) throws Exception {
        String mail = userName + EMAIL_SUFFIX;
        String dn = getDN(mail);
        if (StringUtils.isEmpty(dn)) {
            throw new Exception("不存在的用户!");
        }
        try {
            new LDAPConnection(LDAP_HOST, LDAP_PORT, dn, password);
        } catch (Exception e) {
            throw new Exception("账号或者密码不正确!");
        }
        return findUser(userName);
    }


    /**
     * @param userId 用户id
     * @return 返回用户信息
     */
    public static UserInfo findUser(String userId) throws Exception {
        UserInfo userInfo = new UserInfo();
        String mail = userId + EMAIL_SUFFIX;
        String name = getCN(mail);
        userInfo.setEmail(mail);
        userInfo.setUserId(mail.replace(EMAIL_SUFFIX, ""));
        userInfo.setName(name);
        return userInfo;
    }


    /**
     * 根据条件查找用户
     * @param keyword 搜索关键字
     * @param offset  偏移
     * @param limit   查询多少个返回
     * @return 查找的用户集
     */
    public static ArrayList<UserInfo> findUsers(String keyword, int offset, int limit) throws Exception {
        ArrayList<UserInfo> res = new ArrayList<>();
        SearchResult result = fetchAll();
        if (result == null) return res;
        List<SearchResultEntry> searchEntries = result.getSearchEntries();
        int totalCount = 0;
        for (int i = offset; i < searchEntries.size(); i++) {
            SearchResultEntry entry = searchEntries.get(i);
            if (res.size() >= limit) return res;
            if (entry == null)
                continue;
            String displayName = entry.getAttributeValue("displayName");
            String accountName = entry.getAttributeValue("sAMAccountName");

            if (displayName.contains(keyword) || accountName.contains(keyword)) {
                totalCount = totalCount + 1;
                if (totalCount >= offset) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(accountName);
                    userInfo.setName(entry.getAttributeValue("cn"));
                    userInfo.setEmail(entry.getAttributeValue("mail"));
                    res.add(userInfo);
                }
            }
        }
        return res;
    }

    /**
     * 查询已经知道的用户集合
     * @param userIds 多个用户userId
     * @return 返回查询的用户信息集合
     */
    public static ArrayList<UserInfo> findUsers(List<String> userIds) throws Exception {
        ArrayList<UserInfo> res = new ArrayList<>();
        SearchResult result = fetchAll("mail");
        List<SearchResultEntry> searchEntries = result.getSearchEntries();
        if (searchEntries == null) return res;
        for (SearchResultEntry entry : searchEntries) {
            if (entry != null) {
                for (String userId : userIds) {
                    String mail = entry.getAttributeValue("mail");
                    if (StringUtils.isNotBlank(mail) && StringUtils.equals(userId + EMAIL_SUFFIX, mail)) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(userId);
                        userInfo.setName(entry.getAttributeValue("cn"));
                        userInfo.setEmail(mail);
                        res.add(userInfo);
                    }
                }
            }
        }
        return res;
    }

    /**
     * 查询用户的dn
     */
    private static String getDN(String userName) throws Exception {
        SearchResult result = fetchAll("mail");
        if (result == null)
            return null;
        for (SearchResultEntry searchResultEntry : result.getSearchEntries()) {
            String mail = searchResultEntry.getAttributeValue("mail");
            if (StringUtils.isNotEmpty(mail) && StringUtils.equals(mail, userName)) {
                return searchResultEntry.getDN();
            }
        }
        return null;
    }

    /**
     * 获取LDAP中所有用户信息
     * @param attributes
     * @return
     * @throws Exception
     */
    private static SearchResult fetchAll(String... attributes) throws Exception {
        String baseDN = "";
        String filter = "objectClass=user";
        return getConnection().search(baseDN, SearchScope.SUB, filter, attributes);
    }

    /**
     * 获取所有用户
     */
    private static ArrayList<UserInfo> getAllUsers() throws Exception {
        ArrayList<UserInfo> res = new ArrayList<>();
        SearchResult searchResult = fetchAll();
        List<SearchResultEntry> searchEntries = searchResult.getSearchEntries();
        if (searchEntries == null) return res;
        for (SearchResultEntry resultEntry : searchEntries) {
            if (resultEntry != null) {
                String mail = resultEntry.getAttributeValue("mail");
                String name = resultEntry.getAttributeValue("cn");
                String userId = resultEntry.getAttributeValue("sAMAccountName");
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(userId);
                userInfo.setName(name);
                userInfo.setEmail(mail);
                res.add(userInfo);
            }
        }
        return res;
    }

    /**
     * 根据用户的mail获取用户名
     */
    private static String getCN(String mail) throws Exception {
        SearchResult result = fetchAll();
        List<SearchResultEntry> searchEntries = result.getSearchEntries();
        if (searchEntries == null) return null;
        for (SearchResultEntry entry : searchEntries) {
            if (entry != null) {
                String value_mail = entry.getAttributeValue("mail");
                if (StringUtils.isNotEmpty(value_mail) && StringUtils.equals(value_mail, mail))
                    return entry.getAttributeValue("cn");
            }
        }
        throw new Exception("不存在的用户!");
    }

}
