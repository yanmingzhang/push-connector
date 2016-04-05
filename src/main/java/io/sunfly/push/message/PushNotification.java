package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;
import io.sunfly.push.model.Notification;

public class PushNotification implements Message {
    private Notification notification;

    public PushNotification() {

    }

    public PushNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public int estimateSize() {
        return 3 + notification.estimateSize();
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.PUSH_NOTIFICATION);

        notification.encode(out);
    }

    @Override
    public void decode(ByteBuf in) {
        notification = new Notification();
        notification.decode(in);
    }

    public Notification getNotification() {
        return notification;
    }
}
