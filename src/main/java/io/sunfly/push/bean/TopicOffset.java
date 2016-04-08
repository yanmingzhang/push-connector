package io.sunfly.push.bean;

import java.util.UUID;

public class TopicOffset {
    private final String topic;
    private final UUID offset;

    public TopicOffset(String topic, UUID offset) {
        this.topic = topic;
        this.offset = offset;
    }

    public String getTopic() {
        return topic;
    }

    public UUID getOffset() {
        return offset;
    }
}
