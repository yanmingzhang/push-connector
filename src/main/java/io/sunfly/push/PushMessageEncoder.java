package io.sunfly.push;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.sunfly.push.message.Message;

public class PushMessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        msg.encode(out);
    }

    @Override
    public ByteBuf allocateBuffer(ChannelHandlerContext ctx, Message msg, boolean preferDirect) throws Exception {
        if (preferDirect) {
            return ctx.alloc().ioBuffer();
        } else {
            return ctx.alloc().heapBuffer();
        }
    }
}
