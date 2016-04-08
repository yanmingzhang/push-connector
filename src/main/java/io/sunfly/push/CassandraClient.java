package io.sunfly.push;

import io.sunfly.push.model.Device;
import io.sunfly.push.model.Notification;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

    public Device getDevice(String deviceId) {
        PreparedStatement ps = session.prepare("SELECT last_active_time, province, topics FROM devices WHERE device_id=?");
        BoundStatement bs = ps.bind(deviceId);

        Row row = session.execute(bs).one();
        if (row == null) {
            return null;
        }

        Date lastActiveTime = row.getTimestamp(0);
        String province = row.getString(1);
        Set<String> topics = row.getSet(2, String.class);

        return new Device(deviceId, lastActiveTime, province, topics);
    }

    public List<Notification> getNotifications(String topic, UUID offset) {
        // topic format:  t:in_app_topic_name@app_name or d:device_id@app_name

        if (offset == null) {
            offset = UUIDs.startOf(0);
        }

        PreparedStatement ps = session.prepare("SELECT create_time, sender, content FROM messages " +
                                               "WHERE topic = ? AND create_time > ? ORDER BY create_time ASC");
        BoundStatement bs = ps.bind(topic, offset);
        ResultSet rs = session.execute(bs);

        List<Notification> notifications = new ArrayList<Notification>();
        for (Row row: rs) {
            UUID createTime = row.getUUID(0);
            String sender = row.getString(1);
            String content = row.getString(2);

            notifications.add(new Notification(topic, createTime, sender, content));
        }

        return notifications;
    }

    public void setOnline(String deviceId, InetAddress lanListenIp, int lanListenPort) {
        PreparedStatement ps = session.prepare("INSERT INTO online_devices(device_id, gate_svr_ip, gate_svr_port) VALUES(?, ?, ?)");
        BoundStatement bs = ps.bind(deviceId, lanListenIp, (short)lanListenPort);
        session.execute(bs);
    }

    public void setOffline(String deviceId, InetAddress lanListenIp, int lanListenPort) {
        PreparedStatement ps = session.prepare("DELETE FROM online_devices WHERE device_id = ? IF gate_svr_ip = ? AND gate_svr_port = ?");
        BoundStatement bs = ps.bind(deviceId, lanListenIp, (short)lanListenPort);
        session.execute(bs);
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
