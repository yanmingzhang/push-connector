package io.sunfly.push;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

public class RabbitmqClient implements AutoCloseable {
    private final Connection conn;
    private final Channel channel;

    // Key: queue name  Value: consumer tag
    private ConcurrentHashMap<String, String> consumerMap;

    public RabbitmqClient() throws IOException, TimeoutException {
        ConnectionFactory cf = new ConnectionFactory();

        this.conn = cf.newConnection();
        this.channel = conn.createChannel();

        consumerMap = new ConcurrentHashMap<>(8192);
    }

    public void consume(String queue, Consumer callback) throws IOException {
        if (consumerMap.containsKey(queue)) {
            // dup consumer?
            throw new IllegalStateException("Duplicate consumer");
        }

        channel.queueDeclare(queue, true, false, false, null);

        String consumerTag = channel.basicConsume(queue, false, callback);

        consumerMap.put(queue, consumerTag);
    }

    public void cancel(String queue) throws IOException {
        String consumerTag = consumerMap.get(queue);
        if (consumerTag != null) {
            channel.basicCancel(consumerTag);
        }
    }

    @Override
    public void close() throws Exception {
        // cancel consumers
        for (String consumerTag: consumerMap.values()) {
            try { channel.basicCancel(consumerTag); } catch (Exception ex) {}
        }

        try { channel.close(); } catch (Exception ex) {}

        try { conn.close(); } catch (Exception ex) {}
    }
}
