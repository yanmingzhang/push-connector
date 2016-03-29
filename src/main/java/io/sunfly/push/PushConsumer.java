package io.sunfly.push;

import io.netty.channel.Channel;
import io.sunfly.push.message.PushNotification;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class PushConsumer implements Consumer {

    private final Channel channel;

    public PushConsumer(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
    }

    @Override
    public void handleCancelOk(String consumerTag) {
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
        // delivery tag is an increasing sequence number per channel
        long deliveryTag = envelope.getDeliveryTag();

        long timestamp = properties.getTimestamp().getTime();

        PushNotification pn = new PushNotification(deliveryTag, timestamp, null, null);
        channel.write(pn);

        // TODO: implement group flush?
        channel.flush();
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
    }
}
