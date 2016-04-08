package io.sunfly.push.lan;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.sunfly.push.Message;
import io.sunfly.push.lan.message.MessageTypes;
import io.sunfly.push.lan.message.LanPushNotification;

import java.util.List;

public class LanMessageDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();

        if (readableBytes < 4) {
            return;
        }

        long size = in.getUnsignedInt(in.readerIndex());
        if (readableBytes < size) {
            return;
        }

        // decode message
        in.skipBytes(4);    // skip size field

        if (size == 4) {
            // heartbeat
            return;
        }

        int type = in.readUnsignedShort();
        Message message;
        switch (type) {
        case MessageTypes.LAN_PUSH_NOTIFICATION:
            message = new LanPushNotification();
            break;
        default:
            throw new IllegalArgumentException("Unknown message type");
        }

        message.decode(in);

        out.add(message);
    }
}
