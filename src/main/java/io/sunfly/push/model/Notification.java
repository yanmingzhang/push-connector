package io.sunfly.push.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.sunfly.push.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Notification implements Message {
    private String appName;
    private UUID createTime;    // ms since EPOCH
    private String topic;
    private String sender;
    private String content;

    public Notification() {

    }

    public Notification(String appName, UUID createTime, String topic,
            String sender, String content) {
        this.appName = appName;
        this.createTime = createTime;
        this.topic = topic;
        this.sender = sender;
        this.content = content;
    }

    @Override
    public int estimateSize() {
        return 3 + (1 + appName.length() * MAX_BYTES_PER_CHAR_UTF8) +
               16 + (1 + topic.length() * MAX_BYTES_PER_CHAR_UTF8) +
               (1 + sender.length() * MAX_BYTES_PER_CHAR_UTF8) +
               (2 + content.length() * MAX_BYTES_PER_CHAR_UTF8);
    }

    @Override
    public void encode(ByteBuf out) {
        int index, length;

        // appName
        index = out.writerIndex();
        out.writerIndex(index + 1);
        length = ByteBufUtil.writeUtf8(out, appName);
        out.setByte(index, length);

        // createTime
        out.writeLong(createTime.getMostSignificantBits());
        out.writeLong(createTime.getLeastSignificantBits());

        // topic
        index = out.writerIndex();
        out.writerIndex(index + 1);
        length = ByteBufUtil.writeUtf8(out, topic);
        out.setByte(index, length);

        // sender
        index = out.writerIndex();
        out.writerIndex(index + 1);
        length = ByteBufUtil.writeUtf8(out, sender);
        out.setByte(index, length);

        // content
        index = out.writerIndex();
        out.writerIndex(index + 2);
        length = ByteBufUtil.writeUtf8(out, content);
        out.setShort(index, length);
    }

    @Override
    public void decode(ByteBuf in) {
        int length;

        // appName
        length = in.readUnsignedByte();
        appName = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // createTime
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        createTime = new UUID(mostSigBits, leastSigBits);

        // topic
        length = in.readUnsignedByte();
        topic = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // sender
        length = in.readUnsignedByte();
        sender = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // content
        length = in.readUnsignedShort();
        content = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);
    }

    public String getAppName() {
        return appName;
    }

    public UUID getCreateTime() {
        return createTime;
    }

    public String getTopic() {
        return topic;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}
