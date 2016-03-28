package io.sunfly.push.message;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class LoginRequest implements Message {
    private String deviceId;

    public LoginRequest() {

    }

    public LoginRequest(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void encode(ByteBuf out) {
        int index = out.writerIndex();
        out.writerIndex(index + 1);
        int length = ByteBufUtil.writeUtf8(out, deviceId);
        out.setByte(index, length);
    }

    @Override
    public void decode(ByteBuf in) {
        int length = in.readUnsignedByte();
        deviceId = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);
    }

    public String getDeviceId() {
        return deviceId;
    }
}
