package io.sunfly.push.wan.message;

public class MessageTypes {
    private static final int SERVER_TO_DEVICE_BASE = 128;

    // device to server message types
    public static final int REQ_LOGIN = 1;
    public static final int NOTIFICATION_ACK = 2;

    // server to device message types
    public static final int PUSH_NOTIFICATION = SERVER_TO_DEVICE_BASE + 1;
}
