package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class GetNotificationRequest implements Message {
    private UUID minCreateTime;

    public GetNotificationRequest() {

    }

    public GetNotificationRequest(UUID minCreateTime) {
        this.minCreateTime = minCreateTime;
    }

    @Override
    public int estimateSize() {
        return 3 + 16;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.REQ_GET_NOTIFICATION);

        out.writeLong(minCreateTime.getMostSignificantBits());
        out.writeLong(minCreateTime.getLeastSignificantBits());
    }

    @Override
    public void decode(ByteBuf in) {
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();

        minCreateTime = new UUID(mostSigBits, leastSigBits);
    }

    public UUID getMinCreateTime() {
        return minCreateTime;
    }
}
