package com.ctrip.apollo.client.model;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public class ApolloRegistry {
    private long appId;
    private String version;

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
