package io.sunfly.push.lan.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.sunfly.push.Message;
import io.sunfly.push.model.Notification;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LanPushNotification implements Message {
    private Notification notification;
    private List<String> devices;

    public LanPushNotification() {

    }

    public LanPushNotification(Notification notification, List<String> devices) {
        this.notification = notification;
        this.devices = devices;
    }

    @Override
    public int estimateSize() {
        int size = 6;   // message size: 4, message type size: 2

        size += notification.estimateSize();

        for (String device: devices) {
            size += (1 + device.length() * MAX_BYTES_PER_CHAR_UTF8);
        }

        return size;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeShort(MessageTypes.LAN_PUSH_NOTIFICATION);
        notification.encode(out);

        out.writeShort(devices.size());
        for (String device: devices) {
            int index = out.writerIndex();
            out.writerIndex(index + 1);
            int length = ByteBufUtil.writeUtf8(out, device);
            out.setByte(index, length);
        }
    }

    @Override
    public void decode(ByteBuf in) {
        notification = new Notification();
        notification.decode(in);

        int size = in.readUnsignedShort();
        devices = new ArrayList<String>(size);
        for (int i = 0; i < size; ++i) {
            int length = in.readUnsignedByte();
            String device = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
            in.skipBytes(length);

            devices.add(device);
        }
    }

    public Notification getNotification() {
        return notification;
    }

    public List<String> getDevices() {
        return devices;
    }
}
