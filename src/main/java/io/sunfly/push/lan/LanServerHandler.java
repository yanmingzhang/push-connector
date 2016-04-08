package io.sunfly.push.lan;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.sunfly.push.ConnectorServer;
import io.sunfly.push.Message;
import io.sunfly.push.lan.message.LanPushNotification;
import io.sunfly.push.model.Notification;
import io.sunfly.push.wan.message.PushNotification;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(LanServerHandler.class);

    private final ConnectorServer server;

    public LanServerHandler(ConnectorServer server) {
        this.server = server;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg)
            throws Exception {
        if (msg instanceof LanPushNotification) {
            LanPushNotification lpn = (LanPushNotification)msg;

            List<String> devices = lpn.getDevices();
            Notification notification = lpn.getNotification();

            ConcurrentMap<String, ChannelHandlerContext> onlineDevices = server.getDevices();
            for (String device: devices) {
                ChannelHandlerContext context = onlineDevices.get(device);
                if (context != null) {
                    context.writeAndFlush(new PushNotification(notification));
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();

        if (cause instanceof ReadTimeoutException) {
            return;
        }

        if ((cause instanceof IOException) && cause.getMessage() != null &&
                cause.getMessage().endsWith("Connection reset by peer")) {
            return;
        }

        // log exception
        logger.warn("Exception caught", cause);
    }
}
