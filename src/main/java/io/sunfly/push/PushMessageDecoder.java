package io.sunfly.push;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.sunfly.push.message.NotificationAck;
import io.sunfly.push.message.LoginRequest;
import io.sunfly.push.message.Message;
import io.sunfly.push.message.MessageTypes;

import java.util.List;

public class PushMessageDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();

        if (readableBytes < 2) {
            return;
        }

        // max 64K bytes
        int size = in.getUnsignedShort(in.readerIndex());
        if (readableBytes < size) {
            return;
        }

        // decode message
        in.skipBytes(2);    // skip size field

        if (size == 2) {
            // heartbeat
            return;
        }

        int type = in.readUnsignedByte();
        Message message;
        switch (type) {
        case MessageTypes.REQ_LOGIN:
            message = new LoginRequest();
            break;
        case MessageTypes.NOTIFICATION_ACK:
            message = new NotificationAck();
            break;
        default:
            throw new IllegalArgumentException();
        }

        message.decode(in);

        out.add(message);
    }
}
