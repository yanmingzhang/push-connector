package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;

public class NotificationAck implements Message {

    private long deliveryTag;

    public NotificationAck() {
    }

    public NotificationAck(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    @Override
    public int estimateSize() {
        return 3 + 8;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.NOTIFICATION_ACK);

        out.writeLong(deliveryTag);
    }

    @Override
    public void decode(ByteBuf in) {
        deliveryTag = in.readLong();
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }
}
