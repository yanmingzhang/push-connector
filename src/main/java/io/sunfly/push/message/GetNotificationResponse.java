package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;
import io.sunfly.push.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class GetNotificationResponse implements Message {
    private List<Notification> notifications;

    public GetNotificationResponse() {

    }

    public GetNotificationResponse(List<Notification> notifications) {
        super();
        this.notifications = notifications;
    }

    @Override
    public int estimateSize() {
        int size = 3;

        size += 2;
        for (Notification notification: notifications) {
            size += notification.estimateSize();
        }

        return size;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(MessageTypes.RSP_GET_NOTIFICATION);

        out.writeShort(notifications.size());
        for (Notification notification: notifications) {
            notification.encode(out);
        }
    }

    @Override
    public void decode(ByteBuf in) {
        int size = in.readUnsignedShort();

        notifications = new ArrayList<Notification>(size);
        for (int i = 0; i < size; ++i) {
            Notification notification = new Notification();
            notification.decode(in);
            notifications.add(notification);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
