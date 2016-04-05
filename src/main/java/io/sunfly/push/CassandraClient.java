package io.sunfly.push;

import io.sunfly.push.model.Notification;
import io.sunfly.push.model.QueueItem;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;

public class CassandraClient implements Closeable {
    private final Cluster cluster;
    private final Session session;

    public CassandraClient(String address) {
        cluster = Cluster.builder().addContactPoints(address).build();
        session = cluster.connect("push");
    }

    public List<Notification> getNotifications(String deviceId, UUID minCreateTime) {
        PreparedStatement ps;
        BoundStatement bs;
        ResultSet rs;

        ps = session.prepare("SELECT app_name, create_time, msg_create_time FROM queues WHERE device_id=? AND create_time>? ORDER BY create_time ASC");
        bs = ps.bind(deviceId, minCreateTime);

        List<QueueItem> items = new ArrayList<QueueItem>();
        rs = session.execute(bs);
        for (Row row: rs) {
            String appName = row.getString(0);
            UUID createTime = row.getUUID(1);
            UUID msgCreateTime = row.getUUID(2);
            items.add(new QueueItem(appName, createTime, msgCreateTime));
        }

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Notification> notifications = new ArrayList<Notification>(items.size());
        ps = session.prepare("SELECT topic, sender, content FROM messages WHERE app_name=? AND create_time=?");
        for (QueueItem item: items) {
            bs = ps.bind(item.getAppName(), item.getMsgCreateTime());
            rs = session.execute(bs);

            Row row = rs.one();
            if (row != null) {
                String topic = row.getString(0);
                String sender = row.getString(1);
                String content = row.getString(2);
                notifications.add(new Notification(item.getAppName(), item.getCreateTime(),
                        topic, sender, content));
            }
        }

        return notifications;
    }

    @Override
    public void close() {
        cluster.close();
    }

    public static void main(String[] args) {
        try (CassandraClient client = new CassandraClient("10.240.225.101")) {
            UUID minCreateTime = UUIDs.startOf(0);

            List<Notification> notifications = client.getNotifications("push-test-000", minCreateTime);

            for (Notification notification: notifications) {
                System.out.println(notification);
            }
        }
    }
}
