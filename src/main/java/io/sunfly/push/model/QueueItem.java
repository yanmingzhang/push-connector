package io.sunfly.push.model;

import java.util.UUID;

public class QueueItem {
    private final String appName;
    private final UUID createTime;
    private final UUID msgCreateTime;

    public QueueItem(String appName, UUID createTime, UUID msgCreateTime) {
        this.appName = appName;
        this.createTime = createTime;
        this.msgCreateTime = msgCreateTime;
    }

    public String getAppName() {
        return appName;
    }

    public UUID getCreateTime() {
        return createTime;
    }

    public UUID getMsgCreateTime() {
        return msgCreateTime;
    }
}
