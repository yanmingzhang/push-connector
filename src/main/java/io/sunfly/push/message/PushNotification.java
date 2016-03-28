package io.sunfly.push.message;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class PushNotification implements Message {
    private String title;
    private String content;

    public PushNotification() {

    }

    public PushNotification(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public void encode(ByteBuf out) {
        int index;
        int length;

        index = out.writerIndex();
        out.writerIndex(index + 1);
        length = ByteBufUtil.writeUtf8(out, title);
        out.setByte(index, length);

        //
        index = out.writerIndex();
        out.writerIndex(index + 2);
        length = ByteBufUtil.writeUtf8(out, content);
        out.setShort(index, length);
    }

    @Override
    public void decode(ByteBuf in) {
        int length;

        length = in.readUnsignedByte();
        title = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        length = in.readUnsignedShort();
        content = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
