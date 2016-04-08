package io.sunfly.push.model;

import java.util.Date;
import java.util.Set;

public class Device {
    private String id;
    private Date lastActiveTime;
    private String province;
    private Set<String> topics;

    public Device() {

    }

    public Device(String id, Date lastActiveTime, String province, Set<String> topics) {
        this.id = id;
        this.lastActiveTime = lastActiveTime;
        this.province = province;
        this.topics = topics;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }
}
