package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;

public class AckNotification implements Message {

    private long deliveryTag;

    public AckNotification() {
    }

    public AckNotification(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    @Override
    public int estimateSize() {
        return 3 + 8;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.ACK_NOTIFICATION);

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
