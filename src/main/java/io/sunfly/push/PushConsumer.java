package io.sunfly.push;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import io.netty.channel.Channel;

public class PushConsumer implements Consumer {

    private final Channel channel;

    public PushConsumer(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleCancelOk(String consumerTag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
        channel.write(body);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        // TODO Auto-generated method stub

    }
}
