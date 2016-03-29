package io.sunfly.push;

import io.netty.channel.Channel;
import io.sunfly.push.message.PushNotification;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class PushConsumer extends DefaultConsumer {

    private final Channel ioChannel;

    public PushConsumer(com.rabbitmq.client.Channel channel, Channel ioChannel) {
        super(channel);

        this.ioChannel = ioChannel;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
        // delivery tag is an increasing sequence number per channel
        long deliveryTag = envelope.getDeliveryTag();

        long timestamp = properties.getTimestamp().getTime();

        PushNotification pn = new PushNotification(deliveryTag, timestamp, null, null);
        ioChannel.write(pn);

        // TODO: implement group flush?
        ioChannel.flush();
    }

    public Channel getIoChannel() {
        return ioChannel;
    }
}
