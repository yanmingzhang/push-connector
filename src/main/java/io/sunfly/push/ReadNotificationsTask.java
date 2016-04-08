package io.sunfly.push;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.OneTimeTask;
import io.sunfly.push.model.Device;
import io.sunfly.push.model.Notification;
import io.sunfly.push.wan.message.PushNotification;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReadNotificationsTask implements Runnable {
    private final ConnectorServer server;
    private final CassandraClient cassandraClient;
    private final ChannelHandlerContext ctx;
    private final String deviceId;
    private final Map<String, UUID> topicOffsetMap;

    public ReadNotificationsTask(ConnectorServer server, CassandraClient cassandraClient,
            ChannelHandlerContext ctx, String deviceId,
            Map<String, UUID> topicOffsetMap) {
        this.server = server;
        this.cassandraClient = cassandraClient;
        this.ctx = ctx;
        this.deviceId = deviceId;
        this.topicOffsetMap = topicOffsetMap;
    }

    @Override
    public void run() {
        Device device = cassandraClient.getDevice(deviceId);
        if (device == null) {
            // TODO: insert into devices table?
            return;
        }

        Set<String> topics = device.getTopics();

        if (topics != null) {
            for (String topic: topics) {
                UUID offset = topicOffsetMap.get(topic);
                List<Notification> notifications = cassandraClient.getNotifications(topic, offset);
                for (Notification notification: notifications) {
                    PushNotification pn = new PushNotification(notification);
                    ctx.write(pn);
                }
            }
        }

        // we run flush and put to online device set in the executor
        // associated with the channel, so we won't suffer from following
        // condition:  1) channel closed and channelInactive function was called
        // 2) this task finished and add the device to online set dumbly
        // finally, the device will always in the online set
        ctx.executor().execute(new OneTimeTask() {
            @Override
            public void run() {
                if (ctx.channel().isActive()) {
                    ctx.flush();

                    // add to devices
                    // ATTENTION: we do this after read all notifications, so MAYBE lost notifications
                    // if a new notification arrival between we read notifications and put to online
                    // device set. if we do this before read any notifications, client MAYBE get
                    // DUPLICATE notification and OLDER notification (client get new notification first,
                    // then get readed old notification)
                    server.getDevices().put(deviceId, ctx);

                    // add to cassandra's online set
                    // FIXME: maybe move this call out of the io thread?
                    //        but then maybe suffer out of order event problem
                    final Config conf = server.getConf();
                    cassandraClient.setOnline(deviceId, conf.getLanListenIp(), conf.getLanListenPort());
                }
            }
        });
    }
}
