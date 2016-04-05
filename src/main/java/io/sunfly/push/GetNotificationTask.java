package io.sunfly.push;

import io.netty.channel.ChannelHandlerContext;
import io.sunfly.push.message.GetNotificationResponse;
import io.sunfly.push.model.Notification;

import java.util.List;
import java.util.UUID;

public class GetNotificationTask implements Runnable {
    private final CassandraClient cassandraClient;
    private final ChannelHandlerContext ctx;
    private final String deviceId;
    private final UUID minCreateTime;

    public GetNotificationTask(CassandraClient cassandraClient, ChannelHandlerContext ctx,
            String deviceId, UUID minCreateTime) {
        this.cassandraClient = cassandraClient;
        this.ctx = ctx;
        this.deviceId = deviceId;
        this.minCreateTime = minCreateTime;
    }

    @Override
    public void run() {
        List<Notification> notifications = cassandraClient.getNotifications(deviceId, minCreateTime);
        GetNotificationResponse response = new GetNotificationResponse(notifications);
        ctx.writeAndFlush(response);
    }
}
