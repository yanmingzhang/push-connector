package io.sunfly.push;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.sunfly.push.message.LoginRequest;
import io.sunfly.push.message.Message;
import io.sunfly.push.message.NotificationAck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorServerHandler.class);

    private final RabbitmqClient rabbitmqClient;
    private String deviceId;

    public ConnectorServerHandler(final RabbitmqClient rabbitmqClient) {
        this.rabbitmqClient = rabbitmqClient;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof NotificationAck) {
            NotificationAck ack = (NotificationAck)msg;

            // because we use tcp to transfer push notification, so here
            // we can ack multiple
            rabbitmqClient.ack(ack.getDeliveryTag(), false);
        } if (msg instanceof LoginRequest) {
            LoginRequest request = (LoginRequest)msg;

            deviceId = request.getDeviceId();

            try {
                rabbitmqClient.consume(deviceId, new PushConsumer(ctx.channel()));
            } catch (Exception ex) {
                ctx.close();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (deviceId != null) {
            rabbitmqClient.cancel(deviceId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof ReadTimeoutException)) {
            StringBuilder sb = new StringBuilder("Exception caught");
            if (deviceId != null) {
                sb.append("(device id = ").append(deviceId).append(")");
            }

            logger.warn(sb.toString(), cause);
        }
        ctx.close();
    }
}
