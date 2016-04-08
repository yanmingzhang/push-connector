package io.sunfly.push.wan;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.sunfly.push.CassandraClient;
import io.sunfly.push.Config;
import io.sunfly.push.ConnectorServer;
import io.sunfly.push.Message;
import io.sunfly.push.ReadNotificationsTask;
import io.sunfly.push.wan.message.LoginRequest;
import io.sunfly.push.wan.message.NotificationAck;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WanServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(WanServerHandler.class);

    private final ConnectorServer server;
    private final CassandraClient cassandraClient;
    private final ExecutorService executorService;
    private String deviceId;

    public WanServerHandler(ConnectorServer server, CassandraClient cassandraClient,
            ExecutorService executorService) {
        this.server = server;
        this.cassandraClient = cassandraClient;
        this.executorService = executorService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof LoginRequest) {
            LoginRequest request = (LoginRequest)msg;
            deviceId = request.getDeviceId();
            ReadNotificationsTask task = new ReadNotificationsTask(server, cassandraClient, ctx,
                    deviceId, request.getTopicOffsetMap());
            executorService.execute(task);
            return;
        }

        if (deviceId == null) {
            // Bad request
            ctx.close();
            return;
        }

        if (msg instanceof NotificationAck) {
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (deviceId != null) {
            server.getDevices().remove(deviceId);

            // remove this device from cassandra's online set
            final Config conf = server.getConf();
            cassandraClient.setOffline(deviceId, conf.getLanListenIp(), conf.getLanListenPort());

            deviceId = null;
        }
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
            sb.append("(deviceId = ").append(deviceId).append(")");
        }

        logger.warn(sb.toString(), cause);
    }
}
