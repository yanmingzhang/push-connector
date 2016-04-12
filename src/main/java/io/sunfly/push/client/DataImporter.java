package io.sunfly.push.client;

import java.util.Collections;
import java.util.Set;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class DataImporter {
    private static final int TOTAL_COUNT = 100000;
    private static final String ADDRESS = "10.240.225.101";
    private static final String TOPIC = "ALL@io.sunfly.push";

    public static void main(String[] args) {
        Cluster cluster = Cluster.builder().addContactPoints(ADDRESS).build();
        Session session = cluster.connect("push");

        PreparedStatement ps;
        BoundStatement bs;
        String deviceId;
        Set<String> topics = Collections.singleton(TOPIC);

        // insert into devices table
        ps = session.prepare("INSERT INTO devices(device_id, last_active_time, province, topics) VALUES(?, toTimestamp(now()), ?, ?)");
        for (int i = 0; i < TOTAL_COUNT; ++i) {
            deviceId = String.format("push-test-%07d", i);
            bs = ps.bind(deviceId, "shanghai", topics);
            session.execute(bs);
        }

        // inset into topics table
        ps = session.prepare("INSERT INTO topics(topic, device_id, subscribe_time) VALUES(?, ?, toTimestamp(now()))");
        for (int i = 0; i < TOTAL_COUNT; ++i) {
            deviceId = String.format("push-test-%07d", i);
            bs = ps.bind(TOPIC, deviceId);
            session.execute(bs);
        }

        session.close();
        cluster.close();
    }
}
