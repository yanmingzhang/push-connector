package io.sunfly.push;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.sunfly.push.message.GetNotificationRequest;
import io.sunfly.push.message.LoginRequest;
import io.sunfly.push.message.Message;
import io.sunfly.push.message.NotificationAck;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorServerHandler.class);

    private final CassandraClient cassandraClient;
    private final ExecutorService executorService;
    private String deviceId;

    public ConnectorServerHandler(CassandraClient cassandraClient, ExecutorService executorService) {
        this.cassandraClient = cassandraClient;
        this.executorService = executorService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof LoginRequest) {
            LoginRequest request = (LoginRequest)msg;
            deviceId = request.getDeviceId();
            return;
        }

        if (deviceId == null) {
            // Bad request
            ctx.close();
            return;
        }

        if (msg instanceof GetNotificationRequest) {
            GetNotificationRequest request = (GetNotificationRequest)msg;
            GetNotificationTask task = new GetNotificationTask(cassandraClient, ctx,
                    deviceId, request.getMinCreateTime());
            executorService.submit(task);
        } else if (msg instanceof NotificationAck) {
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();

        if (cause instanceof ReadTimeoutException) {
            return;
        }

        if ((cause instanceof IOException) && cause.getMessage() != null &&
                cause.getMessage().endsWith("Connection reset by peer")) {
            return;
        }

        // log exception
        StringBuilder sb = new StringBuilder("Exception caught");
        if (deviceId != null) {
            sb.append("(device id = ").append(deviceId).append(")");
        }

        logger.warn(sb.toString(), cause);
    }
}
