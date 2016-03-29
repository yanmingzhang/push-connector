package io.sunfly.push.message;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public interface Message {
    public static final int MAX_BYTES_PER_CHAR_UTF8 =
            (int)CharsetUtil.encoder(CharsetUtil.UTF_8).maxBytesPerChar();

    public void encode(ByteBuf out);

    public void decode(ByteBuf in);

    // MUST equal to or larger than the required size
    public int estimateSize();
}
