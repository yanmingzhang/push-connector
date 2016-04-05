package io.sunfly.push.client;

import io.netty.buffer.Unpooled;
import io.sunfly.push.message.GetNotificationResponse;
import io.sunfly.push.message.MessageTypes;
import io.sunfly.push.model.Notification;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;

public class PushClientSingle {
    public static void main(String[] args) throws Exception {
        String deviceId = "push-test-000";
        Socket sock = new Socket("127.0.0.1", 52572);
        DataOutputStream os = new DataOutputStream(sock.getOutputStream());
        DataInputStream is = new DataInputStream(sock.getInputStream());

        // send login message
        byte[] bytes = deviceId.getBytes(StandardCharsets.UTF_8);

        int size = 3 + 1 + bytes.length;
        os.writeShort(size);
        os.writeByte(MessageTypes.REQ_LOGIN);
        os.writeByte(bytes.length);
        os.write(bytes);

        System.out.println("Login request sent...");

        // send get notification request
        size = 3 + 16;
        os.writeShort(size);
        os.writeByte(MessageTypes.REQ_GET_NOTIFICATION);
        UUID minCreateTime = UUIDs.startOf(0);
        os.writeLong(minCreateTime.getMostSignificantBits());
        os.writeLong(minCreateTime.getLeastSignificantBits());

        // receive response
        size = (is.readShort() & 0xffff);
        int msgType = (is.readByte() & 0xff);

        if (msgType == MessageTypes.RSP_GET_NOTIFICATION) {
            byte[] data = new byte[size - 3];
            is.readFully(data);

            GetNotificationResponse response = new GetNotificationResponse();
            response.decode(Unpooled.wrappedBuffer(data));

            List<Notification> notifications = response.getNotifications();
            for (Notification notification: notifications) {
                System.out.println("Content: " + notification.getContent());
            }
        } else {
            System.err.println("Bad response");
        }

        sock.close();

        System.out.println("Quitted...");
    }
}
