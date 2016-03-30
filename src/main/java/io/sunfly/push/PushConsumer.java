package io.sunfly.push;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.sunfly.push.message.PushNotification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class PushConsumer extends DefaultConsumer {

    private final ChannelHandlerContext ctx;

    public PushConsumer(com.rabbitmq.client.Channel channel, ChannelHandlerContext ctx) {
        super(channel);

        this.ctx = ctx;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
            throws IOException {
        // delivery tag is an increasing sequence number per channel
        long deliveryTag = envelope.getDeliveryTag();

        long timestamp = 0;
        if (properties.getTimestamp() != null) {
            timestamp = properties.getTimestamp().getTime();
        }

        PushNotification pn = new PushNotification(deliveryTag, timestamp, "",
                new String(body, StandardCharsets.UTF_8));
        // TODO: implement group flush?
        ctx.writeAndFlush(pn).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
            }
        });
    }
}
