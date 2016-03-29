package io.sunfly.push;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitmqClient implements AutoCloseable {
    private final Connection conn;
    private final Config conf;

    // Key: queue name  Value: consumer
    private ConcurrentHashMap<String, PushConsumer> consumerMap;

    public RabbitmqClient(Config conf) throws IOException, TimeoutException {
        this.conf = conf;

        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(conf.getMqAddresses());
        cf.setUsername(conf.getMqUsername());
        cf.setPassword(conf.getMqPassword());
        conn = cf.newConnection();

        consumerMap = new ConcurrentHashMap<String, PushConsumer>(16384);
    }

    public PushConsumer consume(String queue, io.netty.channel.Channel ioChannel) throws IOException {
        if (consumerMap.containsKey(queue)) {
            // dup consumer?
            throw new IllegalStateException("Duplicate consumer");
        }

        Channel channel = conn.createChannel();
        // a consumer will bind to a specific thread to dispatch target queue's
        // messages (through consumer callback), so don't worry about
        // messages on a queue with only one consumer maybe dispatched by different thread
        int prefetchCount = conf.getMqPrefetchCount();
        if (prefetchCount > 0) {
            channel.basicQos(prefetchCount);
        }

        channel.queueDeclare(queue, true, false, false, null);

        PushConsumer consumer = new PushConsumer(channel, ioChannel);
        channel.basicConsume(queue, false, consumer);

        consumerMap.put(queue, consumer);

        return consumer;
    }

    public void cancel(String queue) throws IOException, TimeoutException {
        PushConsumer consumer = consumerMap.remove(queue);
        if (consumer != null) {
            consumer.getChannel().basicCancel(consumer.getConsumerTag());
            consumer.getChannel().close();
        }
    }

    public void ack(PushConsumer consumer, long deliveryTag, boolean multiple) throws IOException {
        consumer.getChannel().basicAck(deliveryTag, multiple);
    }

    @Override
    public void close() throws Exception {
        // cancel consumers
        for (PushConsumer consumer: consumerMap.values()) {
            Channel channel = consumer.getChannel();

            try { channel.basicCancel(consumer.getConsumerTag()); } catch (Exception ex) {}
            try { channel.close(); } catch (Exception ex) {}
        }

        try { conn.close(); } catch (Exception ex) {}
    }
}
