package io.sunfly.push.wan;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.sunfly.push.Message;
import io.sunfly.push.wan.message.LoginRequest;
import io.sunfly.push.wan.message.MessageTypes;
import io.sunfly.push.wan.message.NotificationAck;
import io.sunfly.push.wan.message.PushNotification;

import java.util.List;

public class WanMessageDecoder extends ByteToMessageDecoder {

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
        // device to server
        case MessageTypes.REQ_LOGIN:
            message = new LoginRequest();
            break;
        case MessageTypes.NOTIFICATION_ACK:
            message = new NotificationAck();
            break;
        // server to device
        case MessageTypes.PUSH_NOTIFICATION:
            message = new PushNotification();
            break;
        default:
            throw new IllegalArgumentException("Unknown message type");
        }

        message.decode(in);

        out.add(message);
    }
}
