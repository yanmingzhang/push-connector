package io.sunfly.push.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.sunfly.push.model.Notification;
import io.sunfly.push.wan.message.PushNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    public static final AttributeKey<String> AK_DEVICE_ID = AttributeKey.newInstance("deviceId");

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            ByteBuf buf = ctx.alloc().ioBuffer(2, 2);
            buf.writeShort(2);

            ctx.writeAndFlush(buf);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof PushNotification) {
            PushNotification pn = (PushNotification)msg;
            Notification notification = pn.getNotification();
            logger.info("deviceId = {}, topic = {}, create_time = {}, content = {}",
                    ctx.channel().attr(AK_DEVICE_ID).get(), notification.getTopic(),
                    notification.getCreateTime(), notification.getContent());
        } else {
            logger.warn("Unknown message: " + msg.getClass().getSimpleName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.warn("Exception caught", cause);
    }
}
