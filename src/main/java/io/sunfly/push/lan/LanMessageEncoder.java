package io.sunfly.push.lan;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.sunfly.push.Message;

public class LanMessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int index = out.writerIndex();
        out.writerIndex(out.writerIndex() + 4);     // size

        msg.encode(out);
        out.setInt(index, out.readableBytes());
    }

    @Override
    public ByteBuf allocateBuffer(ChannelHandlerContext ctx, Message msg, boolean preferDirect) throws Exception {
        int msgSize = msg.estimateSize();

        if (preferDirect) {
            return ctx.alloc().ioBuffer(msgSize, msgSize);
        } else {
            return ctx.alloc().heapBuffer(msgSize, msgSize);
        }
    }
}
