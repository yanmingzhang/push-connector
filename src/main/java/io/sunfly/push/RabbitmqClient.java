package io.sunfly.push;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitmqClient implements AutoCloseable {
    private final Connection conn;
    private final Channel channel;

    // Key: queue name  Value: consumer tag
    private ConcurrentHashMap<String, String> consumerMap;

    public RabbitmqClient(Config conf) throws IOException, TimeoutException {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(conf.getMqAddresses());
        cf.setUsername(conf.getMqUsername());
        cf.setPassword(conf.getMqPassword());

        this.conn = cf.newConnection();
        this.channel = conn.createChannel();
        // a queue will bind to a specific thread to dispatch it's
        // messages (through consumer callback), so don't worry about
        // messages on a queue maybe dispatched by different thread
        int prefetchCount = conf.getMqPrefetchCount();
        if (prefetchCount > 0) {
            channel.basicQos(prefetchCount);
        }

        consumerMap = new ConcurrentHashMap<>(16384);
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

    public void ack(long deliveryTag, boolean multiple) throws IOException {
        channel.basicAck(deliveryTag, multiple);
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

    public static void main(String[] args) throws Exception {
        Config conf = Config.load();
        RabbitmqClient c = new RabbitmqClient(conf);
        c.consume("push-test", new DefaultConsumer(null) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                System.out.println(Thread.currentThread());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {

                }
            }
        });
        Thread.sleep(100000);
        c.close();
    }
}
