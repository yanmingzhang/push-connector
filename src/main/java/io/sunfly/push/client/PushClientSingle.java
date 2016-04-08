package io.sunfly.push.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.sunfly.push.model.Notification;
import io.sunfly.push.wan.message.LoginRequest;
import io.sunfly.push.wan.message.MessageTypes;
import io.sunfly.push.wan.message.PushNotification;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.UUID;

public class PushClientSingle {
    public static void main(String[] args) throws Exception {
        String deviceId = String.format("push-test-%07d", 0);
        Socket sock = new Socket("127.0.0.1", 52572);
        DataOutputStream os = new DataOutputStream(sock.getOutputStream());
        DataInputStream is = new DataInputStream(sock.getInputStream());

        // send login message
        LoginRequest loginRequest = new LoginRequest(deviceId, Collections.<String, UUID>emptyMap());
        ByteBuf buf = Unpooled.buffer(loginRequest.estimateSize());
        int index = buf.writerIndex();
        buf.writerIndex(index + 2);     // size
        loginRequest.encode(buf);
        buf.setShort(index, buf.readableBytes());

        os.write(buf.array(), buf.readerIndex(), buf.readableBytes());
        System.out.println("Login request sent...");

        // receive push notifications
        while (true) {
            int size = (is.readShort() & 0xffff);
            int msgType = (is.readByte() & 0xff);

            if (msgType != MessageTypes.PUSH_NOTIFICATION) {
                System.err.println("Can't process message with type " + msgType);
                break;
            }

            byte[] data = new byte[size - 3];
            is.readFully(data);

            PushNotification pn = new PushNotification();
            pn.decode(Unpooled.wrappedBuffer(data));
            Notification notification = pn.getNotification();
            System.out.format("topic = %s, create time = %s, content = %s\n", notification.getTopic(), notification.getCreateTime(),
                    notification.getContent());
        }

        sock.close();

        System.out.println("Quitted...");
    }
}
