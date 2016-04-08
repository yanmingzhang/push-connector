package io.sunfly.push.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.sunfly.push.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Notification implements Message {
    private String topic;
    private UUID createTime;    // ms since EPOCH
    private String sender;
    private String content;

    public Notification() {

    }

    public Notification(String topic, UUID createTime,
            String sender, String content) {
        this.topic = topic;
        this.createTime = createTime;
        this.sender = sender;
        this.content = content;
    }

    @Override
    public int estimateSize() {
        return (2 + topic.length() * MAX_BYTES_PER_CHAR_UTF8) + 16 +
               (1 + sender.length() * MAX_BYTES_PER_CHAR_UTF8) +
               (2 + content.length() * MAX_BYTES_PER_CHAR_UTF8);
    }

    @Override
    public void encode(ByteBuf out) {
        int index, length;

        // topic
        index = out.writerIndex();
        out.writerIndex(index + 2);
        length = ByteBufUtil.writeUtf8(out, topic);
        out.setShort(index, length);

        // createTime
        out.writeLong(createTime.getMostSignificantBits());
        out.writeLong(createTime.getLeastSignificantBits());

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

        // topic
        length = in.readUnsignedShort();
        topic = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // createTime
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        createTime = new UUID(mostSigBits, leastSigBits);

        // sender
        length = in.readUnsignedByte();
        sender = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        // content
        length = in.readUnsignedShort();
        content = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);
    }

    public String getTopic() {
        return topic;
    }

    public UUID getCreateTime() {
        return createTime;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}
