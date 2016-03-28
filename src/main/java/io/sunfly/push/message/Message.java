package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;

public interface Message {
    void encode(ByteBuf out);
    void decode(ByteBuf in);
}
