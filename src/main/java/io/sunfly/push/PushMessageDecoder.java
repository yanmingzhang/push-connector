package io.sunfly.push;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.sunfly.push.message.LoginRequest;
import io.sunfly.push.message.Message;
import io.sunfly.push.message.MessageTypes;

public class PushMessageDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();

        if (readableBytes < 6)
            return;

        long size = in.getUnsignedInt(in.readerIndex());
        if (readableBytes < size)
            return;

        // decode message
        in.skipBytes(4);    // size
        int type = in.readUnsignedShort();
        Message message;
        switch (type) {
        case MessageTypes.REQ_LOGIN:
            message = new LoginRequest();
            break;
        default:
            throw new IllegalArgumentException();
        }

        message.decode(in);

        out.add(message);
    }
}
