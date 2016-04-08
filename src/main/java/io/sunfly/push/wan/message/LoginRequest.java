package io.sunfly.push.wan.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.sunfly.push.Message;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginRequest implements Message {
    private String deviceId;
    private Map<String, UUID> topicOffsetMap;

    public LoginRequest() {

    }

    public LoginRequest(String deviceId, Map<String, UUID> topicOffsetMap) {
        this.deviceId = deviceId;
        this.topicOffsetMap = topicOffsetMap;
    }

    @Override
    public int estimateSize() {
        return 3 + (1 + deviceId.length() * MAX_BYTES_PER_CHAR_UTF8);
    }

    @Override
    public void encode(ByteBuf out) {
        int index, length;

        out.writeByte(MessageTypes.REQ_LOGIN);

        // deviceId
        index = out.writerIndex();
        out.writerIndex(index + 1);
        length = ByteBufUtil.writeUtf8(out, deviceId);
        out.setByte(index, length);

        // topicOffsetMap
        out.writeShort(topicOffsetMap.size());
        for (Map.Entry<String, UUID> entry: topicOffsetMap.entrySet()) {
            // topic
            String topic = entry.getKey();
            index = out.writerIndex();
            out.writerIndex(index + 2);
            length = ByteBufUtil.writeUtf8(out, topic);
            out.setShort(index, length);

            // offset
            UUID offset = entry.getValue();
            out.writeLong(offset.getMostSignificantBits());
            out.writeLong(offset.getLeastSignificantBits());
        }
    }

    @Override
    public void decode(ByteBuf in) {
        int length;

        // deviceId
        length = in.readUnsignedByte();
        deviceId = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // topicOffsetMap
        int size = in.readUnsignedShort();
        if (size == 0) {
            topicOffsetMap = Collections.emptyMap();
        } else {
            topicOffsetMap = new HashMap<String, UUID>(size);
            for (int i = 0; i < size; ++i) {
                // topic
                length = in.readUnsignedShort();
                String topic = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
                in.skipBytes(length);

                // offset
                long mostSigBits = in.readLong();
                long leastSigBits = in.readLong();
                UUID offset = new UUID(mostSigBits, leastSigBits);

                topicOffsetMap.put(topic, offset);
            }
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Map<String, UUID> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
