package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;

public class PushNotification implements Message {
    private long deliveryTag;
    private long timestamp;     // elapsed milliseconds since EPOCH
    private String title;
    private String content;

    public PushNotification() {

    }

    public PushNotification(long deliveryTag, long timestamp, String title, String content) {
        this.deliveryTag = deliveryTag;
        this.timestamp = timestamp;
        this.title = title;
        this.content = content;
    }

    @Override
    public int estimateSize() {
        return 3 + 8 + 8 + (1 + title.length() * MAX_BYTES_PER_CHAR_UTF8) +
                (2 + content.length() * MAX_BYTES_PER_CHAR_UTF8);
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.PUSH_NOTIFICATION);

        out.writeLong(deliveryTag);
        out.writeLong(timestamp);

        int index;
        int length;

        if (title.length() > 0) {
            index = out.writerIndex();
            out.writerIndex(index + 1);
            length = ByteBufUtil.writeUtf8(out, title);
            out.setByte(index, length);
        } else {
            out.writeByte(0);
        }

        //
        index = out.writerIndex();
        out.writerIndex(index + 2);
        length = ByteBufUtil.writeUtf8(out, content);
        out.setShort(index, length);
    }

    @Override
    public void decode(ByteBuf in) {
        deliveryTag = in.readLong();
        timestamp = in.readLong();

        int length;

        length = in.readUnsignedByte();
        if (length > 0) {
            title = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
            in.skipBytes(length);
        }

        length = in.readUnsignedShort();
        content = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
